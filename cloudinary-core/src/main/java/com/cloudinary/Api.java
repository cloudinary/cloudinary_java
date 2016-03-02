package com.cloudinary;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cloudinary.api.ApiResponse;
import com.cloudinary.api.AuthorizationRequired;
import com.cloudinary.api.exceptions.AlreadyExists;
import com.cloudinary.api.exceptions.BadRequest;
import com.cloudinary.api.exceptions.GeneralError;
import com.cloudinary.api.exceptions.NotAllowed;
import com.cloudinary.api.exceptions.NotFound;
import com.cloudinary.api.exceptions.RateLimited;
import com.cloudinary.strategies.AbstractApiStrategy;
import com.cloudinary.utils.ObjectUtils;

@SuppressWarnings({"rawtypes", "unchecked"})
public class Api {
    public enum HttpMethod {GET, POST, PUT, DELETE}

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

    public final Cloudinary cloudinary;
    private AbstractApiStrategy strategy;

    protected ApiResponse callApi(HttpMethod method, Iterable<String> uri, Map<String, ? extends Object> params, Map options) throws Exception {
        return this.strategy.callApi(method, uri, params, options);
    }

    public Api(Cloudinary cloudinary, AbstractApiStrategy strategy) {
        this.cloudinary = cloudinary;
        this.strategy = strategy;
        this.strategy.init(this);
    }

    public ApiResponse ping(Map options) throws Exception {
        if (options == null) options = ObjectUtils.emptyMap();
        return callApi(HttpMethod.GET, Arrays.asList("ping"), ObjectUtils.emptyMap(), options);
    }

    public ApiResponse usage(Map options) throws Exception {
        if (options == null) options = ObjectUtils.emptyMap();
        return callApi(HttpMethod.GET, Arrays.asList("usage"), ObjectUtils.emptyMap(), options);
    }

    public ApiResponse resourceTypes(Map options) throws Exception {
        if (options == null) options = ObjectUtils.emptyMap();
        return callApi(HttpMethod.GET, Arrays.asList("resources"), ObjectUtils.emptyMap(), options);
    }

    public ApiResponse resources(Map options) throws Exception {
        if (options == null) options = ObjectUtils.emptyMap();
        String resourceType = ObjectUtils.asString(options.get("resource_type"), "image");
        String type = ObjectUtils.asString(options.get("type"));
        List<String> uri = new ArrayList<String>();
        uri.add("resources");
        uri.add(resourceType);
        if (type != null)
            uri.add(type);
        return callApi(HttpMethod.GET, uri, ObjectUtils.only(options, "next_cursor", "direction", "max_results", "prefix", "tags", "context", "moderations", "start_at"), options);
    }

    public ApiResponse resourcesByTag(String tag, Map options) throws Exception {
        if (options == null) options = ObjectUtils.emptyMap();
        String resourceType = ObjectUtils.asString(options.get("resource_type"), "image");
        return callApi(HttpMethod.GET, Arrays.asList("resources", resourceType, "tags", tag), ObjectUtils.only(options, "next_cursor", "direction", "max_results", "tags", "context", "moderations"), options);
    }

    public ApiResponse resourcesByIds(Iterable<String> publicIds, Map options) throws Exception {
        if (options == null) options = ObjectUtils.emptyMap();
        String resourceType = ObjectUtils.asString(options.get("resource_type"), "image");
        String type = ObjectUtils.asString(options.get("type"), "upload");
        Map params = ObjectUtils.only(options, "tags", "context", "moderations");
        params.put("public_ids", publicIds);
        return callApi(HttpMethod.GET, Arrays.asList("resources", resourceType, type), params, options);
    }

    public ApiResponse resourcesByModeration(String kind, String status, Map options) throws Exception {
        if (options == null) options = ObjectUtils.emptyMap();
        String resourceType = ObjectUtils.asString(options.get("resource_type"), "image");
        return callApi(HttpMethod.GET, Arrays.asList("resources", resourceType, "moderations", kind, status), ObjectUtils.only(options, "next_cursor", "direction", "max_results", "tags", "context", "moderations"), options);
    }

    public ApiResponse resource(String public_id, Map options) throws Exception {
        if (options == null) options = ObjectUtils.emptyMap();
        String resourceType = ObjectUtils.asString(options.get("resource_type"), "image");
        String type = ObjectUtils.asString(options.get("type"), "upload");
        return callApi(HttpMethod.GET, Arrays.asList("resources", resourceType, type, public_id),
                ObjectUtils.only(options, "exif", "colors", "faces", "coordinates",
                        "image_metadata", "pages", "phash", "max_results"), options);
    }

