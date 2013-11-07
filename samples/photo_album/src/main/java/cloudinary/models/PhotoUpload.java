package cloudinary.models;

import com.cloudinary.Singleton;
import com.cloudinary.Transformation;
import org.springframework.web.multipart.MultipartFile;

import java.util.regex.*;

public class PhotoUpload {
    private String title;

    private MultipartFile file;

    private Long version;

    private String publicId;

    private String format;

    private String signature;

    private String type;

    private String resourceType = "image";

    private static final Pattern PRELOADED_PATTERN = Pattern.compile("^([^\\/]+)\\/([^\\/]+)\\/v(\\d+)\\/([^#]+)#([^\\/]+)$");

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public MultipartFile getFile() {
        return file;
    }

    public void setFile(MultipartFile file) {
        this.file = file;
    }

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

    public String getPublicIdForSigning() {
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

    public String getPreloadedImage() {
        return resourceType + "/" + type + "/v" + version + "/" + publicId + ((format != null && format.isEmpty()) ? "." + format : "");
    }

    public void setPreloadedImage(String uri) {
        if (uri.matches(PRELOADED_PATTERN.pattern())) {
            Matcher match = PRELOADED_PATTERN.matcher(uri);
            match.find();
            resourceType = match.group(1);
            type = match.group(2);
            version = Long.parseLong(match.group(3));
            String filename = match.group(4);
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

    public String getUrl() {
        if (version != null && format != null && publicId != null) {
            return Singleton.getCloudinary().url().resourceType(resourceType)
                    .format(format)
                    .version(version)
                    .generate(publicId);
        } else return null;
    }

    public String getThumbnailUrl() {
        if (version != null && format != null && publicId != null) {
            return Singleton.getCloudinary().url().format(format)
                    .resourceType(resourceType)
                    .version(version).transformation(new Transformation().width(150).height(150).crop("fit"))
                    .generate(publicId);
        } else return null;
    }

    public boolean getIsImage() {
        return resourceType.equals("image") || resourceType.equals("auto");
    }
}
