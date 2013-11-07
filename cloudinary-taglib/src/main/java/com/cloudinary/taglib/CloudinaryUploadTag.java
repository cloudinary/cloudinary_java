package com.cloudinary.taglib;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;

import com.cloudinary.*;

public class CloudinaryUploadTag extends SimpleTagSupport {

    // HTML basics
    private String id = null;
    private String name = "file";
    private String extraClasses = null;
    
    // Cloudinary Specific
    private String tags = null;
    private String fieldName;
    private String resourceType = "auto";
    private String transformation;
    private String callback;
    
    public void doTag() throws JspException, IOException {
        Cloudinary cloudinary = Singleton.getCloudinary();
        if (cloudinary == null) {
            throw new JspException("Cloudinary config could not be located");
        }
        Uploader uploader = cloudinary.uploader();
        
        Map<String, Object> htmlOptions = new HashMap<String, Object>();
        htmlOptions.put("type", "file");
        htmlOptions.put("name", name);
        if (id != null) {
            htmlOptions.put("id", id);
        }
        
        Map<String, String> options = new HashMap<String, String>();
        options.put("resource_type", resourceType);
        options.put("transformation", transformation);
        if (tags != null) {
            options.put("tags", tags);
        }

        options.put("callback", callback);

        buildCallbackUrl(options);

        String renderedHtml = uploader.imageUploadTag(fieldName, options, htmlOptions);
        
        getJspContext().getOut().println(renderedHtml);
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
        this.transformation = transformation.replaceAll("\\s+","/");;
    }
    
    public String getTransformation() {
        return transformation;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }
    
    public String GetResourceType() {
        return resourceType;
    }

    public String getCallback() {
        return callback;
    }

    public void setCallback(String callback) {
        this.callback = callback;
    }

    private void buildCallbackUrl(Map options) {
        String callback = (String) options.get("callback");
        if (callback == null || callback.isEmpty()) callback = Singleton.getCloudinary().getStringConfig("callback");
        if (callback == null || callback.isEmpty()) callback = "/cloudinary_cors.html";
        if (!callback.matches("^https?://")) {
            PageContext context = (PageContext) getJspContext();
            ServletRequest request = context.getRequest();
            String callbackUrl = request.getScheme() + "://" + request.getServerName();
            if (request.getScheme().equals("https") && request.getServerPort() != 443 ||
                    request.getScheme().equals("http") && request.getServerPort() != 80) {
                callbackUrl += ":" + request.getServerPort();
            }
            callbackUrl += callback;
            options.put("callback", callbackUrl);
        }
    }
    
}