    public ApiResponse update(String public_id, Map options) throws Exception {
        if (options == null) options = ObjectUtils.emptyMap();
        String resourceType = ObjectUtils.asString(options.get("resource_type"), "image");
        String type = ObjectUtils.asString(options.get("type"), "upload");
        Map params = new HashMap<String, Object>();
        Util.processWriteParameters(options, params);
        params.put("moderation_status", options.get("moderation_status"));
        return callApi(HttpMethod.POST, Arrays.asList("resources", resourceType, type, public_id),
                params, options);
    }

    public ApiResponse deleteResources(Iterable<String> publicIds, Map options) throws Exception {
        if (options == null) options = ObjectUtils.emptyMap();
        String resourceType = ObjectUtils.asString(options.get("resource_type"), "image");
        String type = ObjectUtils.asString(options.get("type"), "upload");
        Map params = ObjectUtils.only(options, "keep_original", "invalidate", "next_cursor");
        params.put("public_ids", publicIds);
        return callApi(HttpMethod.DELETE, Arrays.asList("resources", resourceType, type), params, options);
    }

    public ApiResponse deleteResourcesByPrefix(String prefix, Map options) throws Exception {
        if (options == null) options = ObjectUtils.emptyMap();
        String resourceType = ObjectUtils.asString(options.get("resource_type"), "image");
        String type = ObjectUtils.asString(options.get("type"), "upload");
        Map params = ObjectUtils.only(options, "keep_original", "invalidate", "next_cursor");
        params.put("prefix", prefix);
        return callApi(HttpMethod.DELETE, Arrays.asList("resources", resourceType, type), params, options);
    }

    public ApiResponse deleteResourcesByTag(String tag, Map options) throws Exception {
        if (options == null) options = ObjectUtils.emptyMap();
        String resourceType = ObjectUtils.asString(options.get("resource_type"), "image");
        return callApi(HttpMethod.DELETE, Arrays.asList("resources", resourceType, "tags", tag), ObjectUtils.only(options, "keep_original", "invalidate", "next_cursor"), options);
    }

    public ApiResponse deleteAllResources(Map options) throws Exception {
        if (options == null) options = ObjectUtils.emptyMap();
        String resourceType = ObjectUtils.asString(options.get("resource_type"), "image");
        String type = ObjectUtils.asString(options.get("type"), "upload");
        Map filtered = ObjectUtils.only(options, "keep_original", "invalidate", "next_cursor");
        filtered.put("all", true);
        return callApi(HttpMethod.DELETE, Arrays.asList("resources", resourceType, type), filtered, options);
    }

    public ApiResponse deleteDerivedResources(Iterable<String> derivedResourceIds, Map options) throws Exception {
        if (options == null) options = ObjectUtils.emptyMap();
        return callApi(HttpMethod.DELETE, Arrays.asList("derived_resources"), ObjectUtils.asMap("derived_resource_ids", derivedResourceIds), options);
    }

    public ApiResponse tags(Map options) throws Exception {
        if (options == null) options = ObjectUtils.emptyMap();
        String resourceType = ObjectUtils.asString(options.get("resource_type"), "image");
        return callApi(HttpMethod.GET, Arrays.asList("tags", resourceType), ObjectUtils.only(options, "next_cursor", "max_results", "prefix"), options);
    }

    public ApiResponse transformations(Map options) throws Exception {
        if (options == null) options = ObjectUtils.emptyMap();
        return callApi(HttpMethod.GET, Arrays.asList("transformations"), ObjectUtils.only(options, "next_cursor", "max_results"), options);
    }

    public ApiResponse transformation(String transformation, Map options) throws Exception {
        if (options == null) options = ObjectUtils.emptyMap();
        return callApi(HttpMethod.GET, Arrays.asList("transformations", transformation), ObjectUtils.only(options, "max_results"), options);
    }

    public ApiResponse deleteTransformation(String transformation, Map options) throws Exception {
        if (options == null) options = ObjectUtils.emptyMap();
        return callApi(HttpMethod.DELETE, Arrays.asList("transformations", transformation), ObjectUtils.emptyMap(), options);
    }

    // updates - currently only supported update are:
    // "allowed_for_strict": boolean flag
    // "unsafe_update": transformation string
    public ApiResponse updateTransformation(String transformation, Map updates, Map options) throws Exception {
        if (options == null) options = ObjectUtils.emptyMap();
        return callApi(HttpMethod.PUT, Arrays.asList("transformations", transformation), updates, options);
    }

