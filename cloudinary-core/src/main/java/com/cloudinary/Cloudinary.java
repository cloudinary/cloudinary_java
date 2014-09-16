package com.cloudinary;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
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

//import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.ClientConnectionManager;

import com.cloudinary.strategies.AbstractApiStrategy;
import com.cloudinary.strategies.StrategyLoader;
import com.cloudinary.strategies.AbstractUploaderStrategy;
import com.cloudinary.strategies.AbstractUrlBuilderStrategy;
import com.cloudinary.utils.ObjectUtils;
import com.cloudinary.utils.StringUtils;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class Cloudinary {
	
	private static List<String> UPLOAD_STRATEGIES  = new ArrayList<String>(Arrays.asList("com.cloudinary.android.UploaderStrategy","com.cloudinary.http42.UploaderStrategy","com.cloudinary.http43.UploaderStrategy"));
	private static List<String> API_STRATEGIES = new ArrayList<String>(Arrays.asList( "com.cloudinary.android.ApiStrategy", "com.cloudinary.http42.ApiStrategy", "com.cloudinary.http43.ApiStrategy" ));
	private static List<String> URLBUILDER_STRATEGIES = new ArrayList<String>(Arrays.asList( "com.cloudinary.android.UrlBuilderStrategy", "com.cloudinary.http42.UrlBuilderStrategy", "com.cloudinary.http43.UrlBuilderStrategy" ));
	
	public final static String CF_SHARED_CDN = "d3jpl91pxevbkh.cloudfront.net";
	public final static String OLD_AKAMAI_SHARED_CDN = "cloudinary-a.akamaihd.net";
	public final static String AKAMAI_SHARED_CDN = "res.cloudinary.com";
	public final static String SHARED_CDN = AKAMAI_SHARED_CDN;

	public final static String VERSION = "1.0.14";
	public final static String USER_AGENT = "cld-java-" + VERSION;

	private final Map config = new HashMap();
	private AbstractUploaderStrategy uploaderStrategy;
	private AbstractApiStrategy apiStrategy;
	private AbstractUrlBuilderStrategy urlBuilderStrategy;
	protected ClientConnectionManager connectionManager = null;

	public Uploader uploader(){
		return new Uploader(this,uploaderStrategy).withConnectionManager(connectionManager);
		
	};

	public Api api(){
		return new Api(this,apiStrategy).withConnectionManager(connectionManager);
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
	
	public static void registerUrlBuilderStrategy(String className){
		if (!URLBUILDER_STRATEGIES.contains(className)){
			URLBUILDER_STRATEGIES.add(className);
		}
	}
	
	private void loadStrategies() {
		uploaderStrategy= StrategyLoader.find(UPLOAD_STRATEGIES);
		if (uploaderStrategy==null){
			throw new UnknownError("Can't find Cloudinary platform adapter [" + StringUtils.join(UPLOAD_STRATEGIES, ",") + "]");
		}
		
		apiStrategy= StrategyLoader.find(API_STRATEGIES);
		if (apiStrategy==null){
			throw new UnknownError("Can't find Cloudinary platform adapter [" + StringUtils.join(API_STRATEGIES, ",") + "]");
		}
		
		urlBuilderStrategy= StrategyLoader.find(URLBUILDER_STRATEGIES);
		if (urlBuilderStrategy==null){
			throw new UnknownError("Can't find Cloudinary platform adapter [" + StringUtils.join(URLBUILDER_STRATEGIES, ",") + "]");
		}
	}

	public Cloudinary(Map config) {
		loadStrategies();
		this.config.putAll(config);

	}

	public Cloudinary(String cloudinaryUrl) {
		loadStrategies();
		initFromUrl(cloudinaryUrl);
	}

	public Cloudinary() {
		loadStrategies();
		String cloudinaryUrl = System.getProperty("CLOUDINARY_URL", System.getenv("CLOUDINARY_URL"));
		if (cloudinaryUrl != null) {
			initFromUrl(cloudinaryUrl);
		}

	}

	public Url url() {
		return new Url(this);
	}

	public String cloudinaryApiUrl(String action, Map options) {
		String cloudinary = ObjectUtils.asString(options.get("upload_prefix"),
				ObjectUtils.asString(this.config.get("upload_prefix"), "https://api.cloudinary.com"));
		String cloud_name = ObjectUtils.asString(options.get("cloud_name"), ObjectUtils.asString(this.config.get("cloud_name")));
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
				String value = param.getValue().toString();
				if (StringUtils.isNotBlank(value)) {
					params.add(param.getKey() + "=" + value);
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
		String apiKey = ObjectUtils.asString(options.get("api_key"), this.getStringConfig("api_key"));
		if (apiKey == null)
			throw new IllegalArgumentException("Must supply api_key");
		String apiSecret = ObjectUtils.asString(options.get("api_secret"), this.getStringConfig("api_secret"));
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
		AbstractUrlBuilderStrategy builder=  urlBuilderStrategy.init(cloudinaryApiUrl("download", options));
		
		for (Map.Entry<String, Object> param : params.entrySet()) {
			builder.addParam(param.getKey(), param.getValue().toString());
		}
		return builder.url();
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
		AbstractUrlBuilderStrategy builder=  urlBuilderStrategy.init(cloudinaryApiUrl("download_tag.zip", options));
		for (Map.Entry<String, Object> param : params.entrySet()) {
			builder.addParam(param.getKey(), param.getValue().toString());
		}
		return builder.url();
	}

	protected void initFromUrl(String cloudinaryUrl) {
		URI cloudinaryUri = URI.create(cloudinaryUrl);
		setConfig("cloud_name", cloudinaryUri.getHost());
		String[] creds = cloudinaryUri.getUserInfo().split(":");
		setConfig("api_key", creds[0]);
		setConfig("api_secret", creds[1]);
		setConfig("private_cdn", StringUtils.isNotBlank(cloudinaryUri.getPath()));
		setConfig("secure_distribution", cloudinaryUri.getPath());
		if (cloudinaryUri.getQuery() != null) {
			for (String param : cloudinaryUri.getQuery().split("&")) {
				String[] keyValue = param.split("=");
				try {
					setConfig(keyValue[0], URLDecoder.decode(keyValue[1], "ASCII"));
				} catch (UnsupportedEncodingException e) {
					throw new RuntimeException("Unexpected exception", e);
				}
			}
		}
	}

	public boolean getBooleanConfig(String key, boolean default_value) {
		return ObjectUtils.asBoolean(this.config.get(key), default_value);
	}

	public String getStringConfig(String key, String default_value) {
		return ObjectUtils.asString(this.config.get(key), default_value);
	}

	public String getStringConfig(String key) {
		return ObjectUtils.asString(this.config.get(key));
	}

	public void setConfig(String key, Object value) {
		this.config.put(key, value);
	}

	public Cloudinary withConnectionManager(ClientConnectionManager connectionManager) {
		this.connectionManager = connectionManager;
		return this;
	}

}
