package cloudinary.models;

import com.cloudinary.Singleton;
import com.cloudinary.StoredFile;
import com.cloudinary.Transformation;
import org.gmr.web.multipart.GMultipartFile;
import com.google.appengine.api.datastore.Entity;

public class PhotoUpload extends StoredFile {
    private String title;

    private GMultipartFile file;
    public PhotoUpload() {
    	super();
    }
    
    public PhotoUpload(Entity entity) {
    	super();
    	this.publicId = entity.getProperty("public_id").toString();
    	this.format = entity.getProperty("format").toString();
    	this.version = (Long) entity.getProperty("version");
    	this.type = entity.getProperty("type").toString();
    	this.resourceType = entity.getProperty("resource_type").toString();
    	this.title = entity.getProperty("title").toString();
    }
    
    public String getUrl() {
        if (version != null && format != null && publicId != null) {
            return Singleton.getCloudinary().url()
                    .resourceType(resourceType)
                    .type(type)
                    .format(format)
                    .version(version)
                    .generate(publicId);
        } else return null;
    }

    public String getThumbnailUrl() {
        if (version != null && format != null && publicId != null) {
            return Singleton.getCloudinary().url().format(format)
                    .resourceType(resourceType)
                    .type(type)
                    .version(version).transformation(new Transformation().width(150).height(150).crop("fit"))
                    .generate(publicId);
        } else return null;
    }

    public String getComputedSignature() {
        return getComputedSignature(Singleton.getCloudinary());
    }

    public boolean validSignature() {
        return getComputedSignature().equals(signature);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public GMultipartFile getFile() {
        return file;
    }

    public void setFile(GMultipartFile file) {
        this.file = file;
    }
    
    public void toEntity(Entity photo) {
    	photo.setProperty("title", getTitle());
        photo.setProperty("version", getVersion());
        photo.setProperty("public_id", getPublicId());
        photo.setProperty("format", getFormat());
        photo.setProperty("url", getUrl());
        photo.setProperty("type", getType());
        photo.setProperty("resource_type", getResourceType());
    }
}
