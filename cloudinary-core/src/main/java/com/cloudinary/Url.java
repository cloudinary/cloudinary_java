package com.cloudinary;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.CRC32;

import com.cloudinary.utils.Base64Coder;
import com.cloudinary.utils.ObjectUtils;
import com.cloudinary.utils.StringUtils;

public class Url {
    private final Cloudinary cloudinary;
    private final Configuration config;
    String publicId = null;
    String type = null;
    String resourceType = null;
    String format = null;
    String version = null;
    Transformation transformation = null;
    boolean signUrl;
    String source = null;
    private String urlSuffix;
    private Boolean useRootPath;
    Map<String, Transformation> sourceTransformation = null;
    String[] sourceTypes = null;
    String fallbackContent = null;
    Transformation posterTransformation = null;
    String posterSource = null;
    Url posterUrl = null;

    private static final String CL_BLANK = "data:image/gif;base64,R0lGODlhAQABAIAAAAAAAP///yH5BAEAAAAALAAAAAABAAEAAAIBRAA7";
    public static final String[] DEFAULT_VIDEO_SOURCE_TYPES = {"webm", "mp4", "ogv"};
    private static final Pattern VIDEO_EXTENSION_RE = Pattern.compile("\\.(" + StringUtils.join(DEFAULT_VIDEO_SOURCE_TYPES, "|") + ")$");

    public Url(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
        this.config = new Configuration(cloudinary.config);
    }

    public Url clone() {
        Url cloned = cloudinary.url();

        cloned.fallbackContent = this.fallbackContent;
        cloned.format = this.format;
        cloned.posterSource = this.posterSource;
        if (this.posterTransformation != null) cloned.posterTransformation = new Transformation(this.posterTransformation);
        if (this.posterUrl != null) cloned.posterUrl = this.posterUrl.clone();
        cloned.publicId = this.publicId;
        cloned.resourceType = this.resourceType;
        cloned.signUrl = this.signUrl;
        cloned.source = this.source;
        if (this.transformation != null) cloned.transformation = new Transformation(this.transformation);
        if (this.sourceTransformation != null) {
            cloned.sourceTransformation = new HashMap<String, Transformation>();
            for (Map.Entry<String, Transformation> keyValuePair : this.sourceTransformation.entrySet()) {
                cloned.sourceTransformation.put(keyValuePair.getKey(), keyValuePair.getValue());
            }
        }
        cloned.sourceTypes = this.sourceTypes;
        cloned.urlSuffix = this.urlSuffix;
        cloned.useRootPath = this.useRootPath;

        return cloned;
    }

    private static Pattern identifierPattern = Pattern.compile("^(?:([^/]+)/)??(?:([^/]+)/)??(?:v(\\d+)/)?" + "(?:([^#/]+?)(?:\\.([^.#/]+))?)(?:#([^/]+))?$");

    /**
     * Parses a cloudinary identifier of the form:<br>
     * {@code [<resource_type>/][<image_type>/][v<version>/]<public_id>[.<format>][#<signature>]}
     */
    public Url fromIdentifier(String identifier) {
        Matcher matcher = identifierPattern.matcher(identifier);
        if (!matcher.matches()) {
            throw new RuntimeException(String.format("Couldn't parse identifier %s", identifier));
        }

        String resourceType = matcher.group(1);
        if (resourceType != null) {
            resourceType(resourceType);
        }

        String type = matcher.group(2);
        if (type != null) {
            type(type);
        }

        String version = matcher.group(3);
        if (version != null) {
            version(version);
        }

        String publicId = matcher.group(4);
        if (publicId != null) {
            publicId(publicId);
        }

        String format = matcher.group(5);
        if (format != null) {
            format(format);
        }

        // Signature (group 6) is not used

        return this;
    }

    public Url type(String type) {
        this.type = type;
        return this;
    }

    public Url resourcType(String resourceType) {
        return resourceType(resourceType);
    }

    public Url resourceType(String resourceType) {
        this.resourceType = resourceType;
        return this;
    }

    public Url publicId(Object publicId) {
        this.publicId = ObjectUtils.asString(publicId);
        return this;
    }

    public Url format(String format) {
        this.format = format;
        return this;
    }

    public Url cloudName(String cloudName) {
        this.config.cloudName = cloudName;
        return this;
    }

    public Url secureDistribution(String secureDistribution) {
        this.config.secureDistribution = secureDistribution;
        return this;
    }

    public Url secureCdnSubdomain(boolean secureCdnSubdomain) {
        this.config.secureCdnSubdomain = secureCdnSubdomain;
        return this;
    }

    public Url suffix(String urlSuffix) {
        this.urlSuffix = urlSuffix;
        return this;
    }

