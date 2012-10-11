package com.cloudinary;

import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class Api {
	enum HttpMethod { GET, POST, PUT, DELETE }
	
	public static class Response extends HashMap {
		public Response(HttpResponse response, Map result) {
			super(result);
		}
	}

	public static class ApiException extends Exception {
		public ApiException(String message) {
			super(message);
		}
	}

	public static class BadRequest extends ApiException {
		public BadRequest(String message) {
			super(message);
		}
	}

	public static class AuthorizationRequired extends ApiException {
		public AuthorizationRequired(String message) {
			super(message);
		}
	}

	public static class NotAllowed extends ApiException {
		public NotAllowed(String message) {
			super(message);
		}
	}

	public static class NotFound extends ApiException {
		public NotFound(String message) {
			super(message);
		}
	}

	public static class AlreadyExists extends ApiException {
		public AlreadyExists(String message) {
			super(message);
		}
	}

	public static class RateLimited extends ApiException {
		public RateLimited(String message) {
			super(message);
		}
	}

	public static class GeneralError extends ApiException {
		public GeneralError(String message) {
			super(message);
		}
	}

	public final static Map<Integer, Class<? extends Exception>> CLOUDINARY_API_ERROR_CLASSES = new HashMap<Integer, Class<? extends Exception>>();
	static {
		CLOUDINARY_API_ERROR_CLASSES.put(400, BadRequest.class);
		CLOUDINARY_API_ERROR_CLASSES.put(401, AuthorizationRequired.class);
		CLOUDINARY_API_ERROR_CLASSES.put(403, NotAllowed.class);
		CLOUDINARY_API_ERROR_CLASSES.put(404, NotFound.class);
		CLOUDINARY_API_ERROR_CLASSES.put(409, AlreadyExists.class);
		CLOUDINARY_API_ERROR_CLASSES.put(420, RateLimited.class);
		CLOUDINARY_API_ERROR_CLASSES.put(500, GeneralError.class);
	}

	private final Cloudinary cloudinary;

	public Api(Cloudinary cloudinary) {
		this.cloudinary = cloudinary;
	}

	public Map resource_types(Map options) throws Exception {
		return call_api(HttpMethod.GET, Arrays.asList("resources"), new HashMap(), options);
	}

	public Map resources(Map options) throws Exception {
		String resource_type = Cloudinary.as_string(options.get("resource_type"), "image");
		String type = Cloudinary.as_string(options.get("type"));
		List<String> uri = new ArrayList<String>();
		uri.add("resources");
		uri.add(resource_type);
		if (type != null)
			uri.add(type);
		return call_api(HttpMethod.GET, uri, only(options, "next_cursor", "max_results", "prefix"), options);
	}

	public Map resources_by_tag(String tag, Map options) throws Exception {
		String resource_type = Cloudinary.as_string(options.get("resource_type"), "image");
		return call_api(HttpMethod.GET, Arrays.asList("resources", resource_type, "tags", tag), only(options, "next_cursor", "max_results"), options);
	}

	public Map resource(String public_id, Map options) throws Exception {
		String resource_type = Cloudinary.as_string(options.get("resource_type"), "image");
		String type = Cloudinary.as_string(options.get("type"), "upload");
		return call_api(HttpMethod.GET, Arrays.asList("resources", resource_type, type, public_id),
				only(options, "exif", "colors", "faces", "max_results"), options);
	}

	public Map delete_resources(Iterable<String> public_ids, Map options) throws Exception {
		String resource_type = Cloudinary.as_string(options.get("resource_type"), "image");
		String type = Cloudinary.as_string(options.get("type"), "upload");
		Map params = new HashMap();
		params.put("public_ids", public_ids);
		return call_api(HttpMethod.DELETE, Arrays.asList("resources", resource_type, type), params, options);
	}

	public Map delete_resources_by_prefix(String prefix, Map options) throws Exception {
		String resource_type = Cloudinary.as_string(options.get("resource_type"), "image");
		String type = Cloudinary.as_string(options.get("type"), "upload");
		Map params = new HashMap();
		params.put("prefix", prefix);
		return call_api(HttpMethod.DELETE, Arrays.asList("resources", resource_type, type), params, options);
	}

	public Map delete_derived_resources(Iterable<String> derived_resource_ids, Map options) throws Exception {
		Map params = new HashMap();
		params.put("derived_resource_ids", derived_resource_ids);
		return call_api(HttpMethod.DELETE, Arrays.asList("derived_resources"), params, options);
	}

	public Map tags(Map options) throws Exception {
		String resource_type = Cloudinary.as_string(options.get("resource_type"), "image");
		return call_api(HttpMethod.GET, Arrays.asList("tags", resource_type), only(options, "next_cursor", "max_results", "prefix"), options);
	}

	public Map transformations(Map options) throws Exception {
		return call_api(HttpMethod.GET, Arrays.asList("transformations"), only(options, "next_cursor", "max_results"), options);
	}

	public Map transformation(String transformation, Map options) throws Exception {
		return call_api(HttpMethod.GET, Arrays.asList("transformations", transformation), only(options, "max_results"), options);
	}

	public Map delete_transformation(String transformation, Map options) throws Exception {
		return call_api(HttpMethod.DELETE, Arrays.asList("transformations", transformation), new HashMap(), options);
	}

	// updates - currently only supported update is the "allowed_for_strict"
	// boolean flag
	public Map update_transformation(String transformation, Map updates, Map options) throws Exception {
		return call_api(HttpMethod.PUT, Arrays.asList("transformations", transformation), updates, options);
	}

	public Map create_transformation(String name, String definition, Map options) throws Exception {
		Map params = new HashMap();
		params.put("transformation", definition);
		return call_api(HttpMethod.POST, Arrays.asList("transformations", name), params, options);
	}

	protected Map call_api(HttpMethod method, Iterable<String> uri, Map<String, ? extends Object> params, Map options) throws Exception {
		String prefix = Cloudinary.as_string(options.get("upload_prefix"),
				this.cloudinary.getStringConfig("upload_prefix", "https://api.cloudinary.com"));
		String cloud_name = Cloudinary.as_string(options.get("cloud_name"), this.cloudinary.getStringConfig("cloud_name"));
		if (cloud_name == null)
			throw new IllegalArgumentException("Must supply cloud_name");
		String api_key = Cloudinary.as_string(options.get("api_key"), this.cloudinary.getStringConfig("api_key"));
		if (api_key == null)
			throw new IllegalArgumentException("Must supply api_key");
		String api_secret = Cloudinary.as_string(options.get("api_secret"), this.cloudinary.getStringConfig("api_secret"));
		if (api_secret == null)
			throw new IllegalArgumentException("Must supply api_secret");

		String api_url = StringUtils.join(Arrays.asList(prefix, "v1_1", cloud_name), "/");
		for (String component : uri) {
			api_url = api_url + "/" + component;
		}
		URIBuilder api_url_builder = new URIBuilder(api_url);
		for (Map.Entry<String, ? extends Object> param : params.entrySet()) {
			if (param.getValue() instanceof Iterable) {
				for (String single : (Iterable<String>) param.getValue()) {
					api_url_builder.addParameter(param.getKey() + "[]", single);
				}
			} else {
				api_url_builder.addParameter(param.getKey(), Cloudinary.as_string(param.getValue()));
			}  
		}
		DefaultHttpClient client = new DefaultHttpClient();
		URI api_uri = api_url_builder.build();
		HttpUriRequest request = null;
		switch (method) {
		case GET: request = new HttpGet(api_uri); break;
		case PUT: request = new HttpPut(api_uri); break;
		case POST: request = new HttpPost(api_uri); break;
		case DELETE: request = new HttpDelete(api_uri); break;
		}
		request.setHeader("Authorization", "Basic " + Base64.encodeBase64String((api_key + ":" + api_secret).getBytes()));
		HttpResponse response = client.execute(request);

		int code = response.getStatusLine().getStatusCode();
		InputStream responseStream = response.getEntity().getContent();
		String responseData = Uploader.readFully(responseStream);

		Class<? extends Exception> exception_class = CLOUDINARY_API_ERROR_CLASSES.get(code);
		if (code != 200 && exception_class == null) {
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
			Constructor<? extends Exception> exceptionConstructor = exception_class.getConstructor(String.class);
			throw exceptionConstructor.newInstance(message);
		}
	}

	protected Map<String, ? extends Object> only(Map<String, ? extends Object> hash, String... keys) {
		Map<String, Object> result = new HashMap<String, Object>();
		for (String key : keys) {
			if (hash.containsKey(key)) {
				result.put(key, hash.get(key));
			}
		}
		return result;
	}
}
