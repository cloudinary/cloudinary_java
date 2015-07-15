package com.cloudinary.http44;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Map;

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
import org.cloudinary.json.JSONException;
import org.cloudinary.json.JSONObject;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import com.cloudinary.Util;
import com.cloudinary.strategies.AbstractUploaderStrategy;
import com.cloudinary.utils.ObjectUtils;
import com.cloudinary.utils.StringUtils;

public class UploaderStrategy extends AbstractUploaderStrategy {
	
	private CloseableHttpClient client = null;
	@Override 
	public void init(Uploader uploader) {
		super.init(uploader);
		
		HttpClientBuilder clientBuilder = HttpClients.custom();
		clientBuilder.useSystemProperties().setUserAgent(Cloudinary.USER_AGENT);
		
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
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Map callApi(String action, Map<String, Object> params, Map options, Object file) throws IOException {
		// initialize options if passed as null
		if (options == null) {
			options = ObjectUtils.emptyMap();
		}

		boolean returnError = ObjectUtils.asBoolean(options.get("return_error"), false);

		if (options.get("unsigned") == null || Boolean.FALSE.equals(options.get("unsigned"))) {
			uploader.signRequestParams(params, options);
		} else {
			Util.clearEmpty(params);
		}

		String apiUrl = uploader.cloudinary().cloudinaryApiUrl(action, options);

		HttpPost postMethod = new HttpPost(apiUrl);
		
		if (options.get("content_range") != null) {
			postMethod.setHeader("Content-Range", (String) options.get("content_range")); 
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

		if (file instanceof String && !((String) file).matches("ftp:.*|https?:.*|s3:.*|data:[^;]*;base64,([a-zA-Z0-9/+\n=]+)")) {
			file = new File((String) file);
		}
		if (file instanceof File) {
			multipart.addBinaryBody("file", (File) file);
		} else if (file instanceof String) {
			multipart.addTextBody("file", (String) file);
		} else if (file instanceof byte[]) {
			multipart.addBinaryBody("file", (byte[]) file, ContentType.APPLICATION_OCTET_STREAM, "file");
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

		if (code != 200 && code != 400 && code != 500) {
			throw new RuntimeException("Server returned unexpected status code - " + code + " - " + responseData);
		}

		Map result;
		
		try {
			JSONObject responseJSON = new JSONObject(responseData);
			result= ObjectUtils.toMap(responseJSON);
		} catch (JSONException e) {
			throw new RuntimeException("Invalid JSON response from server " + e.getMessage());
		}
		
		if (result.containsKey("error")) {
			Map error = (Map) result.get("error");
			if (returnError) {
				error.put("http_code", code);
			} else {
				throw new RuntimeException((String) error.get("message"));
			}
		}
		return result;
	}

}
