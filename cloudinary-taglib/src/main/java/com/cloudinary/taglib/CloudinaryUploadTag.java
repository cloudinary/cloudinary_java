package com.cloudinary.taglib;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.SimpleTagSupport;

import org.apache.commons.lang3.StringEscapeUtils;

import com.cloudinary.*;

public class CloudinaryUploadTag extends SimpleTagSupport {

    private String resourceType = "auto";
    private String fieldName;
    private String extraClasses;
    
    public void doTag() throws JspException, IOException {
        Cloudinary cloudinary = Singleton.getCloudinary();
        if (cloudinary == null) {
            throw new JspException("Cloudinary config could not be located");
        }
        
        StringBuilder renderedHtml = new StringBuilder();
        
        renderedHtml.append("<input ");
        
        Map<String, String> attributes = new HashMap<String, String>();
        attributes.put("type", "file");
        attributes.put("name", "file");
        
        Map<String, String> options = new HashMap<String, String>();
        options.put("resource_type", resourceType);
        
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

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }
    
    public String GetResourceType() {
        return resourceType;
    }
    
    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }
    
    public String getFieldName() {
        return fieldName;
    }
    
    public void setExtraClasses(String extraClasses) {
        this.extraClasses = extraClasses;
    }
    
    public String getExtraClasses() {
        return extraClasses;
    }
    
}
