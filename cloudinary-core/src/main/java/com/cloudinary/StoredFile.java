package com.cloudinary;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StoredFile {
    protected Long version;

    protected String publicId;

    protected String format;

    protected String signature;

    protected String type = "upload";

    protected String resourceType = "image";

    private static final String IMAGE_RESOURCE_TYPE = "image";

    private static final String VIDEO_RESOURCE_TYPE = "video";

    private static final String AUTO_RESOURCE_TYPE = "auto";

    private static final Pattern PRELOADED_PATTERN = Pattern.compile("^([^\\/]+)\\/([^\\/]+)\\/v(\\d+)\\/([^#]+)#?([^\\/]+)?$");

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public String getPublicId() {
        return publicId;
    }

    public void setPublicId(String publicId) {
        this.publicId = publicId;
    }

    protected String getPublicIdForSigning() {
        return publicId + ((format != null && !format.isEmpty() && resourceType.equals("raw")) ? "." + format : "");
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPreloadedFile() {
        StringBuilder sb = new StringBuilder();
        sb.append(resourceType).append("/").append(type).append("/v").append(version).append("/").append(publicId);
        if (format != null && !format.isEmpty()) {
            sb.append(".").append(format);
        }
        if (signature != null && !signature.isEmpty()) {
            sb.append("#").append(signature);
        }
        return sb.toString();
    }

    public void setPreloadedFile(String uri) {
        if (uri.matches(PRELOADED_PATTERN.pattern())) {
            Matcher match = PRELOADED_PATTERN.matcher(uri);
            match.find();
            resourceType = match.group(1);
            type = match.group(2);
            version = Long.parseLong(match.group(3));
            String filename = match.group(4);
            if (match.groupCount() == 5)
                signature = match.group(5);
            int lastDotIndex = filename.lastIndexOf('.');
            if (lastDotIndex == -1) {
                publicId = filename;
            } else {
                publicId = filename.substring(0, lastDotIndex);
                format = filename.substring(lastDotIndex + 1);
            }
        }
    }

    public String getComputedSignature(Cloudinary cloudinary) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("version", getVersion().toString());
        params.put("public_id", getPublicIdForSigning());
        cloudinary.signRequest(params, new HashMap<String, Object>());
        return params.get("signature").toString();
    }

    public boolean getIsImage() {
        return IMAGE_RESOURCE_TYPE.equals(resourceType) || AUTO_RESOURCE_TYPE.equals(resourceType);
    }

    public boolean getIsVideo() {
        return VIDEO_RESOURCE_TYPE.equals(resourceType);
    }
}
