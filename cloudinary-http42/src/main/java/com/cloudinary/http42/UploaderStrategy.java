package com.cloudinary.http42;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

import com.cloudinary.Cloudinary;
import com.cloudinary.Util;
import com.cloudinary.strategies.AbstractUploaderStrategy;
import com.cloudinary.utils.ObjectUtils;
import com.cloudinary.utils.StringUtils;

public class UploaderStrategy extends AbstractUploaderStrategy {

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Map callApi(String action, Map<String, Object> params, Map options, Object file) throws IOException {
		// initialize options if passed as null
		if (options == null){
			options = ObjectUtils.emptyMap();
		}
			
		boolean returnError = ObjectUtils.asBoolean(options.get("return_error"), false);

		if (options.get("unsigned") == null || Boolean.FALSE.equals(options.get("unsigned"))) {
			uploader.signRequestParams(params, options);
		} else {
			Util.clearEmpty(params);
		}

		String apiUrl = uploader.cloudinary().cloudinaryApiUrl(action, options);

		HttpClient client = new DefaultHttpClient(uploader.connectionManager);

		HttpPost postMethod = new HttpPost(apiUrl);
		postMethod.setHeader("User-Agent", Cloudinary.USER_AGENT);

		MultipartEntity multipart = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
		// Remove blank parameters
		for (Map.Entry<String, Object> param : params.entrySet()) {
			if (param.getValue() instanceof Collection) {
				for (Object value : (Collection) param.getValue()) {
					multipart.addPart(param.getKey() + "[]", new StringBody(ObjectUtils.asString(value)));
				}
			} else {
				String value = param.getValue().toString();
				if (StringUtils.isNotBlank(value)) {
					multipart.addPart(param.getKey(), new StringBody(value));
				}
			}
		}

		if (file instanceof String && !((String) file).matches("https?:.*|s3:.*|data:[^;]*;base64,([a-zA-Z0-9/+\n=]+)")) {
			file = new File((String) file);
		}
		if (file instanceof File) {
			multipart.addPart("file", new FileBody((File) file));
		} else if (file instanceof String) {
			multipart.addPart("file", new StringBody((String) file));
		} else if (file instanceof byte[]) {
			multipart.addPart("file", new ByteArrayBody((byte[]) file, "file"));
		} else if (file == null) {
			// no-problem
		} else {
			throw new IOException("Uprecognized file parameter " + file);
		}
		postMethod.setEntity(multipart);

		HttpResponse response = client.execute(postMethod);
		int code = response.getStatusLine().getStatusCode();
		InputStream responseStream = response.getEntity().getContent();
		String responseData = StringUtils.read(responseStream);

		if (code != 200 && code != 400 && code != 500) {
			throw new RuntimeException("Server returned unexpected status code - " + code + " - " + responseData);
		}

		Map result;
		try {
			result = (Map) JSONValue.parseWithException(responseData);
		} catch (ParseException e) {
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
