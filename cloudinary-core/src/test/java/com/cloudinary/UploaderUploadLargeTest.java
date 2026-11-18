package com.cloudinary;

import com.cloudinary.strategies.AbstractUploaderStrategy;
import com.cloudinary.utils.ObjectUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class UploaderUploadLargeTest {
    private Cloudinary cloudinary;

    @Before
    public void setUp() {
        cloudinary = new Cloudinary("cloudinary://a:b@test123?load_strategies=false&analytics=false");
    }

    @Test
    public void testUploadLargeContentRangeBeyondIntRange() throws IOException {
        // Offsets past Integer.MAX_VALUE must not overflow into negative Content-Range values.
        final int chunkSize = 20 * 1024 * 1024;
        final long totalSize = 2300000000L;
        RecordingUploaderStrategy strategy = new RecordingUploaderStrategy();
        Uploader uploader = new Uploader(cloudinary, strategy);

        uploader.uploadLarge(new SizedInputStream(totalSize), new HashMap(), chunkSize);

        List<String> ranges = strategy.contentRanges;
        int chunks = (int) ((totalSize + chunkSize - 1) / chunkSize);
        assertEquals(chunks, ranges.size());
        for (int i = 0; i < chunks; i++) {
            String expectedPrefix = "bytes " + ((long) i * chunkSize) + "-";
            assertTrue("Content-Range of chunk " + i + " should start with '" + expectedPrefix + "' but was '" + ranges.get(i) + "'",
                    ranges.get(i).startsWith(expectedPrefix));
        }
        long lastStart = (long) (chunks - 1) * chunkSize;
        assertEquals("bytes " + lastStart + "-" + (totalSize - 1) + "/" + totalSize, ranges.get(chunks - 1));
    }

    private static class RecordingUploaderStrategy extends AbstractUploaderStrategy {
        final List<String> contentRanges = new ArrayList<String>();

        @Override
        public Map callApi(String action, Map<String, Object> params, Map options, Object file, ProgressCallback progressCallback) {
            Map extraHeaders = (Map) options.get("extra_headers");
            contentRanges.add((String) extraHeaders.get("Content-Range"));
            return ObjectUtils.asMap("public_id", "test");
        }
    }

    /**
     * Reports a fixed number of bytes without filling the buffer, so large sizes cost no memory.
     */
    private static class SizedInputStream extends InputStream {
        private long remaining;

        SizedInputStream(long size) {
            this.remaining = size;
        }

        @Override
        public int read() {
            if (remaining <= 0) return -1;
            remaining--;
            return 0;
        }

        @Override
        public int read(byte[] b, int off, int len) {
            if (remaining <= 0) return -1;
            int count = (int) Math.min(len, remaining);
            remaining -= count;
            return count;
        }
    }
}
