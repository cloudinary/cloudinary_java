package com.cloudinary;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.TreeMap;
import java.util.zip.CRC32;

import com.cloudinary.utils.Base64Coder;
import com.cloudinary.utils.ObjectUtils;
import com.cloudinary.utils.StringUtils;

public class Url {
	String cloudName;
	boolean secure;
	boolean privateCdn;
	String secureDistribution;
	boolean cdnSubdomain;
	boolean shorten;
	boolean signUrl;
	String cname;
	String type = "upload";
	String resourceType = "image";
	String format = null;
	String version = null;
	String source = null;
	String apiSecret = null;
	Transformation transformation = null;
	private static final String CL_BLANK = "data:image/gif;base64,R0lGODlhAQABAIAAAAAAAP///yH5BAEAAAAALAAAAAABAAEAAAIBRAA7";

	public Url(Cloudinary cloudinary) {
		this.cloudName = cloudinary.getStringConfig("cloud_name");
		this.secureDistribution = cloudinary.getStringConfig("secure_distribution");
		this.cname = cloudinary.getStringConfig("cname");
		this.secure = cloudinary.getBooleanConfig("secure", false);
		this.privateCdn = cloudinary.getBooleanConfig("private_cdn", false);
		this.cdnSubdomain = cloudinary.getBooleanConfig("cdn_subdomain", false);
		this.shorten = cloudinary.getBooleanConfig("shorten", false);
		this.signUrl = cloudinary.getBooleanConfig("sign_url", false);
		this.apiSecret = cloudinary.getStringConfig("api_secret");
	}

	public Url type(String type) {
		this.type = type;
		return this;
	}

	@Deprecated
	public Url resourcType(String resourceType) {
		return resourceType(resourceType);
	}

	public Url resourceType(String resourceType) {
		this.resourceType = resourceType;
		return this;
	}

	public Url format(String format) {
		this.format = format;
		return this;
	}

	public Url cloudName(String cloudName) {
		this.cloudName = cloudName;
		return this;
	}

	public Url secureDistribution(String secureDistribution) {
		this.secureDistribution = secureDistribution;
		return this;
	}

	public Url cname(String cname) {
		this.cname = cname;
		return this;
	}

