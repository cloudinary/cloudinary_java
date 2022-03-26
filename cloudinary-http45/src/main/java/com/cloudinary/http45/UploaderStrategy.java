package com.cloudinary.http45;

import com.cloudinary.Cloudinary;
import com.cloudinary.ProgressCallback;
import com.cloudinary.Uploader;
import com.cloudinary.Util;
import com.cloudinary.strategies.AbstractUploaderStrategy;
import com.cloudinary.utils.ObjectUtils;
import com.cloudinary.utils.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MIME;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Map;

public class UploaderStrategy extends AbstractUploaderStrategy {

    private CloseableHttpClient client = null;

    @Override
    public void init(Uploader uploader) {
        super.init(uploader);

        HttpClientBuilder clientBuilder = HttpClients.custom();
        clientBuilder.useSystemProperties().setUserAgent(cloudinary().getUserAgent() + " ApacheHTTPComponents/4.5");

        // If the configuration specifies a proxy then apply it to the client
        if (cloudinary().config.proxyHost != null && cloudinary().config.proxyPort != 0) {
            HttpHost proxy = new HttpHost(cloudinary().config.proxyHost, cloudinary().config.proxyPort);
            clientBuilder.setProxy(proxy);
        }

        HttpClientConnectionManager connectionManager = (HttpClientConnectionManager) cloudinary().config.properties.get("connectionManager");
        if (connectionManager != null) {
            clientBuilder.setConnectionManager(connectionManager);
        }

        this.client = clientBuilder.build();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public Map callApi(String action, Map<String, Object> params, Map options, Object file, ProgressCallback progressCallback) throws IOException {
        if (progressCallback != null){
            throw new IllegalArgumentException("Progress callback is not supported");
        }

        // initialize options if passed as null
        if (options == null) {
            options = ObjectUtils.emptyMap();
        }

        boolean returnError = ObjectUtils.asBoolean(options.get("return_error"), false);

        HttpPost postMethod = createPostMethod(action, params, options);
        ApiUtils.setTimeouts(postMethod, options);

        Map<String, String> extraHeaders = (Map<String, String>) options.get("extra_headers");
        if (extraHeaders != null) {
            for (Map.Entry<String, String> header : extraHeaders.entrySet()) {
                postMethod.setHeader(header.getKey(), header.getValue());
            }
        }

        MultipartEntityBuilder multipart = MultipartEntityBuilder.create();
        multipart.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        ContentType contentType = ContentType.MULTIPART_FORM_DATA.withCharset(MIME.UTF8_CHARSET);
        // Remove blank parameters
        for (Map.Entry<String, Object> param : params.entrySet()) {
            if (param.getValue() instanceof Collection) {
                for (Object value : (Collection) param.getValue()) {
                    multipart.addTextBody(param.getKey() + "[]", ObjectUtils.asString(value), contentType);
                }
            } else {
                String value = param.getValue().toString();
                if (StringUtils.isNotBlank(value)) {
                    multipart.addTextBody(param.getKey(), value, contentType);
                }
            }
        }

        if (file instanceof String && !StringUtils.isRemoteUrl((String) file)) {
            File _file = new File((String) file);
            if (!_file.isFile() && !_file.canRead()) {
                throw new IOException("File not found or unreadable: " + file);
            }
            file = _file;
        }
        String filename = (String) options.get("filename");
        if (file instanceof File) {
            if (filename == null) filename = ((File) file).getName();
            multipart.addBinaryBody("file", (File) file, ContentType.APPLICATION_OCTET_STREAM, filename);
        } else if (file instanceof String) {
            multipart.addTextBody("file", (String) file, contentType);
        } else if (file instanceof byte[]) {
            if (filename == null) filename = "file";
            multipart.addBinaryBody("file", (byte[]) file, ContentType.APPLICATION_OCTET_STREAM, filename);
        } else if (file == null) {
            // no-problem
        } else {
            throw new IOException("Unrecognized file parameter " + file);
        }
        postMethod.setEntity(multipart.build());

        String responseData = null;
        int code = 0;
        CloseableHttpResponse response = client.execute(postMethod);
        try {
            code = response.getStatusLine().getStatusCode();
            InputStream responseStream = response.getEntity().getContent();
            responseData = StringUtils.read(responseStream);
        } finally {
            response.close();
        }

        Map result = processResponse(returnError, code, responseData);
        return result;
    }
}
