package com.cloudinary;

import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.net.URI;
import java.util.Arrays;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

import com.cloudinary.api.ApiBase;
import com.cloudinary.api.ApiResponse;
import com.cloudinary.api.Response;
import com.cloudinary.api.exceptions.GeneralError;
import com.cloudinary.utils.Base64Coder;
import com.cloudinary.utils.ObjectUtils;
import com.cloudinary.utils.StringUtils;

public class Api extends ApiBase {

	public Api(CloudinaryBase cloudinary) {
		super(cloudinary);
		// TODO Auto-generated constructor stub
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override

	protected ApiResponse callApi(HttpMethod method, Iterable<String> uri, Map<String, ? extends Object> params, Map options) throws Exception {
		if (options == null)
			options = ObjectUtils.emptyMap();
		String prefix = ObjectUtils.asString(options.get("upload_prefix"), this.cloudinary.getStringConfig("upload_prefix", "https://api.cloudinary.com"));
		String cloudName = ObjectUtils.asString(options.get("cloud_name"), this.cloudinary.getStringConfig("cloud_name"));
		if (cloudName == null)
			throw new IllegalArgumentException("Must supply cloud_name");
		String apiKey = ObjectUtils.asString(options.get("api_key"), this.cloudinary.getStringConfig("api_key"));
		if (apiKey == null)
			throw new IllegalArgumentException("Must supply api_key");
		String apiSecret = ObjectUtils.asString(options.get("api_secret"), this.cloudinary.getStringConfig("api_secret"));
		if (apiSecret == null)
			throw new IllegalArgumentException("Must supply api_secret");

		String apiUrl = StringUtils.join(Arrays.asList(prefix, "v1_1", cloudName), "/");
		for (String component : uri) {
			apiUrl = apiUrl + "/" + component;
		}
		URIBuilder apiUrlBuilder = new URIBuilder(apiUrl);
		for (Map.Entry<String, ? extends Object> param : params.entrySet()) {
			if (param.getValue() instanceof Iterable) {
				for (String single : (Iterable<String>) param.getValue()) {
					apiUrlBuilder.addParameter(param.getKey() + "[]", single);
				}
			} else {
				apiUrlBuilder.addParameter(param.getKey(), ObjectUtils.asString(param.getValue()));
			}
		}
		DefaultHttpClient client = new DefaultHttpClient(connectionManager);
		URI apiUri = apiUrlBuilder.build();
		HttpUriRequest request = null;
		switch (method) {
		case GET:
			request = new HttpGet(apiUri);
			break;
		case PUT:
			request = new HttpPut(apiUri);
			break;
		case POST:
			request = new HttpPost(apiUri);
			break;
		case DELETE:
			request = new HttpDelete(apiUri);
			break;
		}
		request.setHeader("Authorization", "Basic " + Base64Coder.encodeString(apiKey + ":" + apiSecret));
		request.setHeader("User-Agent", Cloudinary.USER_AGENT);

		HttpResponse response = client.execute(request);

		int code = response.getStatusLine().getStatusCode();
		InputStream responseStream = response.getEntity().getContent();
		String responseData = StringUtils.read(responseStream);

		Class<? extends Exception> exceptionClass = CLOUDINARY_API_ERROR_CLASSES.get(code);
		if (code != 200 && exceptionClass == null) {
			throw new GeneralError("Server returned unexpected status code - " + code + " - " + responseData);
		}
		Map result;
		try {
			result = (Map) JSONValue.parseWithException(responseData);
		} catch (ParseException e) {
			throw new RuntimeException("Invalid JSON response from server " + e.getMessage());
		}

		if (code == 200) {
			return new Response(response, result);
		} else {
			String message = (String) ((Map) result.get("error")).get("message");
			Constructor<? extends Exception> exceptionConstructor = exceptionClass.getConstructor(String.class);
			throw exceptionConstructor.newInstance(message);
		}
	}

}
