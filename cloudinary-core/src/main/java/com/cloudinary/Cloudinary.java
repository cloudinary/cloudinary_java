package com.cloudinary;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.cloudinary.strategies.AbstractApiStrategy;
import com.cloudinary.strategies.AbstractUploaderStrategy;
import com.cloudinary.strategies.StrategyLoader;
import com.cloudinary.utils.ObjectUtils;
import com.cloudinary.utils.StringUtils;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class Cloudinary {

	private static List<String> UPLOAD_STRATEGIES  = new ArrayList<String>(Arrays.asList(
		"com.cloudinary.android.UploaderStrategy",
		"com.cloudinary.http42.UploaderStrategy",
		"com.cloudinary.http43.UploaderStrategy",
		"com.cloudinary.http44.UploaderStrategy"));
	private static List<String> API_STRATEGIES = new ArrayList<String>(Arrays.asList(
		"com.cloudinary.android.ApiStrategy", 
		"com.cloudinary.http42.ApiStrategy", 
		"com.cloudinary.http43.ApiStrategy",
		"com.cloudinary.http44.ApiStrategy" ));

	public final static String CF_SHARED_CDN = "d3jpl91pxevbkh.cloudfront.net";
	public final static String OLD_AKAMAI_SHARED_CDN = "cloudinary-a.akamaihd.net";
	public final static String AKAMAI_SHARED_CDN = "res.cloudinary.com";
	public final static String SHARED_CDN = AKAMAI_SHARED_CDN;

	public final static String VERSION = "1.2.1";
	public final static String USER_AGENT = "cld-java-" + VERSION;

	public final Configuration config;
	private AbstractUploaderStrategy uploaderStrategy;
	private AbstractApiStrategy apiStrategy;

	public Uploader uploader(){
		return new Uploader(this,uploaderStrategy);

	};

	public Api api(){
		return new Api(this,apiStrategy);
	};

	public static void registerUploaderStrategy(String className){
		if (!UPLOAD_STRATEGIES.contains(className)){
			UPLOAD_STRATEGIES.add(className);
		}

	}

	public static void registerAPIStrategy(String className){
		if (!API_STRATEGIES.contains(className)){
			API_STRATEGIES.add(className);
		}
	}

	private void loadStrategies() {
		if (!this.config.loadStrategies) return;
		uploaderStrategy= StrategyLoader.find(UPLOAD_STRATEGIES);

		if (uploaderStrategy==null){
			throw new UnknownError("Can't find Cloudinary platform adapter [" + StringUtils.join(UPLOAD_STRATEGIES, ",") + "]");
		}

		apiStrategy= StrategyLoader.find(API_STRATEGIES);
		if (apiStrategy==null){
			throw new UnknownError("Can't find Cloudinary platform adapter [" + StringUtils.join(API_STRATEGIES, ",") + "]");
		}
	}

	public Cloudinary(Map config) {
		this.config = new Configuration(config);
		loadStrategies();
	}

	public Cloudinary(String cloudinaryUrl) {
		this.config = new Configuration(parseConfigUrl(cloudinaryUrl));
		loadStrategies();
	}

	public Cloudinary() {
		String cloudinaryUrl = System.getProperty("CLOUDINARY_URL", System.getenv("CLOUDINARY_URL"));
		if (cloudinaryUrl != null) {
			this.config = new Configuration(parseConfigUrl(cloudinaryUrl));
		}else {
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
		return StringUtils.join(new String[] { cloudinary, "v1_1", cloud_name, resource_type, action }, "/");
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
		Collection<String> params = new ArrayList<String>();
		for (Map.Entry<String, Object> param : new TreeMap<String, Object>(paramsToSign).entrySet()) {
			if (param.getValue() instanceof Collection) {
				params.add(param.getKey() + "=" + StringUtils.join((Collection) param.getValue(), ","));
			} else {
				if (StringUtils.isNotBlank(param.getValue())) {
					params.add(param.getKey() + "=" + param.getValue().toString());
				}
			}
		}
		String to_sign = StringUtils.join(params, "&");
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("SHA-1");
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("Unexpected exception", e);
		}
		byte[] digest = md.digest((to_sign + apiSecret).getBytes());
		return StringUtils.encodeHexString(digest);
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
		params.put("timestamp", new Long(System.currentTimeMillis() / 1000L).toString());
		signRequest(params, options);
		return buildUrl(cloudinaryApiUrl("download", options), params);
	}

	public String zipDownload(String tag, Map<String, Object> options) throws Exception {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("timestamp", new Long(System.currentTimeMillis() / 1000L).toString());
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
	
	private String buildUrl(String base, Map<String, Object> params) throws UnsupportedEncodingException {
		StringBuilder urlBuilder = new StringBuilder();
		urlBuilder.append(base);
		if (!params.isEmpty()) {
			urlBuilder.append("?");
		}
		boolean first = true;
		for (Map.Entry<String, Object> param : params.entrySet()) {
			if (!first) urlBuilder.append("&");
			urlBuilder.append(param.getKey()).append("=").append(
				URLEncoder.encode(param.getValue().toString(), "UTF-8"));
			first = false;
		}
		return urlBuilder.toString();
	}

	protected Map parseConfigUrl(String cloudinaryUrl) {
		Map params = new HashMap();
		URI cloudinaryUri = URI.create(cloudinaryUrl);
		params.put("cloud_name", cloudinaryUri.getHost());
		if (cloudinaryUri.getUserInfo() != null) {
			String[] creds = cloudinaryUri.getUserInfo().split(":");
			params.put("api_key", creds[0]);
			params.put("api_secret", creds[1]);
		}
		params.put("private_cdn", !StringUtils.isEmpty(cloudinaryUri.getPath()));
		params.put("secure_distribution", cloudinaryUri.getPath());
		if (cloudinaryUri.getQuery() != null) {
			for (String param : cloudinaryUri.getQuery().split("&")) {
				String[] keyValue = param.split("=");
				try {
					params.put(keyValue[0], URLDecoder.decode(keyValue[1], "ASCII"));
				} catch (UnsupportedEncodingException e) {
					throw new RuntimeException("Unexpected exception", e);
				}
			}
		}
		return params;
	}

	@Deprecated
	public static Map asMap(Object... values) {
		return ObjectUtils.asMap(values);
	}
}
