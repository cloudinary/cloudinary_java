package com.cloudinary.okhttp;

import com.cloudinary.ProgressCallback;
import com.cloudinary.Uploader;
import com.cloudinary.Util;
import com.cloudinary.strategies.AbstractUploaderStrategy;
import com.cloudinary.utils.ObjectUtils;
import com.cloudinary.utils.StringUtils;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import okhttp3.*;
import okhttp3.RequestBody;
import okhttp3.MultipartBody;
import okio.BufferedSink;
import okio.Okio;
import okio.Source;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class UploaderStrategy extends AbstractUploaderStrategy {

    private OkHttpClient client;

    @Override
    public void init(Uploader uploader) {
        super.init(uploader);

        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    Request original = chain.request();
                    Request.Builder requestBuilder = original.newBuilder()
                            .header("User-Agent", cloudinary().getUserAgent() + " OkHttpClient/4.x");
                    return chain.proceed(requestBuilder.build());
                });

        // If the configuration specifies a proxy, apply it to the client
        if (cloudinary().config.proxyHost != null && cloudinary().config.proxyPort != 0) {
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(cloudinary().config.proxyHost, cloudinary().config.proxyPort));
            clientBuilder.proxy(proxy);
        }

        // Apply connection timeout if specified in the configuration
        if (cloudinary().config.properties.containsKey("connectionTimeout")) {
            int connectionTimeout = (Integer) cloudinary().config.properties.get("connectionTimeout");
            clientBuilder.connectTimeout(connectionTimeout, TimeUnit.MILLISECONDS);
        }

        this.client = clientBuilder.build();
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
        Request request = prepareRequest(apiUrl, params, options, file);

        // Execute the request and handle the response
        String responseData = null;
        int code = 0;
        try (Response response = client.newCall(request).execute()) {
            code = response.code();
            responseData = response.body().string();
        }

        // Process and return the response
        return processResponse(returnError, code, responseData);
    }

    private Request prepareRequest(String apiUrl, Map<String, Object> params, Map<String, ?> options, Object file) throws IOException {
        MultipartBody.Builder multipartBuilder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM);

        // Add text parameters
        for (Map.Entry<String, Object> param : params.entrySet()) {
            if (param.getValue() instanceof Collection) {
                for (Object value : (Collection) param.getValue()) {
                    multipartBuilder.addFormDataPart(param.getKey() + "[]", ObjectUtils.asString(value));
                }
            } else {
                String value = param.getValue().toString();
                if (StringUtils.isNotBlank(value)) {
                    multipartBuilder.addFormDataPart(param.getKey(), value);
                }
            }
        }

        // Add file part
        addFilePart(multipartBuilder, file, options);

        // Build the request body
        RequestBody requestBody = multipartBuilder.build();

        // Prepare the request builder
        Request.Builder requestBuilder = new Request.Builder()
                .url(apiUrl)
                .post(requestBody);

        // Add extra headers if provided
        Map<String, String> extraHeaders = (Map<String, String>) options.get("extra_headers");
        if (extraHeaders != null) {
            for (Map.Entry<String, String> header : extraHeaders.entrySet()) {
                requestBuilder.addHeader(header.getKey(), header.getValue());
            }
        }

        return requestBuilder.build();
    }


    private void addFilePart(MultipartBody.Builder multipartBuilder, Object file, Map<String, ?> options) throws IOException {
        String filename = (String) options.get("filename");

        if (file instanceof String && !StringUtils.isRemoteUrl((String) file)) {
            File _file = new File((String) file);
            if (!_file.isFile() && !_file.canRead()) {
                throw new IOException("File not found or unreadable: " + file);
            }
            file = _file;
        }

        if (file instanceof File) {
            if (filename == null) filename = ((File) file).getName();
            multipartBuilder.addFormDataPart("file", filename, createFileRequestBody((File) file));
        } else if (file instanceof String) {
            multipartBuilder.addFormDataPart("file", (String) file);
        } else if (file instanceof byte[]) {
            if (filename == null) filename = "file";
            multipartBuilder.addFormDataPart("file", filename, createByteArrayRequestBody((byte[]) file));
        } else if (file == null) {
            // No file to add
        } else {
            throw new IOException("Unrecognized file parameter " + file);
        }
    }

    private RequestBody createFileRequestBody(File file) {
        return new RequestBody() {
            @Override
            public MediaType contentType() {
                return MediaType.parse("application/octet-stream");
            }

            @Override
            public long contentLength() throws IOException {
                return file.length();
            }

            @Override
            public void writeTo(BufferedSink sink) throws IOException {
                try (Source source = Okio.source(file)) {
                    sink.writeAll(source);
                }
            }
        };
    }

    private RequestBody createByteArrayRequestBody(byte[] data) {
        return new RequestBody() {
            @Override
            public MediaType contentType() {
                return MediaType.parse("application/octet-stream");
            }

            @Override
            public long contentLength() {
                return data.length;
            }

            @Override
            public void writeTo(BufferedSink sink) throws IOException {
                sink.write(data);
            }
        };
    }
}
