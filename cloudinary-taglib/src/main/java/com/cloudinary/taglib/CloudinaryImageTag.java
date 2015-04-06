package com.cloudinary.taglib;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.DynamicAttributes;
import javax.servlet.jsp.tagext.SimpleTagSupport;

import com.cloudinary.*;

/**
 * <cl:img source='test' height='101' width='100' crop="crop" />
 * 
 * Transformation transformation = new Transformation().width(100).height(101).crop("crop");
 * String result = cloudinary.url().transformation(transformation).imageTag("test",
 * Cloudinary.asMap("alt", "my image"));
 * 
 * <img src='http://res.cloudinary.com/test123/image/upload/c_crop,h_101,w_100/test' alt='my image'
 * height='101' width='100'/>
 * 
 * @author jpollak
 * 
 */
public class CloudinaryImageTag extends CloudinaryUrl {

    private String id = null;
    private String extraClasses = null;
    
    protected Map<String, String> prepareAttributes() {
    	Map<String, String> attributes = new HashMap<String, String>();
        if (id != null) {
            attributes.put("id", id);
        }
        if (extraClasses != null) {
            attributes.put("class", extraClasses);
        }
        return attributes;
    }
    
    public void doTag() throws JspException, IOException {
        JspWriter out = getJspContext().getOut();
        Url url = this.prepareUrl();
        out.println(url.imageTag(prepareAttributes()));
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public String getExtraClasses() {
        return extraClasses;
    }

    public void setExtraClasses(String extraClasses) {
        this.extraClasses = extraClasses;
    }

    @Deprecated
    public void setPublicId(String src) {
        this.src = src;
    }

    @Deprecated
    public String getPublicId() {
        return src;
    }

}