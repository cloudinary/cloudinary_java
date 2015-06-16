package com.cloudinary.android;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.Collection;
import java.util.Map;

import org.cloudinary.json.JSONException;
import org.cloudinary.json.JSONObject;

import com.cloudinary.strategies.AbstractUploaderStrategy;
import com.cloudinary.utils.ObjectUtils;
import com.cloudinary.utils.StringUtils;

public class UploaderStrategy extends AbstractUploaderStrategy {

	@SuppressWarnings("rawtypes")
	@Override
	public Map callApi(String action, Map<String, Object> params, Map options, Object file) throws IOException {
		// initialize options if passed as null
		if (options == null) {
			options = ObjectUtils.emptyMap();
		}
		boolean returnError = ObjectUtils.asBoolean(options.get("return_error"), false);

		if (Boolean.TRUE.equals(options.get("unsigned"))) {
			// Nothing to do
		} else {
			String apiKey = ObjectUtils.asString(options.get("api_key"), this.cloudinary().config.apiKey);
			if (apiKey == null)
				throw new IllegalArgumentException("Must supply api_key");
			if (options.containsKey("signature") && options.containsKey("timestamp")) {
				params.put("timestamp", options.get("timestamp"));
				params.put("signature", options.get("signature"));
				params.put("api_key", apiKey);
			} else {
				String apiSecret = ObjectUtils.asString(options.get("api_secret"), this.cloudinary().config.apiSecret);
				if (apiSecret == null)
					throw new IllegalArgumentException("Must supply api_secret");
				params.put("timestamp", Long.valueOf(System.currentTimeMillis() / 1000L).toString());
				params.put("signature", this.cloudinary().apiSignRequest(params, apiSecret));
				params.put("api_key", apiKey);
			}
		}
		String apiUrl = this.cloudinary().cloudinaryApiUrl(action, options);
		MultipartUtility multipart = new MultipartUtility(apiUrl, "UTF-8", this.cloudinary().randomPublicId(), (String) options.get("content_range"));

		// Remove blank parameters
		for (Map.Entry<String, Object> param : params.entrySet()) {
			if (param.getValue() instanceof Collection) {
				for (Object value : (Collection) param.getValue()) {
					multipart.addFormField(param.getKey() + "[]", ObjectUtils.asString(value));
				}
			} else {
				if (StringUtils.isNotBlank(param.getValue())) {
					multipart.addFormField(param.getKey(), param.getValue().toString());
				}
			}
		}

		if (file instanceof String && !((String) file).matches("ftp:.*|https?:.*|s3:.*|data:[^;]*;base64,([a-zA-Z0-9/+\n=]+)")) {
			file = new File((String) file);
		}
		if (file instanceof File) {
			multipart.addFilePart("file", (File) file);
		} else if (file instanceof String) {
			multipart.addFormField("file", (String) file);
		} else if (file instanceof InputStream) {
			multipart.addFilePart("file", (InputStream) file);
		} else if (file instanceof byte[]) {
			multipart.addFilePart("file", new ByteArrayInputStream((byte[]) file));
		}
		HttpURLConnection connection = multipart.execute();
		int code;
		try {
			code = connection.getResponseCode();
		} catch (IOException e) {
			if (e.getMessage().equals("No authentication challenges found")) {
				// Android trying to be clever...
				code = 401;
			} else {
				throw e;
			}
		}
		InputStream responseStream = code >= 400 ? connection.getErrorStream() : connection.getInputStream();
		String responseData = readFully(responseStream);
		connection.disconnect();

		if (code != 200 && code != 400 && code != 500) {
			throw new RuntimeException("Server returned unexpected status code - " + code + " - " + responseData);
		}

		JSONObject result;
		try {
			result = new JSONObject(responseData);
			if (result.has("error")) {
				JSONObject error = result.getJSONObject("error");
				if (returnError) {
					error.put("http_code", code);
				} else {
					throw new RuntimeException(error.getString("message"));
				}
			}
			return ObjectUtils.toMap(result);
		} catch (JSONException e) {
			throw new RuntimeException("Invalid JSON response from server " + e.getMessage());
		}
	}

	protected static String readFully(InputStream in) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int length = 0;
		while ((length = in.read(buffer)) != -1) {
			baos.write(buffer, 0, length);
		}
		return new String(baos.toByteArray());
	}
}
