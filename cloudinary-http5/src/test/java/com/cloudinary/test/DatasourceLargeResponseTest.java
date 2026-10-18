package com.cloudinary.test;

import com.cloudinary.Cloudinary;
import com.cloudinary.api.ApiResponse;
import com.cloudinary.api.exceptions.BadRequest;
import com.cloudinary.api.exceptions.GeneralError;
import com.cloudinary.api.exceptions.NotFound;
import com.cloudinary.metadata.MetadataDataSource;
import com.cloudinary.utils.ObjectUtils;
import com.sun.net.httpserver.HttpServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Offline tests (local HTTP server) for handling of very large datasource responses.
 * Run with a constrained heap via the {@code memoryTest} gradle task to act as an OOM regression test.
 */
public class DatasourceLargeResponseTest {
    private static final int LARGE_ENTRIES = Integer.getInteger("datasource.test.entries", 300_000);

    private HttpServer server;
    private Cloudinary cloudinary;
    private volatile int status = 200;
    private volatile String errorBody;
    private volatile int responseEntries = 3;
    private volatile boolean truncate = false;
    private volatile String rawBody;
    private final AtomicInteger receivedEntries = new AtomicInteger();

    @Before
    public void setUp() throws Exception {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> {
            ByteArrayOutputStream body = new ByteArrayOutputStream();
            try (InputStream in = exchange.getRequestBody()) {
                byte[] buf = new byte[8192];
                int n;
                while ((n = in.read(buf)) > 0) body.write(buf, 0, n);
            }
            String req = new String(body.toByteArray(), StandardCharsets.UTF_8);
            int count = 0;
            for (int i = req.indexOf("\"external_id\""); i >= 0; i = req.indexOf("\"external_id\"", i + 1)) count++;
            receivedEntries.set(count);

            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
            exchange.getResponseHeaders().set("X-FeatureRateLimit-Limit", "500");
            exchange.getResponseHeaders().set("X-FeatureRateLimit-Remaining", "499");
            exchange.getResponseHeaders().set("X-FeatureRateLimit-Reset", "Wed, 01 Jan 2031 00:00:00 GMT");
            if (status != 200) {
                byte[] err = errorBody.getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(status, err.length);
                try (OutputStream os = exchange.getResponseBody()) { os.write(err); }
                return;
            }
            if (rawBody != null) {
                byte[] raw = rawBody.getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(200, raw.length == 0 ? -1 : raw.length);
                if (raw.length > 0) {
                    try (OutputStream os = exchange.getResponseBody()) { os.write(raw); }
                }
                exchange.close();
                return;
            }
            if (truncate) {
                // promise more bytes than we send, then drop the connection mid-body
                exchange.sendResponseHeaders(200, 1_000_000);
                OutputStream os = exchange.getResponseBody();
                os.write("{\"external_id\":\"upc\",\"datasource\":{\"values\":[{\"external_id\":\"a".getBytes(StandardCharsets.UTF_8));
                os.flush();
                exchange.close();
                return;
            }
            exchange.sendResponseHeaders(200, 0); // chunked, generated on the fly
            try (OutputStream os = exchange.getResponseBody()) {
                os.write("{\"external_id\":\"upc\",\"datasource\":{\"values\":[".getBytes(StandardCharsets.UTF_8));
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < responseEntries; i++) {
                    sb.append(i == 0 ? "" : ",").append("{\"external_id\":\"id").append(i)
                            .append("\",\"value\":\"vé中").append(i).append("\"}");
                    if (sb.length() > 8192) { os.write(sb.toString().getBytes(StandardCharsets.UTF_8)); sb.setLength(0); }
                }
                os.write(sb.append("]}}").toString().getBytes(StandardCharsets.UTF_8));
            }
        });
        server.start();
        Map<String, Object> config = ObjectUtils.asMap(
                "cloud_name", "test", "api_key", "key", "api_secret", "secret",
                "upload_prefix", "http://127.0.0.1:" + server.getAddress().getPort());
        cloudinary = new Cloudinary(config);
    }

    @After
    public void tearDown() {
        server.stop(0);
    }

    private static List<MetadataDataSource.Entry> entries(int n) {
        List<MetadataDataSource.Entry> list = new ArrayList<>(n);
        for (int i = 0; i < n; i++) list.add(new MetadataDataSource.Entry("id" + i, "value" + i));
        return list;
    }

    @Test
    public void testSmallResponseParsedWithUtf8() throws Exception {
        ApiResponse res = cloudinary.api().updateMetadataFieldDatasource("upc", entries(2));
        assertEquals("upc", res.get("external_id"));
        List<?> values = (List<?>) ((Map<?, ?>) ((Map<?, ?>) res.get("datasource"))).get("values");
        assertEquals(3, values.size());
        assertEquals("vé中1", ((Map<?, ?>) values.get(1)).get("value"));
        assertEquals(2, receivedEntries.get());
        assertEquals(500L, res.apiRateLimit().getLimit());
    }

    @Test
    public void testSkipResponseBodyReturnsEmptyResponseWithRateLimits() throws Exception {
        responseEntries = LARGE_ENTRIES;
        ApiResponse res = cloudinary.api().updateMetadataFieldDatasource("upc", entries(10),
                ObjectUtils.asMap("skip_response_body", true));
        assertTrue(res.isEmpty());
        assertEquals(499L, res.apiRateLimit().getRemaining());
    }

    @Test
    public void testLargeRequestSentIntact() throws Exception {
        cloudinary.api().updateMetadataFieldDatasource("upc", entries(LARGE_ENTRIES),
                ObjectUtils.asMap("skip_response_body", true));
        assertEquals(LARGE_ENTRIES, receivedEntries.get());
    }

    @Test
    public void testLargeResponseParsed() throws Exception {
        responseEntries = LARGE_ENTRIES;
        ApiResponse res = cloudinary.api().updateMetadataFieldDatasource("upc", entries(1));
        List<?> values = (List<?>) ((Map<?, ?>) res.get("datasource")).get("values");
        assertEquals(LARGE_ENTRIES, values.size());
    }

    @Test
    public void testConnectionDroppedMidBodyThrowsGeneralError() throws Exception {
        truncate = true;
        try {
            cloudinary.api().updateMetadataFieldDatasource("upc", entries(1));
            fail("expected GeneralError");
        } catch (GeneralError e) {
            assertTrue(e.getMessage(), e.getMessage().startsWith("Error executing request"));
        }
    }

    @Test
    public void testMalformedJsonThrowsInvalidJsonRuntimeException() throws Exception {
        rawBody = "{\"external_id\": not json";
        try {
            cloudinary.api().updateMetadataFieldDatasource("upc", entries(1));
            fail("expected RuntimeException");
        } catch (RuntimeException e) {
            assertTrue(e.getMessage(), e.getMessage().startsWith("Invalid JSON response from server"));
        }
    }

    @Test
    public void testEmptyBodyThrowsInvalidJsonRuntimeException() throws Exception {
        rawBody = "";
        try {
            cloudinary.api().updateMetadataFieldDatasource("upc", entries(1));
            fail("expected RuntimeException");
        } catch (RuntimeException e) {
            assertTrue(e.getMessage(), e.getMessage().startsWith("Invalid JSON response from server"));
        }
    }

    @Test
    public void testErrorStatusWithNonJsonBodyThrowsInvalidJsonRuntimeException() throws Exception {
        status = 500;
        errorBody = "<html>Internal Server Error</html>";
        try {
            cloudinary.api().updateMetadataFieldDatasource("upc", entries(1));
            fail("expected RuntimeException");
        } catch (RuntimeException e) {
            assertTrue(e.getMessage(), e.getMessage().startsWith("Invalid JSON response from server"));
        }
    }

    @Test
    public void testErrorMappedToException() throws Exception {
        status = 404;
        errorBody = "{\"error\":{\"message\":\"nope\"}}";
        try {
            cloudinary.api().updateMetadataFieldDatasource("upc", entries(1));
            fail("expected NotFound");
        } catch (NotFound e) {
            assertEquals("nope", e.getMessage());
        }
    }

    @Test
    public void testErrorWithoutErrorKeyDoesNotCrash() throws Exception {
        status = 400;
        errorBody = "{\"message\":\"bad\"}";
        try {
            cloudinary.api().updateMetadataFieldDatasource("upc", entries(1));
            fail("expected BadRequest");
        } catch (BadRequest e) {
            // mapped to proper exception rather than NPE/ClassCastException
        }
    }
}
