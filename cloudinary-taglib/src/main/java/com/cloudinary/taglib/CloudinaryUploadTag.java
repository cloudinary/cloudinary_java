package com.cloudinary.taglib;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.SimpleTagSupport;

import org.apache.commons.lang3.StringEscapeUtils;

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
    
    public void doTag() throws JspException, IOException {
        Cloudinary cloudinary = Singleton.getCloudinary();
        if (cloudinary == null) {
            throw new JspException("Cloudinary config could not be located");
        }
        
        StringBuilder renderedHtml = new StringBuilder();
        
        renderedHtml.append("<input ");
        
        Map<String, String> attributes = new HashMap<String, String>();
        attributes.put("type", "file");
        attributes.put("name", name);
        if (id != null) {
            attributes.put("id", id);
        }
        
        Map<String, String> options = new HashMap<String, String>();
        options.put("resource_type", resourceType);
        if (tags != null) {
            options.put("tags", tags);
        }
        
        String cloudinaryUrl = cloudinary.cloudinaryApiUrl("upload", options);
        
        Uploader uploader = new Uploader(cloudinary);
        Map<String, Object> params = uploader.buildUploadParams(options);
        uploader.signRequestParams(params, options);
        
        attributes.put("data-url", cloudinaryUrl);
        
        StringBuilder jsonParams = new StringBuilder();
        jsonParams.append("{");
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            if (entry.getValue() != null) {
                jsonParams.append("'" + entry.getKey() + "':'" + entry.getValue() + "',");
            }
        }
        jsonParams.append("}");
        String escapedJsonParams = StringEscapeUtils.escapeHtml4(jsonParams.toString());
        
        attributes.put("data-form-data", escapedJsonParams);
        attributes.put("data-cloudinary-field", fieldName);
        attributes.put("class", "cloudinary-fileupload" + ((extraClasses != null) ? " " + extraClasses : ""));
        
        for (Map.Entry<String, String> entry : attributes.entrySet()) {
            renderedHtml.append(" " + entry.getKey() + "=\"" + entry.getValue() + "\"");
        }

        renderedHtml.append("/>");
        
        getJspContext().getOut().println(renderedHtml.toString());
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

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }
    
    public String GetResourceType() {
        return resourceType;
    }
    
}
