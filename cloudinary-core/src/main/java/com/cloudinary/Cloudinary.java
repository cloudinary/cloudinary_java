package com.cloudinary;

import com.cloudinary.api.signing.ApiResponseSignatureVerifier;
import com.cloudinary.api.signing.NotificationRequestSignatureVerifier;
import com.cloudinary.strategies.AbstractApiStrategy;
import com.cloudinary.strategies.AbstractUploaderStrategy;
import com.cloudinary.strategies.StrategyLoader;
import com.cloudinary.utils.Analytics;
import com.cloudinary.utils.ObjectUtils;
import com.cloudinary.utils.StringUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.SecureRandom;
import java.util.*;

import static com.cloudinary.Util.buildMultiParams;

@SuppressWarnings({"rawtypes", "unchecked"})
public class Cloudinary {

    private static List<String> UPLOAD_STRATEGIES = new ArrayList<String>(Arrays.asList(
            "com.cloudinary.android.UploaderStrategy",
            "com.cloudinary.http42.UploaderStrategy",
            "com.cloudinary.http43.UploaderStrategy",
            "com.cloudinary.http44.UploaderStrategy",
            "com.cloudinary.http45.UploaderStrategy"));
    public static List<String> API_STRATEGIES = new ArrayList<String>(Arrays.asList(
            "com.cloudinary.android.ApiStrategy",
            "com.cloudinary.http42.ApiStrategy",
            "com.cloudinary.http43.ApiStrategy",
            "com.cloudinary.http44.ApiStrategy",
            "com.cloudinary.http45.ApiStrategy"));

    public final static String CF_SHARED_CDN = "d3jpl91pxevbkh.cloudfront.net";
    public final static String OLD_AKAMAI_SHARED_CDN = "cloudinary-a.akamaihd.net";
    public final static String AKAMAI_SHARED_CDN = "res.cloudinary.com";
    public final static String SHARED_CDN = AKAMAI_SHARED_CDN;

    public final static String VERSION = "1.39.0";
    static String USER_AGENT_PREFIX = "CloudinaryJava";
    public final static String USER_AGENT_JAVA_VERSION = "(Java " + System.getProperty("java.version") + ")";

    public final Configuration config;
    private AbstractUploaderStrategy uploaderStrategy;
    private AbstractApiStrategy apiStrategy;
    private String userAgent = USER_AGENT_PREFIX+"/"+ VERSION + " "+USER_AGENT_JAVA_VERSION;
    public Analytics analytics = new Analytics();
    public Uploader uploader() {
        return new Uploader(this, uploaderStrategy);
    }

    public Api api() {
        return new Api(this, apiStrategy);
    }

    public Search search() {
        return new Search(this);
    }

    public SearchFolders searchFolders() {
        return new SearchFolders(this);
    }

    public static void registerUploaderStrategy(String className) {
        if (!UPLOAD_STRATEGIES.contains(className)) {
            UPLOAD_STRATEGIES.add(className);
        }

    }

    public static void registerAPIStrategy(String className) {
        if (!API_STRATEGIES.contains(className)) {
            API_STRATEGIES.add(className);
        }
    }

    private void loadStrategies() {
        if (!this.config.loadStrategies) return;
        uploaderStrategy = StrategyLoader.find(UPLOAD_STRATEGIES);

        if (uploaderStrategy == null) {
            throw new UnknownError("Can't find Cloudinary platform adapter [" + StringUtils.join(UPLOAD_STRATEGIES, ",") + "]");
        }

        apiStrategy = StrategyLoader.find(API_STRATEGIES);
        if (apiStrategy == null) {
            throw new UnknownError("Can't find Cloudinary platform adapter [" + StringUtils.join(API_STRATEGIES, ",") + "]");
        }
    }

    public Cloudinary(Map config) {
        this.config = new Configuration(config);
        loadStrategies();
    }

    public Cloudinary(String cloudinaryUrl) {
        this.config = Configuration.from(cloudinaryUrl);
        loadStrategies();
    }

