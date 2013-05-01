package com.cloudinary.taglib;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.DynamicAttributes;
import javax.servlet.jsp.tagext.SimpleTagSupport;

import com.cloudinary.*;

/**
 * <cl:img source='test' height='101' width='100' transform="crop" />
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
public class CloudinaryImageTag extends SimpleTagSupport implements DynamicAttributes {

    private String id = null;
    private String extraClasses = null;
    
    private String publicId = null;
    private String format = null;
    
    /** stores the dynamic attributes */
    private Map<String,Object> tagAttrs = new HashMap<String,Object>();

    public void doTag() throws JspException, IOException {
        Cloudinary cloudinary = Singleton.getCloudinary();
        if (cloudinary == null) {
            throw new JspException("Cloudinary config could not be located");
        }
        
        JspWriter out = getJspContext().getOut();
        
        Map<String, String> attributes = new HashMap<String, String>();
        if (id != null) {
            attributes.put("id", id);
        }
        if (extraClasses != null) {
            attributes.put("class", extraClasses);
        }
        
        Transformation transformation = new Transformation().params(tagAttrs);
        Url url = cloudinary.url().transformation(transformation).format(format);
        
        out.println(url.imageTag(publicId, attributes));
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setExtraClasses(String extraClasses) {
        this.extraClasses = extraClasses;
    }

    public String getExtraClass() {
        return extraClasses;
    }

    public void setPublicId(String publicId) {
        this.publicId = publicId;
    }

    public String getPublicId() {
        return publicId;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getFormat() {
        return format;
    }
    
    @Override
    public void setDynamicAttribute(String uri, String name, Object value) throws JspException {
        tagAttrs.put(name, value);
    }
}