    public Url useRootPath(boolean useRootPath) {
        this.useRootPath = useRootPath;
        return this;
    }

    public Url cname(String cname) {
        this.config.cname = cname;
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
        this.config.secure = secure;
        return this;
    }

    public Url privateCdn(boolean privateCdn) {
        this.config.privateCdn = privateCdn;
        return this;
    }

    public Url cdnSubdomain(boolean cdnSubdomain) {
        this.config.cdnSubdomain = cdnSubdomain;
        return this;
    }

    public Url shorten(boolean shorten) {
        this.config.shorten = shorten;
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

    public Url sourceTransformation(Map<String, Transformation> sourceTransformation) {
        this.sourceTransformation = sourceTransformation;
        return this;
    }

    public Url sourceTransformationFor(String source, Transformation transformation) {
        if (this.sourceTransformation == null) {
            this.sourceTransformation = new HashMap<String, Transformation>();
        }
        this.sourceTransformation.put(source, transformation);
        return this;
    }

    public Url sourceTypes(String[] sourceTypes) {
        this.sourceTypes = sourceTypes;
        return this;
    }

    public Url fallbackContent(String fallbackContent) {
        this.fallbackContent = fallbackContent;
        return this;
    }

    public Url posterTransformation(Transformation posterTransformation) {
        this.posterTransformation = posterTransformation;
        return this;
    }

    @SuppressWarnings("rawtypes")
    public Url posterTransformation(List<Map> posterTransformations) {
        this.posterTransformation = new Transformation(posterTransformations);
        return this;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public Url posterTransformation(Map posterTransformations) {
        List<Map> transformations = new ArrayList<Map>();
        Map copy = new HashMap();
        copy.putAll(posterTransformations);
        transformations.add(copy);
        this.posterTransformation = new Transformation(transformations);
        return this;
    }

    public Url posterSource(String posterSource) {
        this.posterSource = posterSource;
        return this;
    }

    public Url posterUrl(Url posterUrl) {
        this.posterUrl = posterUrl;
        return this;
    }

    public Url poster(Object poster) {
        if (poster instanceof Transformation) {
            return posterTransformation((Transformation) poster);
        } else if (poster instanceof List) {
            return posterTransformation((List) poster);
        } else if (poster instanceof Map) {
            return posterTransformation((Map) poster);
        } else if (poster instanceof Url) {
            return posterUrl((Url) poster);
        } else if (poster instanceof String) {
            return posterSource((String) poster);
        } else if (poster == null || poster.equals(Boolean.FALSE)) {
            return posterSource("");
        } else {
            throw new IllegalArgumentException("Illegal value type supplied to poster. must be one of: <Transformation>, <List<Map>>, <Map>, <Url>, <String>");
        }
    }

    public String generate() {
        return generate(null);
    }

    public String generate(String source) {

        boolean useRootPath = this.config.useRootPath;
        if (this.useRootPath != null) {
            useRootPath = this.useRootPath;
        }

        if (StringUtils.isEmpty(this.config.cloudName)) {
            throw new IllegalArgumentException("Must supply cloud_name in tag or in configuration");
        }

        if (!this.config.privateCdn) {
            if (StringUtils.isNotBlank(urlSuffix)) {
                throw new IllegalArgumentException("URL Suffix only supported in private CDN");
            }
            if (useRootPath) {
                throw new IllegalArgumentException("Root path only supported in private CDN");
            }
        }

        if (source == null) {
            if (publicId == null) {
                if (this.source == null) {
                    return null;
                }
                source = this.source;
            } else {
                source = publicId;
            }
        }

        if (source.toLowerCase(Locale.US).matches("^https?:/.*")) {
            if (StringUtils.isEmpty(type) || "asset".equals(type)) {
                return source;
            }
        }


        if (type != null && type.equals("fetch") && !StringUtils.isEmpty(format)) {
            transformation().fetchFormat(format);
            this.format = null;
        }
        String transformationStr = transformation().generate();
        String signature = "";


        String[] finalizedSource = finalizeSource(source, format, urlSuffix);
        source = finalizedSource[0];
        String sourceToSign = finalizedSource[1];

        if (sourceToSign.contains("/") && !sourceToSign.matches("v[0-9]+.*") && !sourceToSign.matches("https?:/.*") && StringUtils.isEmpty(version)) {
            version = "1";
        }

        if (version == null)
            version = "";
        else
            version = "v" + version;


        if (signUrl) {
            MessageDigest md = null;
            try {
                md = MessageDigest.getInstance("SHA-1");
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException("Unexpected exception", e);
            }

            String toSign = StringUtils.join(new String[]{transformationStr, sourceToSign}, "/");
            toSign = toSign.replaceAll("^/+", "").replaceAll("([^:])\\/+", "$1/");

            byte[] digest = md.digest(cloudinary.getUTF8Bytes(toSign + this.config.apiSecret));
            signature = Base64Coder.encodeURLSafeString(digest);
            signature = "s--" + signature.substring(0, 8) + "--";
        }

        String resourceType = this.resourceType;
        if (resourceType == null) resourceType = "image";
        String finalResourceType = finalizeResourceType(resourceType, type, urlSuffix, useRootPath, config.shorten);
        String prefix = unsignedDownloadUrlPrefix(source, config.cloudName, config.privateCdn, config.cdnSubdomain, config.secureCdnSubdomain, config.cname, config.secure, config.secureDistribution);

        return StringUtils.join(new String[]{prefix, finalResourceType, signature, transformationStr, version, source}, "/").replaceAll("([^:])\\/+", "$1/");
    }

    private String[] finalizeSource(String source, String format, String urlSuffix) {
        String[] result = new String[2];
        source = source.replaceAll("([^:])//", "\1/");

        String sourceToSign;
        if (source.toLowerCase().matches("^https?:/.*")) {
            source = SmartUrlEncoder.encode(source);
            sourceToSign = source;
        } else {
            try {
                source = SmartUrlEncoder.encode(URLDecoder.decode(source.replace("+", "%2B"), "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
            sourceToSign = source;
            if (StringUtils.isNotBlank(urlSuffix)) {
                Pattern pattern = Pattern.compile("[\\./]");
                Matcher matcher = pattern.matcher(urlSuffix);
                if (matcher.find()) {
                    throw new IllegalArgumentException("url_suffix should not include . or /");
                }
                source = source + "/" + urlSuffix;
            }
            if (StringUtils.isNotBlank(format)) {
                source = source + "." + format;
                sourceToSign = sourceToSign + "." + format;
            }
        }
        result[0] = source;
        result[1] = sourceToSign;
        return result;
    }

    public String finalizeResourceType(String resourceType, String type, String urlSuffix, boolean useRootPath, boolean shorten) {
        if (type == null) {
            type = "upload";
        }
        if (!StringUtils.isBlank(urlSuffix)) {
            if (resourceType.equals("image") && type.equals("upload")) {
                resourceType = "images";
                type = null;
            } else if (resourceType.equals("raw") && type.equals("upload")) {
                resourceType = "files";
                type = null;
            } else {
                throw new IllegalArgumentException("URL Suffix only supported for image/upload and raw/upload");
            }
        }
        if (useRootPath) {
            if ((resourceType.equals("image") && type.equals("upload")) || (resourceType.equals("images") && StringUtils.isBlank(type))) {
                resourceType = null;
                type = null;
            } else {
                throw new IllegalArgumentException("Root path only supported for image/upload");
            }
        }
        if (shorten && resourceType.equals("image") && type.equals("upload")) {
            resourceType = "iu";
            type = null;
        }
        String result = resourceType;
        if (type != null) {
            result += "/" + type;
        }
        return result;
    }

    public String unsignedDownloadUrlPrefix(String source, String cloudName, boolean privateCdn, boolean cdnSubdomain, Boolean secureCdnSubdomain, String cname, boolean secure, String secureDistribution) {
        if (this.config.cloudName.startsWith("/")) {
            return "/res" + this.config.cloudName;
        }
        boolean sharedDomain = !this.config.privateCdn;

        String prefix;

        if (this.config.secure) {
            if (StringUtils.isEmpty(this.config.secureDistribution) || this.config.secureDistribution.equals(Cloudinary.OLD_AKAMAI_SHARED_CDN)) {
                secureDistribution = this.config.privateCdn ? this.config.cloudName + "-res.cloudinary.com" : Cloudinary.SHARED_CDN;
            }
            if (!sharedDomain) {
                sharedDomain = secureDistribution.equals(Cloudinary.SHARED_CDN);
            }

            if (secureCdnSubdomain == null && sharedDomain) {
                secureCdnSubdomain = this.config.cdnSubdomain;
            }

            if (secureCdnSubdomain != null && secureCdnSubdomain == true) {
                secureDistribution = this.config.secureDistribution.replace("res.cloudinary.com", "res-" + shard(source) + ".cloudinary.com");
            }

            prefix = "https://" + secureDistribution;
        } else if (StringUtils.isNotBlank(this.config.cname)) {
            String subdomain = this.config.cdnSubdomain ? "a" + shard(source) + "." : "";
            prefix = "http://" + subdomain + this.config.cname;
        } else {
            String protocol = "http://";
            cloudName = this.config.privateCdn ? this.config.cloudName + "-" : "";
            String res = "res";
            String subdomain = this.config.cdnSubdomain ? "-" + shard(source) : "";
            String domain = ".cloudinary.com";
            prefix = StringUtils.join(new String[]{protocol, cloudName, res, subdomain, domain}, "");
        }
        if (sharedDomain) {
            prefix += "/" + this.config.cloudName;
        }
        return prefix;
    }

    private String shard(String input) {
        CRC32 crc32 = new CRC32();
        crc32.update(cloudinary.getUTF8Bytes(input));
        return String.valueOf((crc32.getValue() % 5 + 5) % 5 + 1);
    }

    @SuppressWarnings("unchecked")
    public String imageTag(String source) {
        return imageTag(source, ObjectUtils.emptyMap());
    }

    public String imageTag(Map<String, String> attributes) {
        return imageTag(null, attributes);
    }

    public String imageTag(String source, Map<String, String> attributes) {
        String url = generate(source);
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

    public String videoTag() {
        return videoTag("", new HashMap<String, String>());
    }

    public String videoTag(Map<String, String> attributes) {
        return videoTag("", attributes);
    }

    private String finalizePosterUrl(String source) {
        String posterUrl = null;
        if (this.posterUrl != null) {
            posterUrl = this.posterUrl.generate();
        } else if (this.posterTransformation != null) {
            posterUrl = this.clone().format("jpg").transformation(new Transformation(this.posterTransformation))
                    .generate(source);
        } else if (this.posterSource != null) {
            if (!StringUtils.isEmpty(this.posterSource))
                posterUrl = this.clone().format("jpg").generate(this.posterSource);
        } else {
            posterUrl = this.clone().format("jpg").generate(source);
        }
        return posterUrl;
    }

    private void appendVideoSources(StringBuilder html, String source, String sourceType) {
        Url sourceUrl = this.clone();
        if (this.sourceTransformation != null) {
            Transformation transformation = this.transformation;
            Transformation sourceTransformation = null;
            if (this.sourceTransformation.get(sourceType) != null)
                sourceTransformation = new Transformation(this.sourceTransformation.get(sourceType));
            if (transformation == null) {
                transformation = sourceTransformation;
            } else if (sourceTransformation != null) {
                transformation = transformation.chainWith(sourceTransformation);
            }
            sourceUrl.transformation(transformation);
        }
        String src = sourceUrl.format(sourceType).generate(source);
        String videoType = sourceType;
        if (sourceType.equals("ogv"))
            videoType = "ogg";
        String mimeType = "video/" + videoType;
        html.append("<source src='").append(src).append("' type='").append(mimeType).append("'>");
    }

    public String videoTag(String source, Map<String, String> attributes) {
        if (StringUtils.isEmpty(source))
            source = this.source;
        if (StringUtils.isEmpty(source))
            source = publicId;
        if (StringUtils.isEmpty(source))
            throw new IllegalArgumentException("must supply source or public id");
        source = VIDEO_EXTENSION_RE.matcher(source).replaceFirst("");

        if (this.resourceType == null) this.resourceType = "video";
        attributes = new TreeMap<String, String>(attributes); // Make sure they are ordered.

        String[] sourceTypes = this.sourceTypes;

        if (sourceTypes == null) {
            sourceTypes = DEFAULT_VIDEO_SOURCE_TYPES;
        }

        String posterUrl = this.finalizePosterUrl(source);

        if (!StringUtils.isEmpty(posterUrl))
            attributes.put("poster", posterUrl);

        StringBuilder html = new StringBuilder().append("<video");

        String url = null;

        boolean multiSource = sourceTypes.length > 1;
        if (!multiSource) {
            url = generate(source + "." + sourceTypes[0]);
            attributes.put("src", url);
        } else {
            generate(source);
        }

        if (this.transformation.getHtmlHeight() != null)
            attributes.put("height", this.transformation.getHtmlHeight());
        if (attributes.containsKey("html_height"))
            attributes.put("height", attributes.remove("html_height"));
        if (this.transformation.getHtmlWidth() != null)
            attributes.put("width", this.transformation.getHtmlWidth());
        if (attributes.containsKey("html_width"))
            attributes.put("width", attributes.remove("html_width"));

        for (Map.Entry<String, String> attr : attributes.entrySet()) {
            html.append(" ").append(attr.getKey());
            if (attr.getValue() != null) {
                String value = ObjectUtils.asString(attr.getValue());
                html.append("='").append(value).append("'");
            }
        }

        html.append(">");

        if (multiSource) {
            for (String sourceType : sourceTypes) {
                this.appendVideoSources(html, source, sourceType);
            }
        }

        if (this.fallbackContent != null)
            html.append(this.fallbackContent);
        html.append("</video>");
        return html.toString();
    }

    public String generateSpriteCss(String source) {
        this.type = "sprite";
        if (!source.endsWith(".css"))
            this.format = "css";
        return generate(source);
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
}
