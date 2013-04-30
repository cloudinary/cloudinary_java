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
        Uploader uploader = cloudinary.uploader();
        
        Map<String, Object> htmlOptions = new HashMap<String, Object>();
        htmlOptions.put("type", "file");
        htmlOptions.put("name", name);
        if (id != null) {
            htmlOptions.put("id", id);
        }
        
        Map<String, String> options = new HashMap<String, String>();
        options.put("resource_type", resourceType);
        if (tags != null) {
            options.put("tags", tags);
        }

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

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }
    
    public String GetResourceType() {
        return resourceType;
    }
    
}
