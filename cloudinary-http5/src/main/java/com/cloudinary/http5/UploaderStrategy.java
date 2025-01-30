package com.cloudinary.http5;

import com.cloudinary.ProgressCallback;
import com.cloudinary.Uploader;
import com.cloudinary.Util;
import com.cloudinary.strategies.AbstractUploaderStrategy;
import com.cloudinary.utils.ObjectUtils;
import com.cloudinary.utils.StringUtils;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.entity.mime.ByteArrayBody;
import org.apache.hc.client5.http.entity.mime.FileBody;
import org.apache.hc.client5.http.entity.mime.HttpMultipartMode;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.util.Timeout;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Map;

public class UploaderStrategy extends AbstractUploaderStrategy {

    private static final String APACHE_HTTP_CLIENT_VERSION = System.getProperty("apache.http.client.version", "5.3.1");

    private CloseableHttpClient client;

    @Override
    public void init(Uploader uploader) {
        super.init(uploader);

        HttpClientBuilder clientBuilder = HttpClients.custom();
        clientBuilder.useSystemProperties().setUserAgent(cloudinary().getUserAgent() + " ApacheHttpClient/" + APACHE_HTTP_CLIENT_VERSION);

        HttpClientConnectionManager connectionManager = (HttpClientConnectionManager) cloudinary().config.properties.get("connectionManager");
        if (connectionManager != null) {
            clientBuilder.setConnectionManager(connectionManager);
        }

        RequestConfig requestConfig = buildRequestConfig();

        client = clientBuilder
                .setDefaultRequestConfig(requestConfig)
                .build();
    }

    public RequestConfig buildRequestConfig() {
        RequestConfig.Builder requestConfigBuilder = RequestConfig.custom();

        if (cloudinary().config.proxyHost != null && cloudinary().config.proxyPort != 0) {
            HttpHost proxy = new HttpHost(cloudinary().config.proxyHost, cloudinary().config.proxyPort);
            requestConfigBuilder.setProxy(proxy);
        }

        int timeout = cloudinary().config.timeout;
        if (timeout > 0) {
            requestConfigBuilder.setResponseTimeout(Timeout.ofSeconds(timeout))
                    .setConnectionRequestTimeout(Timeout.ofSeconds(timeout))
                    .setConnectTimeout(Timeout.ofSeconds(timeout));
        }

        return requestConfigBuilder.build();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public Map callApi(String action, Map<String, Object> params, Map options, Object file, ProgressCallback progressCallback) throws IOException {
        if (progressCallback != null) {
            throw new IllegalArgumentException("Progress callback is not supported");
        }

        // Initialize options if passed as null
        if (options == null) {
            options = ObjectUtils.emptyMap();
        }

        boolean returnError = ObjectUtils.asBoolean(options.get("return_error"), false);

        if (requiresSigning(action, options)) {
            uploader.signRequestParams(params, options);
        } else {
            Util.clearEmpty(params);
        }

        String apiUrl = buildUploadUrl(action, options);

        // Prepare the request
        HttpUriRequestBase request = prepareRequest(apiUrl, params, options, file);

        // Execute the request and handle the response
        String responseData;
        int code;

        try (CloseableHttpResponse response = client.execute(request)) {
            code = response.getCode();
            responseData = EntityUtils.toString(response.getEntity());
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        // Process and return the response
        return processResponse(returnError, code, responseData);
    }

    private HttpUriRequestBase prepareRequest(String apiUrl, Map<String, Object> params, Map<String, ?> options, Object file) throws IOException {
        HttpPost request = new HttpPost(apiUrl);

        MultipartEntityBuilder multipartBuilder = MultipartEntityBuilder.create()
                .setCharset(StandardCharsets.UTF_8).setMode(HttpMultipartMode.LEGACY);

        // Add text parameters
        for (Map.Entry<String, Object> param : params.entrySet()) {
            if (param.getValue() instanceof Collection) {
                for (Object value : (Collection<?>) param.getValue()) {
                    multipartBuilder.addTextBody(param.getKey() + "[]", ObjectUtils.asString(value), ContentType.TEXT_PLAIN.withCharset(StandardCharsets.UTF_8));
                }
            } else {
                String value = param.getValue().toString();
                if (StringUtils.isNotBlank(value)) {
                    multipartBuilder.addTextBody(param.getKey(), value, ContentType.TEXT_PLAIN.withCharset(StandardCharsets.UTF_8));
                }
            }
        }

        // Add file part
        addFilePart(multipartBuilder, file, options);

        request.setEntity(multipartBuilder.build());

        // Add extra headers if provided
        Map<String, String> extraHeaders = (Map<String, String>) options.get("extra_headers");
        if (extraHeaders != null) {
            for (Map.Entry<String, String> header : extraHeaders.entrySet()) {
                request.addHeader(header.getKey(), header.getValue());
            }
        }

        return request;
    }


    private void addFilePart(MultipartEntityBuilder multipartBuilder, Object file, Map<String, ?> options) throws IOException {
        String filename = (String) options.get("filename");

        if (file instanceof String && !StringUtils.isRemoteUrl((String) file)) {
            File _file = new File((String) file);
            if (!_file.isFile() || !_file.canRead()) {
                throw new IOException("File not found or unreadable: " + file);
            }
            file = _file;
        }

        if (file instanceof File) {
            if (filename == null) {
                filename = ((File) file).getName();
            }
            // Encode filename properly
            filename = new String(filename.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);

            // Create FileBody with correct filename encoding
            FileBody fileBody = new FileBody((File) file, ContentType.APPLICATION_OCTET_STREAM, filename);
            multipartBuilder.addPart("file", fileBody);
        } else if (file instanceof String) {
            multipartBuilder.addTextBody("file", (String) file, ContentType.TEXT_PLAIN);
        } else if (file instanceof byte[]) {
            if (filename == null) {
                filename = "file";
            }
            ByteArrayBody byteArrayBody = new ByteArrayBody((byte[]) file, ContentType.APPLICATION_OCTET_STREAM, filename);
            multipartBuilder.addPart("file", byteArrayBody);
        } else if (file == null) {
            // No file to add
        } else {
            throw new IOException("Unrecognized file parameter " + file);
        }
    }
}
