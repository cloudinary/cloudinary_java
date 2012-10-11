package com.cloudinary;

import java.util.HashMap;
import java.util.Map;
import java.util.zip.CRC32;

import org.apache.commons.lang.StringUtils;

public class Url {
	String cloud_name;
	boolean secure;
	boolean private_cdn;
	String secure_distribution;
	boolean cdn_subdomain;
	String cname;
	String type = "upload"; 
	String resource_type = "image";
	String format = null;
	String version = null;	
	Transformation transformation = null;

	public Url(Cloudinary cloudinary) {
		this.cloud_name = cloudinary.getStringConfig("cloud_name");
		this.secure_distribution = cloudinary.getStringConfig("secure_distribution");
		this.cname = cloudinary.getStringConfig("cname");
		this.secure = cloudinary.getBooleanConfig("secure", false);
		this.private_cdn = cloudinary.getBooleanConfig("private_cdn", false);
		this.cdn_subdomain = cloudinary.getBooleanConfig("cdn_subdomain", false);
	}
	
	public Url type(String type) {
		this.type = type;
		return this;
	}

	public Url resource_type(String resource_type) {
		this.resource_type = resource_type;
		return this;
	}

	public Url format(String format) {
		this.format = format;
		return this;
	}

	public Url cloud_name(String cloud_name) {
		this.cloud_name = cloud_name;
		return this;
	}

	public Url secure_distribution(String secure_distribution) {
		this.secure_distribution = secure_distribution;
		return this;
	}

	public Url cname(String cname) {
		this.cname = cname;
		return this;
	}

	public Url version(Object version) {
		this.version = Cloudinary.as_string(version);
		return this;
	}
	
	public Url transformation(Transformation transformation) {
		this.transformation = transformation;
		return this;
	}

	public Url secure(boolean secure) {
		this.secure = secure;
		return this;
	}

	public Url private_cdn(boolean private_cdn) {
		this.private_cdn = private_cdn;
		return this;
	}

	public Url cdn_subdomain(boolean cdn_subdomain) {
		this.cdn_subdomain = cdn_subdomain;
		return this;
	}

	public Transformation transformation() {
		if (this.transformation == null) this.transformation = new Transformation();
		return this.transformation;
	}

	public String generate(String source) {
		if (type.equals("fetch") && StringUtils.isNotBlank(format)) {
			transformation().fetch_format(format);
			this.format = null;
		}
		String transformation_str = transformation().generate();
		if (StringUtils.isBlank(this.cloud_name)) {
			throw new IllegalArgumentException("Must supply cloud_name in tag or in configuration");
		}

        if (source == null) return null;
        String original_source = source;

        if (source.toLowerCase().matches("^https?:/.*")) {
          if ("upload".equals(type) || "asset".equals(type)) {
        	  return original_source;
          }
          source = SmartUrlEncoder.encode(source);
        } else if (format != null) {
          source = source + "." + format;
        }            
        if (secure && StringUtils.isBlank(secure_distribution)) {
            if (private_cdn) {
                throw new IllegalArgumentException("secure_distribution not defined");
            } else {
                secure_distribution = Cloudinary.SHARED_CDN;
            }
        } 
        String prefix;
        if (secure) {
            prefix = "https://" + secure_distribution;
        } else {
        	CRC32 crc32 = new CRC32();
        	crc32.update(source.getBytes());
            String subdomain = cdn_subdomain ? "a" + ((crc32.getValue() % 5 + 5) % 5 + 1) + "." : "";
            String host = cname != null ? cname : (private_cdn ? cloud_name + "-" : "") + "res.cloudinary.com";
            prefix = "http://" + subdomain + host;
        }
        if (!private_cdn) prefix = prefix + "/" + cloud_name;
        if (version != null) version = "v" + version;
        
        return StringUtils.join(new String[]{prefix, resource_type, type, transformation_str, version, source}, "/").replaceAll("([^:])\\/+", "$1/");
	}

	public String image_tag(String source) {
		return image_tag(source, new HashMap<String, String>());
	}

	public String image_tag(String source, Map<String, String> attributes) {
		String url = generate(source);
		if (transformation().getHtmlHeight() != null) attributes.put("height", transformation().getHtmlHeight());
		if (transformation().getHtmlWidth() != null) attributes.put("width", transformation().getHtmlWidth());
		StringBuilder builder = new StringBuilder();
		builder.append("<img src='").append(url).append("'");
		for (Map.Entry<String, String> attr : attributes.entrySet()) {
			builder.append(" ").append(attr.getKey()).append("='").append(attr.getValue()).append("'");
		}
		builder.append("/>");
		return builder.toString();
	}
}