    public Cloudinary() {
        String cloudinaryUrl = System.getProperty("CLOUDINARY_URL", System.getenv("CLOUDINARY_URL"));
        if (cloudinaryUrl != null) {
            this.config = Configuration.from(cloudinaryUrl);
        } else {
            this.config = new Configuration();
        }
        loadStrategies();
    }

    public Url url() {
        return new Url(this);
    }

    public String cloudinaryApiUrl(String action, Map options) {
        String cloudinary = ObjectUtils.asString(options.get("upload_prefix"),
                ObjectUtils.asString(this.config.uploadPrefix, "https://api.cloudinary.com"));
        String cloud_name = ObjectUtils.asString(options.get("cloud_name"), ObjectUtils.asString(this.config.cloudName));
        if (cloud_name == null)
            throw new IllegalArgumentException("Must supply cloud_name in tag or in configuration");
        String resource_type = ObjectUtils.asString(options.get("resource_type"), "image");
        return StringUtils.join(new String[]{cloudinary, "v1_1", cloud_name, resource_type, action}, "/");
    }

    private final static SecureRandom RND = new SecureRandom();

    public String randomPublicId() {
        byte[] bytes = new byte[8];
        RND.nextBytes(bytes);
        return StringUtils.encodeHexString(bytes);
    }

    public String signedPreloadedImage(Map result) {
        return result.get("resource_type") + "/upload/v" + result.get("version") + "/" + result.get("public_id")
                + (result.containsKey("format") ? "." + result.get("format") : "") + "#" + result.get("signature");
    }

    public String apiSignRequest(Map<String, Object> paramsToSign, String apiSecret) {
        return Util.produceSignature(paramsToSign, apiSecret, config.signatureAlgorithm);
    }

    /**
     * @return the userAgent that will be sent with every API call.
     */
    public String getUserAgent(){
        return userAgent;
    }

    /**
     * Set the prefix and version for the user agent that will be sent with every API call
     * a userAgent is built from `prefix/version (additional data)`
     * @param prefix - the prefix of the userAgent to be set
     * @param version - the version of the userAgent to be set
     */
    public void setUserAgent(String prefix, String version){
        userAgent = prefix+"/"+ version + " ("+USER_AGENT_PREFIX+ " "+VERSION+") " + USER_AGENT_JAVA_VERSION;
    }

    /**
     * Set the analytics object that will be sent with every URL generation call.
     * @param analytics - the analytics object to set
     */
    public void setAnalytics(Analytics analytics) {
        this.analytics = analytics;
    }

    /**
     * Verifies that Cloudinary notification request is genuine by checking its signature.
     *
     * Cloudinary can asynchronously process your e.g. image uploads requests. This is achieved by calling back API you
     * specified during preparing of upload request as soon as it has been processed. See Upload Notifications in
     * Cloudinary documentation for more details. In order to make sure it is Cloudinary calling your API back, hashed
     * message authentication codes (HMAC's) based on agreed hashing function and configured Cloudinary API secret key
     * are used for signing the requests.
     *
     * The following method serves as a convenient utility to perform the verification procedure.
     *
     * @param body Cloudinary Notification request body represented as string
     * @param timestamp Cloudinary Notification request custom X-Cld-Timestamp HTTP header value
     * @param signature Cloudinary Notification request custom X-Cld-Signature HTTP header value, i.e. the HMAC
     * @param validFor desired period of request validity since issued, in seconds, for protection against replay attacks
     * @return whether request signature is valid or not
     */
    public boolean verifyNotificationSignature(String body, String timestamp, String signature, long validFor) {
        return new NotificationRequestSignatureVerifier(config.apiSecret, config.signatureAlgorithm).verifySignature(body, timestamp, signature, validFor);
    }

