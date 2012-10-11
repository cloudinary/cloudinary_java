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
	
	public Cloudinary(String cloudinary_url) {
		init_from_url(cloudinary_url);
	}

	public Cloudinary() {
		String cloudinary_url = System.getProperty("CLOUDINARY_URL", System.getenv("CLOUDINARY_URL"));
		if (cloudinary_url != null) {
			init_from_url(cloudinary_url);
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
	
	public String cloudinary_api_url(String action, Map options) {
        String cloudinary = as_string(options.get("upload_prefix"), as_string(this.config.get("upload_prefix"), "https://api.cloudinary.com"));
        String cloud_name = as_string(options.get("cloud_name"), as_string(this.config.get("cloud_name")));
        if (cloud_name == null) throw new IllegalArgumentException("Must supply cloud_name in tag or in configuration");
        String resource_type = as_string(options.get("resource_type"), "image"); 
        return StringUtils.join(new String[]{cloudinary, "v1_1", cloud_name, resource_type, action}, "/");
    }

    private final static SecureRandom RND = new SecureRandom();
    
    public String random_public_id() {
    	byte[] bytes = new byte[8];
        RND.nextBytes(bytes);
        return Hex.encodeHexString(bytes);
    }

    public String signed_preloaded_image(Map result) {
        return result.get("resource_type") + "/upload/v" + result.get("version") + "/" + result.get("public_id") +
               (result.containsKey("format") ? "." + result.get("format") : "") + "#" + result.get("signature");
    }

    public String api_sign_request(Map<String, String> params_to_sign, String api_secret) {
    	Collection<String> params = new ArrayList<String>();
        for (Map.Entry<String, String> param : new TreeMap<String, String>(params_to_sign).entrySet()) {
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
        byte[] digest = md.digest((to_sign + api_secret).getBytes());
        return Hex.encodeHexString(digest);
    }
	
	protected void init_from_url(String cloudinary_url) {
		URI cloudinary_uri = URI.create(cloudinary_url);
		setConfig("cloud_name", cloudinary_uri.getHost());
		String[] creds = cloudinary_uri.getUserInfo().split(":");
		setConfig("api_key", creds[0]);
		setConfig("api_secret", creds[1]);
		setConfig("private_cdn", StringUtils.isNotBlank(cloudinary_uri.getPath()));
		setConfig("secure_distribution", cloudinary_uri.getPath());
		if (cloudinary_uri.getQuery() != null) {
			for (String param : cloudinary_uri.getQuery().split("&")) {
				String[] key_value = param.split("=");
				try {
					setConfig(key_value[0], URLDecoder.decode(key_value[1], "ASCII"));
				} catch (UnsupportedEncodingException e) {
					throw new RuntimeException("Unexpected exception", e);
				}
			}
		}
	}

	public boolean getBooleanConfig(String key, boolean default_value) {
		return as_bool(this.config.get(key), default_value);
	}

	public String getStringConfig(String key, String default_value) {
		return as_string(this.config.get(key), default_value);
	}

	public String getStringConfig(String key) {
		return as_string(this.config.get(key));
	}

	public void setConfig(String key, Object value) {
		this.config.put(key, value);
	}

	public static String as_string(Object value) {
		if (value == null) {
			return null;
		} else {
			return value.toString();
		}
	}

	public static String as_string(Object value, String default_value) {
		if (value == null) {
			return default_value;
		} else {
			return value.toString();
		}
	}

	public static List as_array(Object value) {
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

	public static Boolean as_bool(Object value, Boolean default_value) {
		if (value == null) {
			return default_value;
		} else if (value instanceof Boolean) {
			return (Boolean) value;
		} else {
			return "true".equals(value);
		}
	}
}
