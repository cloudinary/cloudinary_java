package com.cloudinary;

import java.util.*;

import com.cloudinary.api.ApiResponse;
import com.cloudinary.api.AuthorizationRequired;
import com.cloudinary.api.exceptions.*;
import com.cloudinary.metadata.MetadataField;
import com.cloudinary.metadata.MetadataDataSource;
import com.cloudinary.metadata.MetadataRule;
import com.cloudinary.strategies.AbstractApiStrategy;
import com.cloudinary.utils.Base64Coder;
import com.cloudinary.utils.ObjectUtils;
import com.cloudinary.utils.StringUtils;
import org.cloudinary.json.JSONArray;

@SuppressWarnings({"rawtypes", "unchecked"})
public class Api {


    public AbstractApiStrategy getStrategy() {
        return strategy;
    }

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
        if (options == null)
            options = ObjectUtils.emptyMap();

        String apiKey = ObjectUtils.asString(options.get("api_key"), this.cloudinary.config.apiKey);
        String apiSecret = ObjectUtils.asString(options.get("api_secret"), this.cloudinary.config.apiSecret);
        String oauthToken = ObjectUtils.asString(options.get("oauth_token"), this.cloudinary.config.oauthToken);

        validateAuthorization(apiKey, apiSecret, oauthToken);


        String authorizationHeader = getAuthorizationHeaderValue(apiKey, apiSecret, oauthToken);
        String apiUrl = createApiUrl(uri, options);
        return this.strategy.callApi(method, apiUrl, params, options, authorizationHeader);
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

        final List<String> uri = new ArrayList<String>();
        uri.add("usage");

        Object date = options.get("date");

        if (date != null) {
            if (date instanceof Date) {
                date = ObjectUtils.toUsageApiDateFormat((Date) date);
            }

            uri.add(date.toString());
        }

