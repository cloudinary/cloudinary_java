package com.cloudinary;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.ClientConnectionManager;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class Cloudinary {
	public final static String CF_SHARED_CDN = "d3jpl91pxevbkh.cloudfront.net";
	public final static String OLD_AKAMAI_SHARED_CDN = "cloudinary-a.akamaihd.net";
	public final static String AKAMAI_SHARED_CDN = "res.cloudinary.com";
	public final static String SHARED_CDN = AKAMAI_SHARED_CDN;

	public final static String VERSION = "1.0.14";
	public final static String USER_AGENT = "cld-java-" + VERSION;

	private final Map config = new HashMap();
	private ClientConnectionManager connectionManager = null;

	public Cloudinary(Map config) {
		this.config.putAll(config);
	}

	public Cloudinary(String cloudinaryUrl) {
		initFromUrl(cloudinaryUrl);
	}

	public Cloudinary() {
		String cloudinaryUrl = System.getProperty("CLOUDINARY_URL", System.getenv("CLOUDINARY_URL"));
		if (cloudinaryUrl != null) {
			initFromUrl(cloudinaryUrl);
		}

	}

	public Url url() {
		return new Url(this);
	}

	public Uploader uploader() {
		return new Uploader(this).withConnectionManager(connectionManager);
	}

	public Api api() {
		return new Api(this).withConnectionManager(connectionManager);
	}

	public String cloudinaryApiUrl(String action, Map options) {
		String cloudinary = asString(options.get("upload_prefix"), asString(this.config.get("upload_prefix"), "https://api.cloudinary.com"));
		String cloud_name = asString(options.get("cloud_name"), asString(this.config.get("cloud_name")));
		if (cloud_name == null)
			throw new IllegalArgumentException("Must supply cloud_name in tag or in configuration");
		String resource_type = asString(options.get("resource_type"), "image");
		return StringUtils.join(new String[] { cloudinary, "v1_1", cloud_name, resource_type, action }, "/");
	}

	private final static SecureRandom RND = new SecureRandom();

	public String randomPublicId() {
		byte[] bytes = new byte[8];
		RND.nextBytes(bytes);
		return Hex.encodeHexString(bytes);
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
		return Hex.encodeHexString(digest);
	}

	public void signRequest(Map<String, Object> params, Map<String, Object> options) {
		String apiKey = Cloudinary.asString(options.get("api_key"), this.getStringConfig("api_key"));
		if (apiKey == null)
			throw new IllegalArgumentException("Must supply api_key");
		String apiSecret = Cloudinary.asString(options.get("api_secret"), this.getStringConfig("api_secret"));
		if (apiSecret == null)
			throw new IllegalArgumentException("Must supply api_secret");
		Util.clearEmpty(params);
		params.put("signature", this.apiSignRequest(params, apiSecret));
		params.put("api_key", apiKey);
	}

	public String privateDownload(String publicId, String format, Map<String, Object> options) throws URISyntaxException {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("public_id", publicId);
		params.put("format", format);
		params.put("attachment", options.get("attachment"));
		params.put("type", options.get("type"));
		params.put("timestamp", new Long(System.currentTimeMillis() / 1000L).toString());
		signRequest(params, options);
		URIBuilder builder = new URIBuilder(cloudinaryApiUrl("download", options));
		for (Map.Entry<String, Object> param : params.entrySet()) {
			builder.addParameter(param.getKey(), param.getValue().toString());
		}
		return builder.toString();
	}

	public String zipDownload(String tag, Map<String, Object> options) throws URISyntaxException {
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
		URIBuilder builder = new URIBuilder(cloudinaryApiUrl("download_tag.zip", options));
		for (Map.Entry<String, Object> param : params.entrySet()) {
			builder.addParameter(param.getKey(), param.getValue().toString());
		}
		return builder.toString();
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
        
        

	public Integer getIntegerConfig(String key, Integer default_value) {
		return asInteger(this.config.get(key), default_value);
	}

	public boolean getBooleanConfig(String key, boolean default_value) {
		return asBoolean(this.config.get(key), default_value);
	}

	public String getStringConfig(String key, String default_value) {
		return asString(this.config.get(key), default_value);
	}

	public String getStringConfig(String key) {
		return asString(this.config.get(key));
	}

	public void setConfig(String key, Object value) {
		this.config.put(key, value);
	}
	
	public Cloudinary withConnectionManager(ClientConnectionManager connectionManager) {
		this.connectionManager = connectionManager;
		return this;
	}

	public static String asString(Object value) {
		if (value == null) {
			return null;
		} else {
			return value.toString();
		}
	}

	public static String asString(Object value, String defaultValue) {
		if (value == null) {
			return defaultValue;
		} else {
			return value.toString();
		}
	}

	public static List asArray(Object value) {
		if (value == null) {
			return Collections.EMPTY_LIST;
		} else if (value instanceof int[]) {
			List array = new ArrayList();
			for (int i : (int[]) value) {
				array.add(new Integer(i));
			}
			return array;
		} else if (value instanceof Object[]) {
			return Arrays.asList((Object[]) value);
		} else if (value instanceof List) {
			return (List) value;
		} else {
			List array = new ArrayList();
			array.add(value);
			return array;
		}
	}

	public static Boolean asBoolean(Object value, Boolean defaultValue) {
		if (value == null) {
			return defaultValue;
		} else if (value instanceof Boolean) {
			return (Boolean) value;
		} else {
			return "true".equals(value);
		}
	}

	public static Integer asInteger(Object value, Integer defaultValue) {
		if (value == null) {
			return defaultValue;
		} else if (value instanceof Integer) {
			return (Integer) value;
		} else {
			return Integer.parseInt(value.toString());
		}
	}

	public static Float asFloat(Object value) {
		if (value == null) {
			return null;
		} else if (value instanceof Float) {
			return (Float) value;
		} else {
			return Float.parseFloat(value.toString());
		}
	}

	public static Map asMap(Object... values) {
		if (values.length % 2 != 0)
			throw new RuntimeException("Usage - (key, value, key, value, ...)");
		Map result = new HashMap(values.length / 2);
		for (int i = 0; i < values.length; i += 2) {
			result.put(values[i], values[i + 1]);
		}
		return result;
	}

	public static Map emptyMap() {
		return Collections.EMPTY_MAP;
	}

	public static String encodeMap(Object arg) {
		if (arg != null && arg instanceof Map) {
			Map<String, String> mapArg = (Map<String, String>) arg;
			HashSet out = new HashSet();
			for (Map.Entry<String, String> entry : mapArg.entrySet()) {
				out.add(entry.getKey() + "=" + entry.getValue());
			}
			return StringUtils.join(out.toArray(), "|");
		} else if (arg == null) {
			return null;
		} else {
			return arg.toString();
		}
	}

	public static Map<String, ? extends Object> only(Map<String, ? extends Object> hash, String... keys) {
        Map<String, Object> result = new HashMap<String, Object>();
        for (String key : keys) {
            if (hash.containsKey(key)) {
                result.put(key, hash.get(key));
            }
        }
        return result;
    }
	
}