    /**
     * Verifies that Cloudinary API response is genuine by checking its signature.
     *
     * Cloudinary can add a signature value in the response to API methods returning public id's and versions. In order
     * to make sure it is genuine Cloudinary response, hashed message authentication codes (HMAC's) based on agreed hashing
     * function and configured Cloudinary API secret key are used for signing the responses.
     *
     * The following method serves as a convenient utility to perform the verification procedure.
     *
     * @param publicId publicId response field value
     * @param version version response field value
     * @param signature signature response field value, i.e. the HMAC
     * @return whether response signature is valid or not
     */
    public boolean verifyApiResponseSignature(String publicId, String version, String signature) {
        return new ApiResponseSignatureVerifier(config.apiSecret, config.signatureAlgorithm).verifySignature(publicId, version, signature);
    }

    public void signRequest(Map<String, Object> params, Map<String, Object> options) {
        String apiKey = ObjectUtils.asString(options.get("api_key"), this.config.apiKey);
        if (apiKey == null)
            throw new IllegalArgumentException("Must supply api_key");
        String apiSecret = ObjectUtils.asString(options.get("api_secret"), this.config.apiSecret);
        if (apiSecret == null)
            throw new IllegalArgumentException("Must supply api_secret");
        Util.clearEmpty(params);
        params.put("signature", this.apiSignRequest(params, apiSecret));
        params.put("api_key", apiKey);
    }

    public String privateDownload(String publicId, String format, Map<String, Object> options) throws Exception {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("public_id", publicId);
        params.put("format", format);
        params.put("attachment", options.get("attachment"));
        params.put("type", options.get("type"));
        params.put("expires_at", options.get("expires_at"));
        params.put("timestamp", Util.timestamp());
        signRequest(params, options);
        return buildUrl(cloudinaryApiUrl("download", options), params);
    }

    public String zipDownload(String tag, Map<String, Object> options) throws Exception {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("timestamp", Util.timestamp());
        params.put("tag", tag);
        Object transformation = options.get("transformation");
        if (transformation != null) {
            if (transformation instanceof Transformation) {
                transformation = ((Transformation) transformation).generate();
            }
            params.put("transformation", transformation.toString());
        }
        params.put("transformation", transformation);
        signRequest(params, options);
        return buildUrl(cloudinaryApiUrl("download_tag.zip", options), params);
    }

    public String downloadArchive(Map<String, Object> options, String targetFormat) throws UnsupportedEncodingException {
        Map params = Util.buildArchiveParams(options, targetFormat);
        params.put("mode", ArchiveParams.MODE_DOWNLOAD);
        signRequest(params, options);
        return buildUrl(cloudinaryApiUrl("generate_archive", options), params);
    }

    public String downloadArchive(ArchiveParams params) throws UnsupportedEncodingException {
        return downloadArchive(params.toMap(), params.targetFormat());
    }

    public String downloadZip(Map<String, Object> options) throws UnsupportedEncodingException {
        return downloadArchive(options, "zip");
    }

    public String downloadGeneratedSprite(String tag, Map options) throws IOException {
        if (StringUtils.isEmpty(tag)) throw new IllegalArgumentException("Tag cannot be empty");

        if (options == null)
            options = new HashMap();

        options.put("tag", tag);
        options.put("mode", ArchiveParams.MODE_DOWNLOAD);

        Map params = Util.buildGenerateSpriteParams(options);
        signRequest(params, options);

        return buildUrl(cloudinaryApiUrl("sprite", options), params);
    }

    public String downloadGeneratedSprite(String[] urls, Map options) throws IOException {
        if (urls.length < 1) throw new IllegalArgumentException("Request must contain at least one URL.");
        if (options == null)
            options = new HashMap();

        options.put("urls", urls);
        options.put("mode", ArchiveParams.MODE_DOWNLOAD);

        Map params = Util.buildGenerateSpriteParams(options);
        signRequest(params, options);

        return buildUrl(cloudinaryApiUrl("sprite", options), params);
    }