        return callApi(HttpMethod.GET, uri, ObjectUtils.emptyMap(), options);
    }

    public ApiResponse configuration(Map options) throws  Exception {
        if(options == null) options = ObjectUtils.emptyMap();

        final List<String> uri = new ArrayList<String>();
        uri.add("config");

        Map params = ObjectUtils.only(options, "settings");

        return callApi(HttpMethod.GET, uri, params, options);
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
        if(options.get("fields") != null) {
            options.put("fields", StringUtils.join(ObjectUtils.asArray(options.get("fields")), ","));
        }
        ApiResponse response = callApi(HttpMethod.GET, uri, ObjectUtils.only(options, "next_cursor", "direction", "max_results", "prefix", "tags", "context", "moderations", "start_at", "metadata", "fields"), options);
        return response;
    }

    public ApiResponse visualSearch(Map options) throws Exception {
        List<String> uri = new ArrayList<String>();
        uri.add("resources/visual_search");
        uri.add("image");
        if (options.get("text") == null && options.get("image_asset_id") == null && options.get("image_url") == null) {
            throw new IllegalArgumentException("Must supply image file, image url, image asset id or text");
        }
        ApiResponse response = callApi(HttpMethod.GET, uri, options, options);
        return response;
    }

    public ApiResponse resourcesByTag(String tag, Map options) throws Exception {
        if (options == null) options = ObjectUtils.emptyMap();
        String resourceType = ObjectUtils.asString(options.get("resource_type"), "image");
        if(options.get("fields") != null) {
            options.put("fields", StringUtils.join(ObjectUtils.asArray(options.get("fields")), ","));
        }
        ApiResponse response = callApi(HttpMethod.GET, Arrays.asList("resources", resourceType, "tags", tag), ObjectUtils.only(options, "next_cursor", "direction", "max_results", "tags", "context", "moderations", "metadata", "fields"), options);
        return response;
    }

    public ApiResponse resourcesByContext(String key, Map options) throws Exception {
        return resourcesByContext(key, null, options);
    }

    public ApiResponse resourcesByContext(String key, String value, Map options) throws Exception {
        if (options == null) options = ObjectUtils.emptyMap();
        String resourceType = ObjectUtils.asString(options.get("resource_type"), "image");
        if(options.get("fields") != null) {
            options.put("fields", StringUtils.join(ObjectUtils.asArray(options.get("fields")), ","));
        }
        Map params = ObjectUtils.only(options, "next_cursor", "direction", "max_results", "tags", "context", "moderations", "metadata", "fields");
        params.put("key", key);
        if (StringUtils.isNotBlank(value)) {
            params.put("value", value);
        }
        return callApi(HttpMethod.GET, Arrays.asList("resources", resourceType, "context"), params, options);
    }

    public ApiResponse resourceByAssetID(String assetId, Map options) throws Exception {
        if (options == null) options = ObjectUtils.emptyMap();
        if(options.get("fields") != null) {
            options.put("fields", StringUtils.join(ObjectUtils.asArray(options.get("fields")), ","));
        }
        Map params = ObjectUtils.only(options, "tags", "context", "moderations", "fields");
        ApiResponse response = callApi(HttpMethod.GET, Arrays.asList("resources", assetId), params, options);
        return response;
    }
    public ApiResponse resourcesByAssetIDs(Iterable<String> assetIds, Map options) throws Exception {
        if (options == null) options = ObjectUtils.emptyMap();
        Map params = ObjectUtils.only(options, "public_ids", "tags", "context", "moderations");
        params.put("asset_ids", assetIds);
        ApiResponse response = callApi(HttpMethod.GET, Arrays.asList("resources", "by_asset_ids"), params, options);
        return response;
    }

    public ApiResponse resourcesByAssetFolder(String assetFolder, Map options) throws Exception {
        if (options == null) options = ObjectUtils.emptyMap();
        if(options.get("fields") != null) {
            options.put("fields", StringUtils.join(ObjectUtils.asArray(options.get("fields")), ","));
        }
        Map params = ObjectUtils.only(options, "next_cursor", "direction", "max_results", "tags", "context", "moderations", "fields");
        params.put("asset_folder", assetFolder);
        ApiResponse response = callApi(HttpMethod.GET, Arrays.asList("resources/by_asset_folder"), params, options);
        return response;
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
        if(options.get("fields") != null) {
            options.put("fields", StringUtils.join(ObjectUtils.asArray(options.get("fields")), ","));
        }
        ApiResponse response = callApi(HttpMethod.GET, Arrays.asList("resources", resourceType, "moderations", kind, status), ObjectUtils.only(options, "next_cursor", "direction", "max_results", "tags", "context", "moderations", "metadata", "fields"), options);
        return response;
    }

    public ApiResponse resource(String public_id, Map options) throws Exception {
        if (options == null) options = ObjectUtils.emptyMap();
        String resourceType = ObjectUtils.asString(options.get("resource_type"), "image");
        String type = ObjectUtils.asString(options.get("type"), "upload");

        ApiResponse response = callApi(HttpMethod.GET, Arrays.asList("resources", resourceType, type, public_id),
                ObjectUtils.only(options, "exif", "colors", "faces", "coordinates",
                        "image_metadata", "pages", "phash", "max_results", "quality_analysis", "cinemagraph_analysis",
                        "accessibility_analysis", "versions", "media_metadata", "derived_next_cursor"), options);

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

    public ApiResponse deleteDerivedByTransformation(Iterable<String> publicIds, List<Transformation> transformations, Map options) throws Exception {
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
        return callApi(HttpMethod.GET, Arrays.asList("transformations"), ObjectUtils.only(options, "next_cursor", "max_results", "named"), options);
    }

    public ApiResponse transformation(String transformation, Map options) throws Exception {
        if (options == null) options = ObjectUtils.emptyMap();
        Map map = ObjectUtils.only(options, "next_cursor", "max_results");
        map.put("transformation", transformation);
        return callApi(HttpMethod.GET, Arrays.asList("transformations"), map, options);
    }

    public ApiResponse deleteTransformation(String transformation, Map options) throws Exception {
        if (options == null) options = ObjectUtils.emptyMap();
        Map updates = ObjectUtils.asMap("transformation", transformation);
        return callApi(HttpMethod.DELETE, Arrays.asList("transformations"), updates, options);
    }

    // updates - currently only supported update are:
    // "allowed_for_strict": boolean flag
    // "unsafe_update": transformation string
    public ApiResponse updateTransformation(String transformation, Map updates, Map options) throws Exception {
        if (options == null) options = ObjectUtils.emptyMap();
        updates.put("transformation", transformation);
        return callApi(HttpMethod.PUT, Arrays.asList("transformations"), updates, options);
    }

    public ApiResponse createTransformation(String name, String definition, Map options) throws Exception {
        return callApi(HttpMethod.POST, 
                Arrays.asList("transformations"), 
                ObjectUtils.asMap("transformation", definition, "name", name), options);
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
        return callApi(HttpMethod.GET, Arrays.asList("folders"),
                extractParams(options, Arrays.asList("max_results", "next_cursor")),
                options);
    }

    public ApiResponse subFolders(String ofFolderPath, Map options) throws Exception {
        if (options == null)
            options = ObjectUtils.emptyMap();
        return callApi(HttpMethod.GET, Arrays.asList("folders", ofFolderPath),
                extractParams(options, Arrays.asList("max_results", "next_cursor")),
                options);
    }

    //Creates an empty folder
    public ApiResponse createFolder(String folderName, Map options) throws Exception {
        if (options == null)
            options = ObjectUtils.emptyMap();
        return callApi(HttpMethod.POST, Arrays.asList("folders", folderName), ObjectUtils.emptyMap(), options);
    }

    public ApiResponse restore(Iterable<String> publicIds, Map options) throws Exception {
        if (options == null)
            options = ObjectUtils.emptyMap();
        String resourceType = ObjectUtils.asString(options.get("resource_type"), "image");
        String type = ObjectUtils.asString(options.get("type"), "upload");
        Map params = new HashMap<String, Object>();
        params.put("public_ids", publicIds);
        params.put("versions", options.get("versions"));

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
     *
     * @param name    the name of the profile to fetch
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
     *
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
     *
     * @param name    the name of the profile to delete
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
     *                   <ul>
     *                   <li>resource_type - (default "image") - the type of resources to modify</li>
     *                   <li>max_results - optional - the maximum resources to process in a single invocation</li>
     *                   <li>next_cursor - optional - provided by a previous call to the method</li>
     *                   </ul>
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
     *                   <ul>
     *                   <li>resource_type - (default "image") - the type of resources to modify</li>
     *                   <li>max_results - optional - the maximum resources to process in a single invocation</li>
     *                   <li>next_cursor - optional - provided by a previous call to the method</li>
     *                   </ul>
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
     * Delete a folder (must be empty).
     *
     * @param folder  The full path of the folder to delete
     * @param options additional options.
     * @return The operation result.
     * @throws Exception When the folder isn't empty or doesn't exist.
     */
    public ApiResponse deleteFolder(String folder, Map options) throws Exception {
        List<String> uri = Arrays.asList("folders", folder);
        return callApi(HttpMethod.DELETE, uri, Collections.<String, Object>emptyMap(), options);
    }

    /**
     * Update access mode of one or more resources by publicIds
     *
     * @param accessMode The new access mode, "public" or  "authenticated"
     * @param publicIds  A list of public ids of resources to be updated
     * @param options    additional options
     *                   <ul>
     *                   <li>resource_type - (default "image") - the type of resources to modify</li>
     *                   <li>max_results - optional - the maximum resources to process in a single invocation</li>
     *                   <li>next_cursor - optional - provided by a previous call to the method</li>
     *                   </ul>
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

    /**
     * Add a new metadata field definition
     *
     * @param field The field to add.
     * @return A map representing the newly added field.
     * @throws Exception
     */
    public ApiResponse addMetadataField(MetadataField field) throws Exception {
        return callApi(HttpMethod.POST, Collections.singletonList("metadata_fields"),
                ObjectUtils.toMap(field), ObjectUtils.asMap("content_type", "json"));
    }

    /**
     * List all the metadata field definitions (structure, not values)
     *
     * @return A map containing the list of field definitions maps.
     * @throws Exception
     */
    public ApiResponse listMetadataFields() throws Exception {
        return callApi(HttpMethod.GET, Collections.singletonList("metadata_fields"), Collections.<String, Object>emptyMap(), Collections.emptyMap());
    }

    /**
     * Get a metadata field definition by id
     *
     * @param fieldExternalId The id of the field to retrieve
     * @return The fields definitions.
     * @throws Exception
     */
    public ApiResponse metadataFieldByFieldId(String fieldExternalId) throws Exception {
        return callApi(HttpMethod.GET, Arrays.asList("metadata_fields", fieldExternalId), Collections.<String, Object>emptyMap(), Collections.emptyMap());
    }

    /**
     * Update the definitions of a single metadata field.
     *
     * @param fieldExternalId The id of the field to update
     * @param field           The field definition
     * @return The updated fields definition.
     * @throws Exception
     */
    public ApiResponse updateMetadataField(String fieldExternalId, MetadataField field) throws Exception {
        List<String> uri = Arrays.asList("metadata_fields", fieldExternalId);
        return callApi(HttpMethod.PUT, uri, ObjectUtils.toMap(field), Collections.singletonMap("content_type", "json"));
    }

    /**
     * Update the datasource entries for a given field
     *
     * @param fieldExternalId The id of the field to update
     * @param entries         A list of datasource entries. Existing entries (according to entry id) will be updated,
     *                        new entries will be added.
     * @return The updated field definition.
     * @throws Exception
     */
    public ApiResponse updateMetadataFieldDatasource(String fieldExternalId, List<MetadataDataSource.Entry> entries) throws Exception {
        List<String> uri = Arrays.asList("metadata_fields", fieldExternalId, "datasource");
        return callApi(HttpMethod.PUT, uri, Collections.singletonMap("values", entries), Collections.singletonMap("content_type", "json"));
    }

    /**
     * Delete data source entries for a given field
     *
     * @param fieldExternalId   The id of the field to update
     * @param entriesExternalId The ids of all the entries to delete from the data source
     * @return The remaining datasource entries.
     * @throws Exception
     */
    public ApiResponse deleteDatasourceEntries(String fieldExternalId, List<String> entriesExternalId) throws Exception {
        List<String> uri = Arrays.asList("metadata_fields", fieldExternalId, "datasource");
        return callApi(HttpMethod.DELETE, uri, Collections.singletonMap("external_ids", entriesExternalId), Collections.emptyMap());
    }

    /**
     * Restore deleted data source entries for a given field
     *
     * @param fieldExternalId   The id of the field to operate
     * @param entriesExternalId The ids of all the entries to restore from the data source
     * @return The datasource entries state after restore
     * @throws Exception
     */
    public ApiResponse restoreDatasourceEntries(String fieldExternalId, List<String> entriesExternalId) throws Exception {
        List<String> uri = Arrays.asList("metadata_fields", fieldExternalId, "datasource_restore");
        return callApi(HttpMethod.POST, uri, Collections.singletonMap("external_ids", entriesExternalId), Collections.singletonMap("content_type", "json"));
    }

    /**
     * Delete a field definition.
     *
     * @param fieldExternalId The id of the field to delete
     * @return A map with a "message" key. "ok" value indicates a successful deletion.
     * @throws Exception
     */
    public ApiResponse deleteMetadataField(String fieldExternalId) throws Exception {
        List<String> uri = Arrays.asList("metadata_fields", fieldExternalId);
        return callApi(HttpMethod.DELETE, uri, Collections.<String, Object>emptyMap(), Collections.emptyMap());
    }

    /**
     * Reorders metadata fields.
     *
     * @param orderBy Criteria for the order (one of the fields 'label', 'external_id', 'created_at')
     * @param direction Optional (gets either asc or desc)
     * @param options Additional options
     * @return List of metadata fields in their new order
     * @throws Exception
     */
    public ApiResponse reorderMetadataFields(String orderBy, String direction, Map options) throws Exception {
        if (orderBy == null) {
            throw new IllegalArgumentException("Must supply orderBy");
        }

        List<String> uri = Arrays.asList("metadata_fields", "order");
        Map<String, Object> map = ObjectUtils.asMap("order_by", orderBy);
        if (direction != null) {
            map.put("direction", direction);
        }

        return callApi(HttpMethod.PUT, uri, map, options);
    }

    public ApiResponse listMetadataRules(Map options) throws Exception {
        if (options == null || options.isEmpty()) options = ObjectUtils.asMap();
        final Map params = new HashMap();
        List<String> uri = Arrays.asList("metadata_rules");
        return callApi(HttpMethod.GET, uri, params, options);
    }

    public ApiResponse addMetadataRule(MetadataRule rule, Map options) throws Exception {
        if (options == null || options.isEmpty()) options = ObjectUtils.asMap();
        options.put("content_type", "json");
        final Map params = rule.asMap();
        List<String> uri = Arrays.asList("metadata_rules");
        return callApi(HttpMethod.POST, uri, params, options);
    }

    public ApiResponse updateMetadataRule(String externalId, MetadataRule rule, Map options) throws Exception {
        if (options == null || options.isEmpty()) options = ObjectUtils.asMap();
        options.put("content_type", "json");
        final Map params = rule.asMap();
        List<String> uri = Arrays.asList("metadata_rules", externalId);
        return callApi(HttpMethod.PUT, uri, params, options);
    }

    public ApiResponse deleteMetadataRule(String externalId, Map options) throws Exception {
        if (options == null || options.isEmpty()) options = ObjectUtils.asMap();
        List<String> uri = Arrays.asList("metadata_rules", externalId);
        return callApi(HttpMethod.DELETE, uri, ObjectUtils.emptyMap(), options);
    }

    public ApiResponse analyze(String inputType, String analysisType, String uri, Map options) throws Exception {
        if (options == null || options.isEmpty()) options = ObjectUtils.asMap();
        List<String> url = Arrays.asList("analysis", "analyze", inputType);
        options.put("api_version", "v2");
        options.put("content_type", "json");
        final Map params = new HashMap();
        params.put("analysis_type", analysisType);
        params.put("uri", uri);
        return callApi(HttpMethod.POST, url, params, options);
    }

    public ApiResponse renameFolder(String path, String toPath, Map options) throws Exception {
        if (options == null || options.isEmpty()) options = ObjectUtils.asMap();
        List<String> url = Arrays.asList("folders", path);

        final Map params = new HashMap();
        params.put("to_folder", toPath);

        return callApi(HttpMethod.PUT, url, params, options);

    }

    public ApiResponse deleteBackedUpAssets(String assetId, String[] versionIds, Map options) throws Exception {
        if (options == null || options.isEmpty()) options = ObjectUtils.asMap();
        if (StringUtils.isEmpty(assetId)) {
            throw new IllegalArgumentException("AssetId parameter is required");
        }

        if (versionIds == null || versionIds.length == 0) {
            throw new IllegalArgumentException("VersionIds parameter is required");
        }

        List<String> url = Arrays.asList("resources", "backup", assetId);

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("version_ids[]", StringUtils.join(versionIds, "&"));

        return callApi(HttpMethod.DELETE, url, params, options);

    }

    private Map<String, ?> extractParams(Map options, List<String> keys) {
        Map<String, Object> result = new HashMap<String, Object>();
        for (String key : keys) {
            Object option = options.get(key);

            if (option != null) {
                result.put(key, option);
            }
        }
        return result;
    }

    protected void validateAuthorization(String apiKey, String apiSecret, String oauthToken) {
        if (oauthToken == null) {
            if (apiKey == null) throw new IllegalArgumentException("Must supply api_key");
            if (apiSecret == null) throw new IllegalArgumentException("Must supply api_secret");
        }
    }

    protected String getAuthorizationHeaderValue(String apiKey, String apiSecret, String oauthToken) {
        if (oauthToken != null){
            return "Bearer " + oauthToken;
        } else {
            return "Basic " + Base64Coder.encodeString(apiKey + ":" + apiSecret);
        }
    }

    protected String createApiUrl (Iterable<String> uri, Map options){
        String version = ObjectUtils.asString(options.get("api_version"), "v1_1");
        String prefix = ObjectUtils.asString(options.get("upload_prefix"), ObjectUtils.asString(this.cloudinary.config.uploadPrefix, "https://api.cloudinary.com"));
        String cloudName = ObjectUtils.asString(options.get("cloud_name"), this.cloudinary.config.cloudName);
        if (cloudName == null) throw new IllegalArgumentException("Must supply cloud_name");
        String apiUrl = StringUtils.join(Arrays.asList(prefix, version, cloudName), "/");
        for (String component : uri) {
            component = SmartUrlEncoder.encode(component);
            apiUrl = apiUrl + "/" + component;

        }
        return apiUrl;
    }
}
