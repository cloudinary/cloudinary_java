package com.cloudinary.taglib;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;

import com.cloudinary.Cloudinary;
import com.cloudinary.Singleton;
import com.cloudinary.Transformation;
import com.cloudinary.Uploader;

public class CloudinaryUploadTag extends SimpleTagSupport {

    // HTML basics
    private String id = null;
    private String name = "file";
    private String extraClasses = null;
    private Boolean multiple = false;
    
    // Cloudinary Specific
    private String tags = null;
    protected String fieldName;
    private String resourceType = "auto";
    private String transformation = null;
    private String eager = null;
    private String callback = null;
    private String publicId = null;
    private String format = null;
    private String notificationUrl = null;
    private String eagerNotificationUrl = null;
    private String proxy = null;
    private String folder = null;
    private String faceCoordinates = null;
    private String customCoordinates = null;
	private String allowedFormats = null;
    private String context = null;
    private String ocr = null;
    private String detection = null;
    private String categorization = null;
    private String similaritySearch = null;
    private String backgroundRemoval = null;
    private Float autoTagging = null;
    protected String uploadPreset = null;
	private Boolean backup = null;
    private Boolean exif = null;
    private Boolean faces = null;
    private Boolean colors = null;
    private Boolean imageMetadata = null;
    private Boolean useFilename = null;
    private Boolean uniqueFilename = null;
    private Boolean eagerAsync = null;
    private Boolean invalidate = null;
    private Boolean overwrite = null;
    private Boolean phash = null;
    protected boolean unsigned = false;

	public void doTag() throws JspException, IOException {
        Cloudinary cloudinary = Singleton.getCloudinary();
        if (cloudinary == null) {
            throw new JspException("Cloudinary config could not be located");
        }
        Uploader uploader = (Uploader)cloudinary.uploader();
        
        Map<String, Object> htmlOptions = new HashMap<String, Object>();
        htmlOptions.put("type", "file");
        htmlOptions.put("name", name);
        htmlOptions.put("multiple", multiple);
        htmlOptions.put("class", extraClasses);
        htmlOptions.put("id", id);
        
        Map<String, Object> options = new HashMap<String, Object>();
        options.put("resource_type", resourceType);
        options.put("transformation", transformation);
        options.put("eager", buildEager());
        options.put("tags", tags);
        options.put("callback", callback);
        options.put("public_id", publicId);
        options.put("format", format);
        options.put("notification_url", notificationUrl);
        options.put("eager_notification_url", eagerNotificationUrl);
        options.put("proxy", proxy);
        options.put("folder", folder);
        options.put("backup", backup);
        options.put("exif", exif);
        options.put("faces", faces);
        options.put("colors", colors);
        options.put("image_metadata", imageMetadata);
        options.put("use_filename", useFilename);
        options.put("unique_filename", uniqueFilename);
        options.put("eager_async", eagerAsync);
        options.put("invalidate", invalidate);
        options.put("face_coordinates", faceCoordinates);
        options.put("custom_coordinates", customCoordinates);
        options.put("allowed_formats", allowedFormats);
        options.put("context", context);
        options.put("overwrite", overwrite);
        options.put("phash", phash);
        options.put("ocr", ocr);
        options.put("detection", detection);
        options.put("categorization", categorization);
        options.put("similarity_search", similaritySearch);
        options.put("auto_tagging", autoTagging);
        options.put("background_removal", backgroundRemoval);
        options.put("upload_preset", uploadPreset);
        options.put("unsigned", unsigned);

        buildCallbackUrl(options);

        getJspContext().getOut().println(uploadTag(uploader, options, htmlOptions));
    }
	
    public void setId(String id) {
        this.id = id;
    }
    
    public String getId() {
        return id;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
    
    public void setExtraClasses(String extraClasses) {
        this.extraClasses = extraClasses;
    }
    
    public String getExtraClasses() {
        return extraClasses;
    }
    
    public void setTags(String tags) {
        this.tags = tags;
    }
    
    public String getTags() {
        return tags;
    }
    
    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }
    
    public String getFieldName() {
        return fieldName;
    }
    
    public void setTransformation(String transformation) {
        this.transformation = transformation.replaceAll("\\s+","/");
    }
    
    public String getTransformation() {
        return transformation;
    }


    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public Boolean getMultiple() {
        return multiple;
    }

    public void setMultiple(Boolean multiple) {
        this.multiple = multiple;
    }

    public String getEager() {
        return eager;
    }

    public void setEager(String eager) {
        this.eager = eager.replaceAll("\\s+","|");;
    }

    public String getPublicId() {
        return publicId;
    }

