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

    public Map ping(Map options) throws Exception {
        if (options == null) options = Cloudinary.emptyMap();
        return callApi(HttpMethod.GET, Arrays.asList("ping"), Cloudinary.emptyMap(), options);
    }

    public Map usage(Map options) throws Exception {
        if (options == null) options = Cloudinary.emptyMap();
        return callApi(HttpMethod.GET, Arrays.asList("usage"), Cloudinary.emptyMap(), options);
    }

    public Map resourceTypes(Map options) throws Exception {
        if (options == null) options = Cloudinary.emptyMap();
        return callApi(HttpMethod.GET, Arrays.asList("resources"), Cloudinary.emptyMap(), options);
    }

    public Map resources(Map options) throws Exception {
        if (options == null) options = Cloudinary.emptyMap();
        String resourceType = Cloudinary.asString(options.get("resource_type"), "image");
        String type = Cloudinary.asString(options.get("type"));
        List<String> uri = new ArrayList<String>();
        uri.add("resources");
        uri.add(resourceType);
        if (type != null)
            uri.add(type);
        return callApi(HttpMethod.GET, uri, only(options, "next_cursor", "max_results", "prefix", "tags", "context"), options);
    }

    public Map resourcesByTag(String tag, Map options) throws Exception {
        if (options == null) options = Cloudinary.emptyMap();
        String resourceType = Cloudinary.asString(options.get("resource_type"), "image");
        return callApi(HttpMethod.GET, Arrays.asList("resources", resourceType, "tags", tag), only(options, "next_cursor", "max_results", "tags", "context"), options);
    }
    
    public Map resourcesByIds(Iterable<String> publicIds, Map options) throws Exception {
        if (options == null) options = Cloudinary.emptyMap();
        String resourceType = Cloudinary.asString(options.get("resource_type"), "image");
        String type = Cloudinary.asString(options.get("type"), "upload");
        Map params = only(options, "tags", "context");
        params.put("public_ids", publicIds);
        return callApi(HttpMethod.GET, Arrays.asList("resources", resourceType, type), params, options);
    }

    public Map resource(String public_id, Map options) throws Exception {
        if (options == null) options = Cloudinary.emptyMap();
        String resourceType = Cloudinary.asString(options.get("resource_type"), "image");
        String type = Cloudinary.asString(options.get("type"), "upload");
        return callApi(HttpMethod.GET, Arrays.asList("resources", resourceType, type, public_id),
                only(options, "exif", "colors", "faces", "image_metadata", "pages", "max_results"), options);
    }

    public Map deleteResources(Iterable<String> publicIds, Map options) throws Exception {
        if (options == null) options = Cloudinary.emptyMap();
        String resourceType = Cloudinary.asString(options.get("resource_type"), "image");
        String type = Cloudinary.asString(options.get("type"), "upload");
        Map params = only(options, "keep_original", "next_cursor");
        params.put("public_ids", publicIds);
        return callApi(HttpMethod.DELETE, Arrays.asList("resources", resourceType, type), params, options);
    }

    public Map deleteResourcesByPrefix(String prefix, Map options) throws Exception {
        if (options == null) options = Cloudinary.emptyMap();
        String resourceType = Cloudinary.asString(options.get("resource_type"), "image");
        String type = Cloudinary.asString(options.get("type"), "upload");
        Map params = only(options, "keep_original", "next_cursor");
        params.put("prefix", prefix);
        return callApi(HttpMethod.DELETE, Arrays.asList("resources", resourceType, type), params, options);
    }

    public Map deleteResourcesByTag(String tag, Map options) throws Exception {
        if (options == null) options = Cloudinary.emptyMap();
        String resourceType = Cloudinary.asString(options.get("resource_type"), "image");
        return callApi(HttpMethod.DELETE, Arrays.asList("resources", resourceType, "tags", tag), only(options, "keep_original", "next_cursor"), options);
    }
    
    public Map deleteAllResources(Map options) throws Exception {
        if (options == null) options = Cloudinary.emptyMap();
        String resourceType = Cloudinary.asString(options.get("resource_type"), "image");
        String type = Cloudinary.asString(options.get("type"), "upload");
        Map filtered = only(options, "keep_original", "next_cursor");
        filtered.put("all", true);
        return callApi(HttpMethod.DELETE, Arrays.asList("resources", resourceType, type), filtered, options);
    }

    public Map deleteDerivedResources(Iterable<String> derivedResourceIds, Map options) throws Exception {
        if (options == null) options = Cloudinary.emptyMap();
        return callApi(HttpMethod.DELETE, Arrays.asList("derived_resources"), Cloudinary.asMap("derived_resource_ids", derivedResourceIds), options);
    }

    public Map tags(Map options) throws Exception {
        if (options == null) options = Cloudinary.emptyMap();
        String resourceType = Cloudinary.asString(options.get("resource_type"), "image");
        return callApi(HttpMethod.GET, Arrays.asList("tags", resourceType), only(options, "next_cursor", "max_results", "prefix"), options);
    }

    public Map transformations(Map options) throws Exception {
        if (options == null) options = Cloudinary.emptyMap();
        return callApi(HttpMethod.GET, Arrays.asList("transformations"), only(options, "next_cursor", "max_results"), options);
    }

    public Map transformation(String transformation, Map options) throws Exception {
        if (options == null) options = Cloudinary.emptyMap();
        return callApi(HttpMethod.GET, Arrays.asList("transformations", transformation), only(options, "max_results"), options);
    }

    public Map deleteTransformation(String transformation, Map options) throws Exception {
        if (options == null) options = Cloudinary.emptyMap();
        return callApi(HttpMethod.DELETE, Arrays.asList("transformations", transformation), Cloudinary.emptyMap(), options);
    }

    // updates - currently only supported update are:
    // "allowed_for_strict": boolean flag
    // "unsafe_update": transformation string
    public Map updateTransformation(String transformation, Map updates, Map options) throws Exception {
        if (options == null) options = Cloudinary.emptyMap();
        return callApi(HttpMethod.PUT, Arrays.asList("transformations", transformation), updates, options);
    }

    public Map createTransformation(String name, String definition, Map options) throws Exception {
        return callApi(HttpMethod.POST, Arrays.asList("transformations", name), Cloudinary.asMap("transformation", definition), options);
    }

    protected Map callApi(HttpMethod method, Iterable<String> uri, Map<String, ? extends Object> params, Map options) throws Exception {
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
        DefaultHttpClient client = new DefaultHttpClient();
        URI apiUri = apiUrlBuilder.build();
        HttpUriRequest request = null;
        switch (method) {
        case GET: request = new HttpGet(apiUri); break;
        case PUT: request = new HttpPut(apiUri); break;
        case POST: request = new HttpPost(apiUri); break;
        case DELETE: request = new HttpDelete(apiUri); break;
        }
        request.setHeader("Authorization", "Basic " + Base64.encodeBase64String((apiKey + ":" + apiSecret).getBytes()));
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