    public ApiResponse createTransformation(String name, String definition, Map options) throws Exception {
        return callApi(HttpMethod.POST, Arrays.asList("transformations", name), ObjectUtils.asMap("transformation", definition), options);
    }

    public ApiResponse uploadPresets(Map options) throws Exception {
        if (options == null) options = ObjectUtils.emptyMap();
        return callApi(HttpMethod.GET, Arrays.asList("upload_presets"), ObjectUtils.only(options, "next_cursor", "max_results"), options);
    }

    public ApiResponse uploadPreset(String name, Map options) throws Exception {
        if (options == null) options = ObjectUtils.emptyMap();
        return callApi(HttpMethod.GET, Arrays.asList("upload_presets", name), ObjectUtils.only(options, "max_results"), options);
    }

    public ApiResponse deleteUploadPreset(String name, Map options) throws Exception {
        if (options == null) options = ObjectUtils.emptyMap();
        return callApi(HttpMethod.DELETE, Arrays.asList("upload_presets", name), ObjectUtils.emptyMap(), options);
    }

    public ApiResponse updateUploadPreset(String name, Map options) throws Exception {
        if (options == null) options = ObjectUtils.emptyMap();
        Map params = Util.buildUploadParams(options);
        Util.clearEmpty(params);
        params.putAll(ObjectUtils.only(options, "unsigned", "disallow_public_id"));
        return callApi(HttpMethod.PUT, Arrays.asList("upload_presets", name), params, options);
    }

    public ApiResponse createUploadPreset(Map options) throws Exception {
        if (options == null) options = ObjectUtils.emptyMap();
        Map params = Util.buildUploadParams(options);
        Util.clearEmpty(params);
        params.putAll(ObjectUtils.only(options, "name", "unsigned", "disallow_public_id"));
        return callApi(HttpMethod.POST, Arrays.asList("upload_presets"), params, options);
    }

    public ApiResponse rootFolders(Map options) throws Exception {
        if (options == null)
            options = ObjectUtils.emptyMap();
        return callApi(HttpMethod.GET, Arrays.asList("folders"), ObjectUtils.emptyMap(), options);
    }

    public ApiResponse subFolders(String ofFolderPath, Map options) throws Exception {
        if (options == null)
            options = ObjectUtils.emptyMap();
        return callApi(HttpMethod.GET, Arrays.asList("folders", ofFolderPath), ObjectUtils.emptyMap(), options);
    }

    public ApiResponse restore(Iterable<String> publicIds, Map options) throws Exception {
        if (options == null)
            options = ObjectUtils.emptyMap();
        String resourceType = ObjectUtils.asString(options.get("resource_type"), "image");
        String type = ObjectUtils.asString(options.get("type"), "upload");
        Map params = new HashMap<String, Object>();
        params.put("public_ids", publicIds);
        return callApi(HttpMethod.POST, Arrays.asList("resources", resourceType, type, "restore"), params, options);
    }

    public ApiResponse uploadMappings(Map options) throws Exception {
        if (options == null)
            options = ObjectUtils.emptyMap();
        return callApi(HttpMethod.GET, Arrays.asList("upload_mappings"),
                ObjectUtils.only(options, "next_cursor", "max_results"), options);
    }

    public ApiResponse uploadMapping(String name, Map options) throws Exception {
        if (options == null)
            options = ObjectUtils.emptyMap();
        return callApi(HttpMethod.GET, Arrays.asList("upload_mappings"), ObjectUtils.asMap("folder", name), options);
    }

    public ApiResponse deleteUploadMapping(String name, Map options) throws Exception {
        if (options == null)
            options = ObjectUtils.emptyMap();
        return callApi(HttpMethod.DELETE, Arrays.asList("upload_mappings"), ObjectUtils.asMap("folder", name), options);
    }

    public ApiResponse updateUploadMapping(String name, Map options) throws Exception {
        if (options == null)
            options = ObjectUtils.emptyMap();
        Map params = new HashMap<String, Object>();
        params.put("folder", name);
        params.putAll(ObjectUtils.only(options, "template"));
        return callApi(HttpMethod.PUT, Arrays.asList("upload_mappings"), params, options);
    }

    public ApiResponse createUploadMapping(String name, Map options) throws Exception {
        if (options == null)
            options = ObjectUtils.emptyMap();
        Map params = new HashMap<String, Object>();
        params.put("folder", name);
        params.putAll(ObjectUtils.only(options, "template"));
        return callApi(HttpMethod.POST, Arrays.asList("upload_mappings"), params, options);
    }

}
