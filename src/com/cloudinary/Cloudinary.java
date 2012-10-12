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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang.StringUtils;

@SuppressWarnings({"rawtypes", "unchecked"})
public class Cloudinary {
	public final static String SHARED_CDN = "d3jpl91pxevbkh.cloudfront.net";

	private final Map config = new HashMap();
	
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
		return new Uploader(this);
	}

	public Api api() {
		return new Api(this);
	}
	
	public String cloudinaryApiUrl(String action, Map options) {
        String cloudinary = asString(options.get("upload_prefix"), asString(this.config.get("upload_prefix"), "https://api.cloudinary.com"));
        String cloud_name = asString(options.get("cloud_name"), asString(this.config.get("cloud_name")));
        if (cloud_name == null) throw new IllegalArgumentException("Must supply cloud_name in tag or in configuration");
        String resource_type = asString(options.get("resource_type"), "image"); 
        return StringUtils.join(new String[]{cloudinary, "v1_1", cloud_name, resource_type, action}, "/");
    }

    private final static SecureRandom RND = new SecureRandom();
    
    public String randomPublicId() {
    	byte[] bytes = new byte[8];
        RND.nextBytes(bytes);
        return Hex.encodeHexString(bytes);
    }

    public String signedPreloadedImage(Map result) {
        return result.get("resource_type") + "/upload/v" + result.get("version") + "/" + result.get("public_id") +
               (result.containsKey("format") ? "." + result.get("format") : "") + "#" + result.get("signature");
    }

    public String apiSignRequest(Map<String, String> paramsToSign, String apiSecret) {
    	Collection<String> params = new ArrayList<String>();
        for (Map.Entry<String, String> param : new TreeMap<String, String>(paramsToSign).entrySet()) {
            if (StringUtils.isNotBlank(param.getValue())) {
                params.add(param.getKey() + "=" + param.getValue());
            }
        }
        String to_sign = StringUtils.join(params, "&");
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA-1");
        }
        catch(NoSuchAlgorithmException e) {
            throw new RuntimeException("Unexpected exception", e);
        } 
        byte[] digest = md.digest((to_sign + apiSecret).getBytes());
        return Hex.encodeHexString(digest);
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
		} else if (value instanceof Object[]){
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
	
	public static Map asMap(Object...values) {
		if (values.length % 2 != 0) throw new RuntimeException("Usage - (key, value, key, value, ...)");
		Map result = new HashMap();
		for (int i = 0; i < values.length; i+=2) {
			result.put(values[i], values[i+1]);
		}
		return result;
	}
	
	public static Map emptyMap() {
		return Collections.EMPTY_MAP;
	}
}