    public String downloadMulti(String tag, Map options) throws IOException {
        if (StringUtils.isEmpty(tag)) throw new IllegalArgumentException("Tag cannot be empty");
        if (options == null)
            options = new HashMap();

        options.put("tag", tag);
        options.put("mode", ArchiveParams.MODE_DOWNLOAD);

        Map params = buildMultiParams(options);
        signRequest(params, options);

        return buildUrl(cloudinaryApiUrl("multi", options), params);
    }

    public String downloadMulti(String[] urls, Map options) throws IOException {
        if (urls.length < 1) throw new IllegalArgumentException("Request must contain at least one URL.");
        if (options == null)
            options = new HashMap();

        options.put("urls", urls);
        options.put("mode", ArchiveParams.MODE_DOWNLOAD);

        Map params = buildMultiParams(options);
        signRequest(params, options);

        return buildUrl(cloudinaryApiUrl("multi", options), params);
    }

    /**
     * Generates URL for executing "Download Folder" operation on Cloudinary site.
     * 
     * @param folderPath path of folder to generate download URL for
     * @param options    optional, holds hints for URL generation procedure, see documentation for full list
     * @return generated URL for downloading specified folder as ZIP archive
     */
    public String downloadFolder(String folderPath, Map options) throws UnsupportedEncodingException {
        if (StringUtils.isEmpty(folderPath)) {
            throw new IllegalArgumentException("Folder path parameter value is required");
        }

        Map adjustedOptions = new HashMap();
        if (options != null) {
            adjustedOptions.putAll(options);
        }

        adjustedOptions.put("prefixes", folderPath);

        final Object resourceType = adjustedOptions.get("resource_type");
        adjustedOptions.put("resource_type", resourceType != null ? resourceType : "all");

        return downloadArchive(adjustedOptions, (String) adjustedOptions.get("target_format"));
    }

    /**
     * Returns an URL of a specific version of a backed up asset that can be used to download that
     * version of the asset (within an hour of the request).
     *
     * @param assetId   The identifier of the uploaded asset.
     * @param versionId The identifier of a backed up version of the asset.
     * @param options   Optional, holds hints for URL generation procedure, see documentation for
     *                  full list
     * @return          The download URL of the asset
     */
    public String downloadBackedupAsset(String assetId, String versionId, Map options) throws UnsupportedEncodingException {
        if (StringUtils.isEmpty(assetId)) {
            throw new IllegalArgumentException("AssetId parameter is required");
        }

        if (StringUtils.isEmpty(versionId)) {
            throw new IllegalArgumentException("VersionId parameter is required");
        }

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("asset_id", assetId);
        params.put("version_id", versionId);
        params.put("timestamp", Util.timestamp());

        signRequest(params, options);
        return buildUrl(cloudinaryApiUrl("download_backup", options), params);
    }

    private String buildUrl(String base, Map<String, Object> params) throws UnsupportedEncodingException {
        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(base);
        if (!params.isEmpty()) {
            urlBuilder.append("?");
        }
        boolean first = true;
        for (Map.Entry<String, Object> param : params.entrySet()) {
            if (param.getValue() == null) continue;

            String keyValue = null;
            Object value = param.getValue();
            if (!first) urlBuilder.append("&");
            if (value instanceof Object[])
                value = Arrays.asList(value);
            if (value instanceof Collection) {
                String key = param.getKey() + "[]=";
                Collection<Object> items = (Collection) value;
                List<String> encodedItems = new ArrayList<String>();
                for (Object item : items)
                    encodedItems.add(URLEncoder.encode(item.toString(), "UTF-8"));
                keyValue = key + StringUtils.join(encodedItems, "&" + key);
            } else {
                keyValue = param.getKey() + "=" +
                        URLEncoder.encode(value.toString(), "UTF-8");
            }
            urlBuilder.append(keyValue);
            first = false;
        }
        return urlBuilder.toString();
    }

    @Deprecated
    public static Map asMap(Object... values) {
        return ObjectUtils.asMap(values);
    }
}