	public Url version(Object version) {
		this.version = ObjectUtils.asString(version);
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

	public Url privateCdn(boolean privateCdn) {
		this.privateCdn = privateCdn;
		return this;
	}

	public Url cdnSubdomain(boolean cdnSubdomain) {
		this.cdnSubdomain = cdnSubdomain;
		return this;
	}

	public Url shorten(boolean shorten) {
		this.shorten = shorten;
		return this;
	}

	public Url source(String source) {
		this.source = source;
		return this;
	}

	public Url source(StoredFile source) {
		if (source.getResourceType() != null)
			this.resourceType = source.getResourceType();
		if (source.getType() != null)
			this.type = source.getType();
		if (source.getVersion() != null)
			this.version = source.getVersion().toString();
		this.format = source.getFormat();
		this.source = source.getPublicId();
		return this;
	}

	public Transformation transformation() {
		if (this.transformation == null)
			this.transformation = new Transformation();
		return this.transformation;
	}

	public Url signed(boolean signUrl) {
		this.signUrl = signUrl;
		return this;
	}

	public String generate(String source) {
		this.source = source;
		return this.generate();
	}

	public String generate() {
		if (type.equals("fetch") && StringUtils.isNotBlank(format)) {
			transformation().fetchFormat(format);
			this.format = null;
		}
		String transformationStr = transformation().generate();
		if (StringUtils.isBlank(this.cloudName)) {
			throw new IllegalArgumentException("Must supply cloud_name in tag or in configuration");
		}

		if (source == null)
			return null;
		String original_source = source;

		if (source.toLowerCase().matches("^https?:/.*")) {
			if ("upload".equals(type) || "asset".equals(type)) {
				return original_source;
			}
			source = SmartUrlEncoder.encode(source);
		} else {
			try {
				source = SmartUrlEncoder.encode(URLDecoder.decode(source.replace("+", "%2B"), "UTF-8"));
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException(e);
			}
			if (format != null)
				source = source + "." + format;
		}
		String prefix;
		boolean sharedDomain = !privateCdn;
		if (secure) {
			if (StringUtils.isBlank(secureDistribution) || Cloudinary.OLD_AKAMAI_SHARED_CDN.equals(secureDistribution)) {
				secureDistribution = privateCdn ? cloudName + "-res.cloudinary.com" : Cloudinary.SHARED_CDN;
			}
			sharedDomain = sharedDomain || Cloudinary.SHARED_CDN.equals(secureDistribution);
			prefix = "https://" + secureDistribution;
		} else {
			CRC32 crc32 = new CRC32();
			crc32.update(source.getBytes());
			String subdomain = cdnSubdomain ? "a" + ((crc32.getValue() % 5 + 5) % 5 + 1) + "." : "";
			String host = cname != null ? cname : (privateCdn ? cloudName + "-" : "") + "res.cloudinary.com";
			prefix = "http://" + subdomain + host;
		}
		if (sharedDomain)
			prefix = prefix + "/" + cloudName;

		if (shorten && resourceType.equals("image") && type.equals("upload")) {
			resourceType = "iu";
			type = "";
		}

		if (source.contains("/") && !source.matches("v[0-9]+.*") && !source.matches("https?:/.*") && StringUtils.isBlank(version)) {
			version = "1";
		}

		if (version != null)
			version = "v" + version;

		String rest = StringUtils.join(new String[] { transformationStr, version, source }, "/");
		rest = rest.replaceAll("^/+", "").replaceAll("([^:])\\/+", "$1/");

		if (signUrl) {
			MessageDigest md = null;
			try {
				md = MessageDigest.getInstance("SHA-1");
			} catch (NoSuchAlgorithmException e) {
				throw new RuntimeException("Unexpected exception", e);
			}
			byte[] digest = md.digest((rest + apiSecret).getBytes());
			String signature = Base64Coder.encodeURLSafeString(digest);
			rest = "s--" + signature.substring(0, 8) + "--/" + rest;
		}

		return StringUtils.join(new String[] { prefix, resourceType, type, rest }, "/").replaceAll("([^:])\\/+", "$1/");
	}

	public String generateSpriteCss(String source) {
		this.type = "sprite";
		if (!source.endsWith(".css"))
			this.format = "css";
		return generate(source);
	}

	@SuppressWarnings("unchecked")
	public String imageTag(String source) {
		return imageTag(source, ObjectUtils.emptyMap());
	}

	public String imageTag(String source, Map<String, String> attributes) {
		this.source = source;
		return imageTag(attributes);
	}

	@SuppressWarnings("unchecked")
	public String imageTag() {
		return imageTag(ObjectUtils.emptyMap());
	}

	@SuppressWarnings("unchecked")
	public String imageTag(StoredFile source) {
		return imageTag(source, ObjectUtils.emptyMap());
	}

	public String imageTag(StoredFile source, Map<String, String> attributes) {
		source(source);
		return imageTag(attributes);
	}

	public String imageTag(Map<String, String> attributes) {
		String url = generate();
		attributes = new TreeMap<String, String>(attributes); // Make sure they
																// are ordered.
		if (transformation().getHtmlHeight() != null)
			attributes.put("height", transformation().getHtmlHeight());
		if (transformation().getHtmlWidth() != null)
			attributes.put("width", transformation().getHtmlWidth());

		boolean hiDPI = transformation().isHiDPI();
		boolean responsive = transformation().isResponsive();

		if (hiDPI || responsive) {
			attributes.put("data-src", url);
			String extraClass = responsive ? "cld-responsive" : "cld-hidpi";
			attributes.put("class", (StringUtils.isBlank(attributes.get("class")) ? "" : attributes.get("class") + " ") + extraClass);
			String responsivePlaceholder = attributes.remove("responsive_placeholder");
			if ("blank".equals(responsivePlaceholder)) {
				responsivePlaceholder = CL_BLANK;
			}
			url = responsivePlaceholder;
		}

		StringBuilder builder = new StringBuilder();
		builder.append("<img");
		if (url != null) {
			builder.append(" src='").append(url).append("'");
		}
		for (Map.Entry<String, String> attr : attributes.entrySet()) {
			builder.append(" ").append(attr.getKey()).append("='").append(attr.getValue()).append("'");
		}
		builder.append("/>");
		return builder.toString();
	}
}
