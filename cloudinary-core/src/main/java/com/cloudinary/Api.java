package com.cloudinary;

import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.Header;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class Api {
    enum HttpMethod { GET, POST, PUT, DELETE }
    
    public static class RateLimit {
    	private long limit = 0L;
		private long remaining = 0L;
    	private Date reset = null;
    	public RateLimit(){
    		super();
    	}
    	
    	public long getLimit() {
			return limit;
		}
		public void setLimit(long limit) {
			this.limit = limit;
		}
		public long getRemaining() {
			return remaining;
		}
		public void setRemaining(long remaining) {
			this.remaining = remaining;
		}
		public Date getReset() {
			return reset;
		}
		public void setReset(Date reset) {
			this.reset = reset;
		}
    }
    
    public static interface ApiResponse extends Map {
    	HttpResponse getRawHttpResponse();
    	Map<String, RateLimit> rateLimits() throws java.text.ParseException;
    	RateLimit apiRateLimit() throws java.text.ParseException;
    }
    
    public static class Response extends HashMap implements ApiResponse {
		private static final long serialVersionUID = -5458609797599845837L;
		private HttpResponse response = null;
        public Response(HttpResponse response, Map result) {
            super(result);
            this.response = response;
        }
        
        public HttpResponse getRawHttpResponse(){
        	return this.response;
        }
        
        private static final Pattern RATE_LIMIT_REGEX = Pattern.compile("X-Feature(\\w*)RateLimit(-Limit|-Reset|-Remaining)");
        private static final String RFC1123_PATTERN = "EEE, dd MMM yyyyy HH:mm:ss z";
        private static final DateFormat RFC1123 = new SimpleDateFormat(RFC1123_PATTERN);
        public Map<String, RateLimit> rateLimits() throws java.text.ParseException {
        	Header[] headers = this.response.getAllHeaders();
        	Map<String, RateLimit> limits = new HashMap<String, RateLimit>();
        	for (Header header : headers){
        		Matcher m = RATE_LIMIT_REGEX.matcher(header.getName());
        		if (m.matches()){
        			String limitName = "Api";
        			RateLimit limit = null;
        			if (!m.group(1).isEmpty()) {
        				limitName = m.group(1);
        			}
        			limit = limits.get(limitName);
        			if (limit == null) {
        				limit = new RateLimit();
        			}
        			if (m.group(2).equalsIgnoreCase("-limit")) {
        				limit.setLimit(Long.parseLong(header.getValue()));
        			} else if (m.group(2).equalsIgnoreCase("-remaining")) {
        				limit.setRemaining(Long.parseLong(header.getValue()));
        			} else if (m.group(2).equalsIgnoreCase("-reset")) {
        				limit.setReset(RFC1123.parse(header.getValue()));
        			} 
        			limits.put(limitName, limit);
        		}
        	}
        	return limits;
        }
        
        public RateLimit apiRateLimit() throws java.text.ParseException {
        	return rateLimits().get("Api");
        }
    }

    public static class ApiException extends Exception {
		private static final long serialVersionUID = 4416861825144420038L;
		public ApiException(String message) {
            super(message);
        }
    }

    public static class BadRequest extends ApiException {
		private static final long serialVersionUID = 1410136354253339531L;
		public BadRequest(String message) {
            super(message);
        }
    }

    public static class AuthorizationRequired extends ApiException {
		private static final long serialVersionUID = 7160740370855761014L;
		public AuthorizationRequired(String message) {
            super(message);
        }
    }

    public static class NotAllowed extends ApiException {
		private static final long serialVersionUID = 4371365822491647653L;
		public NotAllowed(String message) {
            super(message);
        }
    }

    public static class NotFound extends ApiException {
		private static final long serialVersionUID = -2072640462778940357L;
		public NotFound(String message) {
            super(message);
        }
    }

    public static class AlreadyExists extends ApiException {
		private static final long serialVersionUID = 999568182896607322L;
		public AlreadyExists(String message) {
            super(message);
        }
    }

    public static class RateLimited extends ApiException {
		private static final long serialVersionUID = -8298038106172355219L;
		public RateLimited(String message) {
            super(message);
        }
    }

    public static class GeneralError extends ApiException {
		private static final long serialVersionUID = 4553362706625067182L;
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

    public ApiResponse ping(Map options) throws Exception {
        if (options == null) options = Cloudinary.emptyMap();
        return callApi(HttpMethod.GET, Arrays.asList("ping"), Cloudinary.emptyMap(), options);
    }

    public ApiResponse usage(Map options) throws Exception {
        if (options == null) options = Cloudinary.emptyMap();
        return callApi(HttpMethod.GET, Arrays.asList("usage"), Cloudinary.emptyMap(), options);
    }

    public ApiResponse resourceTypes(Map options) throws Exception {
        if (options == null) options = Cloudinary.emptyMap();
        return callApi(HttpMethod.GET, Arrays.asList("resources"), Cloudinary.emptyMap(), options);
    }

    public ApiResponse resources(Map options) throws Exception {
        if (options == null) options = Cloudinary.emptyMap();
        String resourceType = Cloudinary.asString(options.get("resource_type"), "image");
        String type = Cloudinary.asString(options.get("type"));
        List<String> uri = new ArrayList<String>();
        uri.add("resources");
        uri.add(resourceType);
        if (type != null)
            uri.add(type);
        return callApi(HttpMethod.GET, uri, Cloudinary.only(options, "next_cursor", "direction", "max_results", "prefix", "tags", "context", "moderations", "start_at"), options);
    }
    
    public ApiResponse resourcesByTag(String tag, Map options) throws Exception {
        if (options == null) options = Cloudinary.emptyMap();
        String resourceType = Cloudinary.asString(options.get("resource_type"), "image");
        return callApi(HttpMethod.GET, Arrays.asList("resources", resourceType, "tags", tag), Cloudinary.only(options, "next_cursor", "direction", "max_results", "tags", "context", "moderations"), options);
    }
    
    public ApiResponse resourcesByIds(Iterable<String> publicIds, Map options) throws Exception {
        if (options == null) options = Cloudinary.emptyMap();
        String resourceType = Cloudinary.asString(options.get("resource_type"), "image");
        String type = Cloudinary.asString(options.get("type"), "upload");
        Map params = Cloudinary.only(options, "tags", "context", "moderations");
        params.put("public_ids", publicIds);
        return callApi(HttpMethod.GET, Arrays.asList("resources", resourceType, type), params, options);
    }
    
    public ApiResponse resourcesByModeration(String kind, String status, Map options) throws Exception {
        if (options == null) options = Cloudinary.emptyMap();
        String resourceType = Cloudinary.asString(options.get("resource_type"), "image");
        return callApi(HttpMethod.GET, Arrays.asList("resources", resourceType, "moderations", kind, status), Cloudinary.only(options, "next_cursor", "direction", "max_results", "tags", "context", "moderations"), options);
    }

    public ApiResponse resource(String public_id, Map options) throws Exception {
        if (options == null) options = Cloudinary.emptyMap();
        String resourceType = Cloudinary.asString(options.get("resource_type"), "image");
        String type = Cloudinary.asString(options.get("type"), "upload");
        return callApi(HttpMethod.GET, Arrays.asList("resources", resourceType, type, public_id),
                Cloudinary.only(options, "exif", "colors", "faces", "coordinates", 
                						 "image_metadata", "pages", "phash", "max_results"), options);
    }
    
    public ApiResponse update(String public_id, Map options) throws Exception {
        if (options == null) options = Cloudinary.emptyMap();
        String resourceType = Cloudinary.asString(options.get("resource_type"), "image");
        String type = Cloudinary.asString(options.get("type"), "upload");
        Map params = new HashMap<String, Object>();
        Util.processWriteParameters(options, params);
        params.put("moderation_status", options.get("moderation_status"));
        return callApi(HttpMethod.POST, Arrays.asList("resources", resourceType, type, public_id), 
        		params, options);
    }

    public ApiResponse deleteResources(Iterable<String> publicIds, Map options) throws Exception {
        if (options == null) options = Cloudinary.emptyMap();
        String resourceType = Cloudinary.asString(options.get("resource_type"), "image");
        String type = Cloudinary.asString(options.get("type"), "upload");
        Map params = Cloudinary.only(options, "keep_original", "next_cursor");
        params.put("public_ids", publicIds);
        return callApi(HttpMethod.DELETE, Arrays.asList("resources", resourceType, type), params, options);
    }

    public ApiResponse deleteResourcesByPrefix(String prefix, Map options) throws Exception {
        if (options == null) options = Cloudinary.emptyMap();
        String resourceType = Cloudinary.asString(options.get("resource_type"), "image");
        String type = Cloudinary.asString(options.get("type"), "upload");
        Map params = Cloudinary.only(options, "keep_original", "next_cursor");
        params.put("prefix", prefix);
        return callApi(HttpMethod.DELETE, Arrays.asList("resources", resourceType, type), params, options);
    }

    public ApiResponse deleteResourcesByTag(String tag, Map options) throws Exception {
        if (options == null) options = Cloudinary.emptyMap();
        String resourceType = Cloudinary.asString(options.get("resource_type"), "image");
        return callApi(HttpMethod.DELETE, Arrays.asList("resources", resourceType, "tags", tag), Cloudinary.only(options, "keep_original", "next_cursor"), options);
    }
    
    public ApiResponse deleteAllResources(Map options) throws Exception {
        if (options == null) options = Cloudinary.emptyMap();
        String resourceType = Cloudinary.asString(options.get("resource_type"), "image");
        String type = Cloudinary.asString(options.get("type"), "upload");
        Map filtered = Cloudinary.only(options, "keep_original", "next_cursor");
        filtered.put("all", true);
        return callApi(HttpMethod.DELETE, Arrays.asList("resources", resourceType, type), filtered, options);
    }

    public ApiResponse deleteDerivedResources(Iterable<String> derivedResourceIds, Map options) throws Exception {
        if (options == null) options = Cloudinary.emptyMap();
        return callApi(HttpMethod.DELETE, Arrays.asList("derived_resources"), Cloudinary.asMap("derived_resource_ids", derivedResourceIds), options);
    }

    public ApiResponse tags(Map options) throws Exception {
        if (options == null) options = Cloudinary.emptyMap();
        String resourceType = Cloudinary.asString(options.get("resource_type"), "image");
        return callApi(HttpMethod.GET, Arrays.asList("tags", resourceType), Cloudinary.only(options, "next_cursor", "max_results", "prefix"), options);
    }

    public ApiResponse transformations(Map options) throws Exception {
        if (options == null) options = Cloudinary.emptyMap();
        return callApi(HttpMethod.GET, Arrays.asList("transformations"), Cloudinary.only(options, "next_cursor", "max_results"), options);
    }

    public ApiResponse transformation(String transformation, Map options) throws Exception {
        if (options == null) options = Cloudinary.emptyMap();
        return callApi(HttpMethod.GET, Arrays.asList("transformations", transformation), Cloudinary.only(options, "max_results"), options);
    }

    public ApiResponse deleteTransformation(String transformation, Map options) throws Exception {
        if (options == null) options = Cloudinary.emptyMap();
        return callApi(HttpMethod.DELETE, Arrays.asList("transformations", transformation), Cloudinary.emptyMap(), options);
    }

    // updates - currently only supported update are:
    // "allowed_for_strict": boolean flag
    // "unsafe_update": transformation string
    public ApiResponse updateTransformation(String transformation, Map updates, Map options) throws Exception {
        if (options == null) options = Cloudinary.emptyMap();
        return callApi(HttpMethod.PUT, Arrays.asList("transformations", transformation), updates, options);
    }

    public ApiResponse createTransformation(String name, String definition, Map options) throws Exception {
        return callApi(HttpMethod.POST, Arrays.asList("transformations", name), Cloudinary.asMap("transformation", definition), options);
    }
    
    public ApiResponse uploadPresets(Map options) throws Exception {
        if (options == null) options = Cloudinary.emptyMap();
        return callApi(HttpMethod.GET, Arrays.asList("upload_presets"), Cloudinary.only(options, "next_cursor", "max_results"), options);
    }

    public ApiResponse uploadPreset(String name, Map options) throws Exception {
        if (options == null) options = Cloudinary.emptyMap();
        return callApi(HttpMethod.GET, Arrays.asList("upload_presets", name), Cloudinary.only(options, "max_results"), options);
    }

    public ApiResponse deleteUploadPreset(String name, Map options) throws Exception {
        if (options == null) options = Cloudinary.emptyMap();
        return callApi(HttpMethod.DELETE, Arrays.asList("upload_presets", name), Cloudinary.emptyMap(), options);
    }

    public ApiResponse updateUploadPreset(String name, Map options) throws Exception {
        if (options == null) options = Cloudinary.emptyMap();
        Map params = Util.buildUploadParams(options);
        Util.clearEmpty(params);
        params.putAll(Cloudinary.only(options, "unsigned", "disallow_public_id"));
        return callApi(HttpMethod.PUT, Arrays.asList("upload_presets", name), params, options);
    }

    public ApiResponse createUploadPreset(Map options) throws Exception {
    	if (options == null) options = Cloudinary.emptyMap();
        Map params = Util.buildUploadParams(options);
        Util.clearEmpty(params);
        params.putAll(Cloudinary.only(options, "name", "unsigned", "disallow_public_id"));
        return callApi(HttpMethod.POST,  Arrays.asList("upload_presets"), params, options);
    }
    
    public Api withConnectionManager(ClientConnectionManager connectionManager) {
		this.connectionManager = connectionManager;
		return this;
	}

    protected ApiResponse callApi(HttpMethod method, Iterable<String> uri, Map<String, ? extends Object> params, Map options) throws Exception {
        if (options == null) options = Cloudinary.emptyMap();
        String prefix = Cloudinary.asString(options.get("upload_prefix"),
                this.cloudinary.getStringConfig("upload_prefix", "https://api.cloudinary.com"));
        String cloudName = Cloudinary.asString(options.get("cloud_name"), this.cloudinary.getStringConfig("cloud_name"));
        if (cloudName == null)
            throw new IllegalArgumentException("Must supply cloud_name");
        String apiKey = Cloudinary.asString(options.get("api_key"), this.cloudinary.getStringConfig("api_key"));
        if (apiKey == null)
            throw new IllegalArgumentException("Must supply api_key");
        String apiSecret = Cloudinary.asString(options.get("api_secret"), this.cloudinary.getStringConfig("api_secret"));
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
                apiUrlBuilder.addParameter(param.getKey(), Cloudinary.asString(param.getValue()));
            }  
        }
        DefaultHttpClient client = new DefaultHttpClient(connectionManager);
        URI apiUri = apiUrlBuilder.build();
        HttpUriRequest request = null;
        switch (method) {
        case GET: request = new HttpGet(apiUri); break;
        case PUT: request = new HttpPut(apiUri); break;
        case POST: request = new HttpPost(apiUri); break;
        case DELETE: request = new HttpDelete(apiUri); break;
        }
        request.setHeader("Authorization", "Basic " + Base64.encodeBase64String((apiKey + ":" + apiSecret).getBytes()));
        request.setHeader("User-Agent", Cloudinary.USER_AGENT);
        
        HttpResponse response = client.execute(request);

        int code = response.getStatusLine().getStatusCode();
        InputStream responseStream = response.getEntity().getContent();
        String responseData = Uploader.readFully(responseStream);

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
    
    private ClientConnectionManager connectionManager = null;
}
