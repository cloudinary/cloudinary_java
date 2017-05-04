package com.cloudinary;

import java.util.*;

import com.cloudinary.api.ApiResponse;
import com.cloudinary.api.AuthorizationRequired;
import com.cloudinary.api.exceptions.*;
import com.cloudinary.strategies.AbstractApiStrategy;
import com.cloudinary.utils.ObjectUtils;
import com.cloudinary.utils.StringUtils;
import org.cloudinary.json.JSONArray;

@SuppressWarnings({"rawtypes", "unchecked"})
public class Api {


    public enum HttpMethod {GET, POST, PUT, DELETE;}
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

        ApiResponse response = callApi(HttpMethod.GET, uri, ObjectUtils.only(options, "next_cursor", "direction", "max_results", "prefix", "tags", "context", "moderations", "start_at"), options);
        return response;
    }

    public ApiResponse resourcesByTag(String tag, Map options) throws Exception {
        if (options == null) options = ObjectUtils.emptyMap();
        String resourceType = ObjectUtils.asString(options.get("resource_type"), "image");

        ApiResponse response = callApi(HttpMethod.GET, Arrays.asList("resources", resourceType, "tags", tag), ObjectUtils.only(options, "next_cursor", "direction", "max_results", "tags", "context", "moderations"), options);
        return response;
    }

    public ApiResponse resourcesByContext(String key, Map options) throws Exception {
      return resourcesByContext(key,null,options);
    }

    public ApiResponse resourcesByContext(String key,String value, Map options) throws Exception {
        if (options == null) options = ObjectUtils.emptyMap();
        String resourceType = ObjectUtils.asString(options.get("resource_type"), "image");
        Map params = ObjectUtils.only(options, "next_cursor", "direction", "max_results", "tags", "context", "moderations");
        params.put("key",key);
        if (StringUtils.isNotBlank(value)) {
          params.put("value",value);
        }
        return callApi(HttpMethod.GET, Arrays.asList("resources", resourceType,"context"), params , options);
    }

    public ApiResponse resourcesByIds(Iterable<String> publicIds, Map options) throws Exception {
        if (options == null) options = ObjectUtils.emptyMap();
        String resourceType = ObjectUtils.asString(options.get("resource_type"), "image");
        String type = ObjectUtils.asString(options.get("type"), "upload");
        Map params = ObjectUtils.only(options, "tags", "context", "moderations");
        params.put("public_ids", publicIds);
        ApiResponse response = callApi(HttpMethod.GET, Arrays.asList("resources", resourceType, type), params, options);
        return response;
    }

    public ApiResponse resourcesByModeration(String kind, String status, Map options) throws Exception {
        if (options == null) options = ObjectUtils.emptyMap();
        String resourceType = ObjectUtils.asString(options.get("resource_type"), "image");

        ApiResponse response = callApi(HttpMethod.GET, Arrays.asList("resources", resourceType, "moderations", kind, status), ObjectUtils.only(options, "next_cursor", "direction", "max_results", "tags", "context", "moderations"), options);
        return response;
    }

    public ApiResponse resource(String public_id, Map options) throws Exception {
        if (options == null) options = ObjectUtils.emptyMap();
        String resourceType = ObjectUtils.asString(options.get("resource_type"), "image");
        String type = ObjectUtils.asString(options.get("type"), "upload");

        ApiResponse response = callApi(HttpMethod.GET, Arrays.asList("resources", resourceType, type, public_id),
                ObjectUtils.only(options, "exif", "colors", "faces", "coordinates",
                        "image_metadata", "pages", "phash", "max_results"), options);

        return response;
    }

    public ApiResponse update(String public_id, Map options) throws Exception {
        if (options == null) options = ObjectUtils.emptyMap();
        String resourceType = ObjectUtils.asString(options.get("resource_type"), "image");
        String type = ObjectUtils.asString(options.get("type"), "upload");
        Map params = new HashMap<String, Object>();
        Util.processWriteParameters(options, params);
        params.put("moderation_status", options.get("moderation_status"));
        params.put("notification_url", options.get("notification_url"));
        ApiResponse response = callApi(HttpMethod.POST, Arrays.asList("resources", resourceType, type, public_id),
                params, options);
        return response;
    }

    public ApiResponse deleteResources(Iterable<String> publicIds, Map options) throws Exception {
        if (options == null) options = ObjectUtils.emptyMap();
        String resourceType = ObjectUtils.asString(options.get("resource_type"), "image");
        String type = ObjectUtils.asString(options.get("type"), "upload");
        Map params = ObjectUtils.only(options, "keep_original", "invalidate", "next_cursor", "transformations");
        params.put("public_ids", publicIds);
        return callApi(HttpMethod.DELETE, Arrays.asList("resources", resourceType, type), params, options);
    }

    public ApiResponse deleteDerivedResourcesByTransformations(Iterable<String> publicIds, List<Transformation> transformations, Map options) throws Exception {
        if (options == null) options = ObjectUtils.emptyMap();
        String resourceType = ObjectUtils.asString(options.get("resource_type"), "image");
        String type = ObjectUtils.asString(options.get("type"), "upload");
        Map params = ObjectUtils.only(options, "invalidate", "next_cursor");
        params.put("keep_original", true);
        params.put("public_ids", publicIds);
        params.put("transformations", Util.buildEager(transformations));
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
        return callApi(HttpMethod.GET, Arrays.asList("transformations", transformation), ObjectUtils.only(options, "next_cursor", "max_results"), options);
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

        ApiResponse response = callApi(HttpMethod.POST, Arrays.asList("resources", resourceType, type, "restore"), params, options);
        return response;
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

    public ApiResponse publishByPrefix(String prefix, Map options) throws Exception {
        return publishResource("prefix", prefix, options);
    }

    public ApiResponse publishByTag(String tag, Map options) throws Exception {
        return publishResource("tag", tag, options);
    }

    public ApiResponse publishByIds(Iterable<String> publicIds, Map options) throws Exception {
        return publishResource("public_ids", publicIds, options);
    }

    private ApiResponse publishResource(String byKey, Object value, Map options) throws Exception {
        if (options == null) options = ObjectUtils.emptyMap();
        String resourceType = ObjectUtils.asString(options.get("resource_type"), "image");
        List<String> uri = new ArrayList<String>();
        uri.add("resources");
        uri.add(resourceType);
        uri.add("publish_resources");
        Map params = new HashMap<String, Object>();
        params.put(byKey, value);
        params.putAll(ObjectUtils.only(options, "invalidate", "overwrite", "type"));
        return callApi(HttpMethod.POST, uri, params, options);
    }

    /**
     * Create a new streaming profile
     *
     * @param name            the of the profile
     * @param displayName     the display name of the profile
     * @param representations a collection of Maps with a transformation key
     * @param options         additional options
     * @return the new streaming profile
     * @throws Exception an exception
     */
    public ApiResponse createStreamingProfile(String name, String displayName, List<Map> representations, Map options) throws Exception {
        if (options == null)
            options = ObjectUtils.emptyMap();
        List<Map> serializedRepresentations = new ArrayList<Map>(representations.size());
        for (Map t : representations) {
            final Object transformation = t.get("transformation");
            serializedRepresentations.add(ObjectUtils.asMap("transformation", transformation.toString()));
        }
        List<String> uri = Collections.singletonList("streaming_profiles");
        final Map params = ObjectUtils.asMap(
                "name", name,
                "representations", new JSONArray(serializedRepresentations.toArray())
        );
        if (displayName != null) {
            params.put("display_name", displayName);
        }
        return callApi(HttpMethod.POST, uri, params, options);
    }

    /**
     * @see Api#createStreamingProfile(String, String, List, Map)
     */
    public ApiResponse createStreamingProfile(String name, String displayName, List<Map> representations) throws Exception {
        return createStreamingProfile(name, displayName, representations, null);
    }

    /**
     * Get a streaming profile information
     * @param name the name of the profile to fetch
     * @param options additional options
     * @return a streaming profile
     * @throws Exception an exception
     */
    public ApiResponse getStreamingProfile(String name, Map options) throws Exception {
        if (options == null)
            options = ObjectUtils.emptyMap();
        List<String> uri = Arrays.asList("streaming_profiles", name);

        return callApi(HttpMethod.GET, uri, ObjectUtils.emptyMap(), options);

    }

    /**
     * @see Api#getStreamingProfile(String, Map)
     */
    public ApiResponse getStreamingProfile(String name) throws Exception {
        return getStreamingProfile(name, null);
    }

    /**
     * List Streaming profiles
     * @param options additional options
     * @return a list of all streaming profiles defined for the current cloud
     * @throws Exception an exception
     */
    public ApiResponse listStreamingProfiles(Map options) throws Exception {
        if (options == null)
            options = ObjectUtils.emptyMap();
        List<String> uri = Collections.singletonList("streaming_profiles");
        return callApi(HttpMethod.GET, uri, ObjectUtils.emptyMap(), options);

    }

    /**
     * @see Api#listStreamingProfiles(Map)
     */
    public ApiResponse listStreamingProfiles() throws Exception {
        return listStreamingProfiles(null);
    }

    /**
     * Delete a streaming profile information. Predefined profiles are restored to the default setting.
     * @param name the name of the profile to delete
     * @param options additional options
     * @return a streaming profile
     * @throws Exception an exception
     */
    public ApiResponse deleteStreamingProfile(String name, Map options) throws Exception {
        if (options == null)
            options = ObjectUtils.emptyMap();
        List<String> uri = Arrays.asList("streaming_profiles", name);

        return callApi(HttpMethod.DELETE, uri, ObjectUtils.emptyMap(), options);

    }

    /**
     * @see Api#deleteStreamingProfile(String, Map)
     */
    public ApiResponse deleteStreamingProfile(String name) throws Exception {
        return deleteStreamingProfile(name, null);
    }

    /**
     * Create a new streaming profile
     *
     * @param name            the of the profile
     * @param displayName     the display name of the profile
     * @param representations a collection of Maps with a transformation key
     * @param options         additional options
     * @return the new streaming profile
     * @throws Exception an exception
     */
    public ApiResponse updateStreamingProfile(String name, String displayName, List<Map> representations, Map options) throws Exception {
        if (options == null)
            options = ObjectUtils.emptyMap();
        List<Map> serializedRepresentations;
        final Map params = new HashMap();
        List<String> uri = Arrays.asList("streaming_profiles", name);

        if (representations != null) {
            serializedRepresentations = new ArrayList<Map>(representations.size());
            for (Map t : representations) {
                final Object transformation = t.get("transformation");
                serializedRepresentations.add(ObjectUtils.asMap("transformation", transformation.toString()));
            }
            params.put("representations", new JSONArray(serializedRepresentations.toArray()));
        }
        if (displayName != null) {
            params.put("display_name", displayName);
        }
        return callApi(HttpMethod.PUT, uri, params, options);
    }

    /**
     * @see Api#updateStreamingProfile(String, String, List, Map)
     */
    public ApiResponse updateStreamingProfile(String name, String displayName, List<Map> representations) throws Exception {
        return createStreamingProfile(name, displayName, representations);
    }

    /**
     * Update access mode of one or more resources by prefix
     *
     * @param accessMode The new access mode, "public" or  "authenticated"
     * @param prefix     The prefix by which to filter applicable resources
     * @param options    additional options
     * <ul>
     * <li>resource_type - (default "image") - the type of resources to modify</li>
     * <li>max_results - optional - the maximum resources to process in a single invocation</li>
     * <li>next_cursor - optional - provided by a previous call to the method</li>
     * </ul>
     * @return a map of the returned values
     * <ul>
     * <li>updated - an array of resources</li>
     * <li>next_cursor - optional - provided if more resources can be processed</li>
     * </ul>
     * @throws ApiException an API exception
     */
    public ApiResponse updateResourcesAccessModeByPrefix(String accessMode, String prefix, Map options) throws Exception {
        return updateResourcesAccessMode(accessMode, "prefix", prefix, options);
    }

    /**
     * Update access mode of one or more resources by tag
     *
     * @param accessMode The new access mode, "public" or  "authenticated"
     * @param tag        The tag by which to filter applicable resources
     * @param options    additional options
     * <ul>
     * <li>resource_type - (default "image") - the type of resources to modify</li>
     * <li>max_results - optional - the maximum resources to process in a single invocation</li>
     * <li>next_cursor - optional - provided by a previous call to the method</li>
     * </ul>
     * @return a map of the returned values
     * <ul>
     * <li>updated - an array of resources</li>
     * <li>next_cursor - optional - provided if more resources can be processed</li>
     * </ul>
     * @throws ApiException an API exception
     */
    public ApiResponse updateResourcesAccessModeByTag(String accessMode, String tag, Map options) throws Exception {
        return updateResourcesAccessMode(accessMode, "tag", tag, options);
    }

    /**
     * Update access mode of one or more resources by publicIds
     *
     * @param accessMode The new access mode, "public" or  "authenticated"
     * @param publicIds  A list of public ids of resources to be updated
     * @param options    additional options
     * <ul>
     * <li>resource_type - (default "image") - the type of resources to modify</li>
     * <li>max_results - optional - the maximum resources to process in a single invocation</li>
     * <li>next_cursor - optional - provided by a previous call to the method</li>
     * </ul>
     * @return a map of the returned values
     * <ul>
     * <li>updated - an array of resources</li>
     * <li>next_cursor - optional - provided if more resources can be processed</li>
     * </ul>
     * @throws ApiException an API exception
     */
    public ApiResponse updateResourcesAccessModeByIds(String accessMode, Iterable<String> publicIds, Map options) throws Exception {
        return updateResourcesAccessMode(accessMode, "public_ids", publicIds, options);
    }

    private ApiResponse updateResourcesAccessMode(String accessMode, String byKey, Object value, Map options) throws Exception {
        if (options == null) options = ObjectUtils.emptyMap();
        String resourceType = ObjectUtils.asString(options.get("resource_type"), "image");
        String type = ObjectUtils.asString(options.get("type"), "upload");
        List<String> uri = Arrays.asList("resources", resourceType, type, "update_access_mode");
        Map params = ObjectUtils.only(options, "next_cursor", "max_results");
        params.put("access_mode", accessMode);
        params.put(byKey, value);
        return callApi(HttpMethod.POST, uri, params, options);
    }

}
