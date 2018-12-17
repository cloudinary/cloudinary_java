package com.cloudinary.http42;

import com.cloudinary.Cloudinary;
import com.cloudinary.ProgressCallback;
import com.cloudinary.Util;
import com.cloudinary.strategies.AbstractUploaderStrategy;
import com.cloudinary.utils.ObjectUtils;
import com.cloudinary.utils.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Map;

public class UploaderStrategy extends AbstractUploaderStrategy {

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

        if (requiresSigning(action, options)) {
            uploader.signRequestParams(params, options);
        } else {
            Util.clearEmpty(params);
        }

        String apiUrl = buildUploadUrl(action, options);

        ClientConnectionManager connectionManager = (ClientConnectionManager) this.uploader.cloudinary().config.properties.get("connectionManager");
        HttpClient client = new DefaultHttpClient(connectionManager);

        // If the configuration specifies a proxy then apply it to the client
        if (uploader.cloudinary().config.proxyHost != null && uploader.cloudinary().config.proxyPort != 0) {
            HttpHost proxy = new HttpHost(uploader.cloudinary().config.proxyHost, uploader.cloudinary().config.proxyPort);
            client.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
        }

        HttpPost postMethod = new HttpPost(apiUrl);
        postMethod.setHeader("User-Agent", Cloudinary.USER_AGENT + " ApacheHTTPComponents/4.2");

        Map<String, String> extraHeaders = (Map<String, String>) options.get("extra_headers");
        if (extraHeaders != null) {
            for (Map.Entry<String, String> header : extraHeaders.entrySet()) {
                postMethod.setHeader(header.getKey(), header.getValue());
            }
        }

        Charset utf8 = Charset.forName("UTF-8");

        MultipartEntity multipart = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
        // Remove blank parameters
        for (Map.Entry<String, Object> param : params.entrySet()) {
            if (param.getValue() instanceof Collection) {
                for (Object value : (Collection) param.getValue()) {
                    multipart.addPart(param.getKey() + "[]", new StringBody(ObjectUtils.asString(value), utf8));
                }
            } else {
                String value = param.getValue().toString();
                if (StringUtils.isNotBlank(value)) {
                    multipart.addPart(param.getKey(), new StringBody(value, utf8));
                }
            }
        }

        if (file instanceof String && !((String) file).matches("ftp:.*|https?:.*|s3:.*|gs:.*|data:[^;]*;base64,([a-zA-Z0-9/+\n=]+)")) {
            File _file = new File((String) file);
            if (!_file.isFile() && !_file.canRead()) {
                throw new IOException("File not found or unreadable: " + file);
            }
            file = _file;
        }
        String filename = (String) options.get("filename");
        if (file instanceof File) {
            multipart.addPart("file", new FileBody((File) file, filename, "application/octet-stream", null));
        } else if (file instanceof String) {
            multipart.addPart("file", new StringBody((String) file, utf8));
        } else if (file instanceof byte[]) {
            if (filename == null) filename = "file";
            multipart.addPart("file", new ByteArrayBody((byte[]) file, filename));
        } else if (file == null) {
            // no-problem
        } else {
            throw new IOException("Unrecognized file parameter " + file);
        }
        postMethod.setEntity(multipart);

        HttpResponse response = client.execute(postMethod);
        int code = response.getStatusLine().getStatusCode();
        InputStream responseStream = response.getEntity().getContent();
        String responseData = StringUtils.read(responseStream);

        return processResponse(returnError, code, responseData);
    }

}