    public void setPublicId(String publicId) {
        this.publicId = publicId;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getNotificationUrl() {
        return notificationUrl;
    }

    public void setNotificationUrl(String notificationUrl) {
        this.notificationUrl = notificationUrl;
    }

    public String getEagerNotificationUrl() {
        return eagerNotificationUrl;
    }

    public void setEagerNotificationUrl(String eagerNotificationUrl) {
        this.eagerNotificationUrl = eagerNotificationUrl;
    }

    public String getProxy() {
        return proxy;
    }

    public void setProxy(String proxy) {
        this.proxy = proxy;
    }

    public String getFolder() {
        return folder;
    }

    public void setFolder(String folder) {
        this.folder = folder;
    }

    public boolean isBackup() {
        return backup;
    }

    public void setBackup(boolean backup) {
        this.backup = backup;
    }

    public boolean isExif() {
        return exif;
    }

    public void setExif(boolean exif) {
        this.exif = exif;
    }

    public boolean isFaces() {
        return faces;
    }

    public void setFaces(boolean faces) {
        this.faces = faces;
    }

    public boolean isColors() {
        return colors;
    }

    public void setColors(boolean colors) {
        this.colors = colors;
    }

    public boolean isImageMetadata() {
        return imageMetadata;
    }

    public void setImageMetadata(boolean imageMetadata) {
        this.imageMetadata = imageMetadata;
    }

    public boolean isUseFilename() {
        return useFilename;
    }

    public void setUseFilename(boolean useFilename) {
        this.useFilename = useFilename;
    }

    public boolean isUniqueFilename() {
        return uniqueFilename;
    }

    public void setUniqueFilename(boolean uniqueFilename) {
        this.uniqueFilename = uniqueFilename;
    }

    public boolean isEagerAsync() {
        return eagerAsync;
    }

    public void setEagerAsync(boolean eagerAsync) {
        this.eagerAsync = eagerAsync;
    }

    public boolean isInvalidate() {
        return invalidate;
    }

    public void setInvalidate(boolean invalidate) {
        this.invalidate = invalidate;
    }

    public String getCallback() {
        return callback;
    }

    public void setCallback(String callback) {
        this.callback = callback;
    }
    
    public String getFaceCoordinates() {
		return faceCoordinates;
	}

	public void setFaceCoordinates(String faceCoordinates) {
		this.faceCoordinates = faceCoordinates;
	}

    public String getCustomCoordinates() {
        return customCoordinates;
    }

    public void setCustomCoordinates(String customCoordinates) {
        this.customCoordinates = customCoordinates;
    }

	public String getAllowedFormats() {
		return allowedFormats;
	}

	public void setAllowedFormats(String allowedFormats) {
		this.allowedFormats = allowedFormats;
	}

	public String getContext() {
		return context;
	}

	public void setContext(String context) {
		this.context = context;
	}
	
	public boolean isOverwrite() {
		return overwrite;
	}

	public void setOverwrite(boolean overwrite) {
		this.overwrite = overwrite;
	}
	
	public boolean isPhash() {
		return phash;
	}

	public void setPhash(boolean phash) {
		this.phash = phash;
	}
	
	public String getOcr() {
		return ocr;
	}

	public void setOcr(String ocr) {
		this.ocr = ocr;
	}

	public String getDetection() {
		return detection;
	}

	public void setDetection(String detection) {
		this.detection = detection;
	}

	public String getCategorization() {
		return categorization;
	}

	public void setCategorization(String categorization) {
		this.categorization = categorization;
	}

	public String getSimilaritySearch() {
		return similaritySearch;
	}

	public void setSimilaritySearch(String similaritySearch) {
		this.similaritySearch = similaritySearch;
	}

    public String getBackgroundRemoval() {
        return backgroundRemoval;
    }

    public void setBackgroundRemoval(String backgroundRemoval) {
        this.backgroundRemoval = backgroundRemoval;
    }

	public Float getAutoTagging() {
		return autoTagging;
	}

	public void setAutoTagging(String autoTagging) {
		this.autoTagging = Float.parseFloat(autoTagging);
	}
	
	public String getUploadPreset() {
		return uploadPreset;
	}

	public void setUploadPreset(String uploadPreset) {
		this.uploadPreset = uploadPreset;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected String uploadTag(Uploader uploader, Map options, Map htmlOptions) {
		return uploader.imageUploadTag(fieldName, options, htmlOptions);
	}

    @SuppressWarnings({ "rawtypes", "unchecked" })
	private void buildCallbackUrl(Map options) {
        String callback = (String) options.get("callback");
        if (callback == null || callback.isEmpty()) callback = Singleton.getCloudinary().config.callback;
        if (callback == null || callback.isEmpty()) callback = "/cloudinary_cors.html";
        if (!callback.matches("^https?://")) {
            PageContext context = (PageContext) getJspContext();
            HttpServletRequest request = (HttpServletRequest) context.getRequest();
            String callbackUrl = request.getScheme() + "://" + request.getServerName();
            if (request.getScheme().equals("https") && request.getServerPort() != 443 ||
                    request.getScheme().equals("http") && request.getServerPort() != 80) {
                callbackUrl += ":" + request.getServerPort() + request.getContextPath();
            }
            callbackUrl += callback;
            options.put("callback", callbackUrl);
        }
    }

    private List<Transformation> buildEager() {
    	if (eager == null) return null;
        String[] raws = eager.split("\\|");
        List<Transformation> list = new ArrayList<Transformation>();
        for (String raw : raws) {
            list.add(new Transformation().rawTransformation(raw));
        }
        return list;
    }
    
}
