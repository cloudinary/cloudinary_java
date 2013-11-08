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
public class CloudinaryImageTag extends SimpleTagSupport implements DynamicAttributes {

    private String id = null;
    private String extraClasses = null;

    private String src = null;

    private String type = null;
    private String resourceType = null;
    private String format = null;

    private String transformation = null;

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

        Url url = cloudinary.url();

        Transformation baseTransformation = new Transformation().params(tagAttrs);

        url.transformation(baseTransformation.chain().rawTransformation(transformation));
        if (format != null) url.format(format);
        if (type != null) url.type(type);
        if (resourceType != null) url.resourceType(resourceType);
        
        out.println(url.imageTag(src, attributes));
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

    public void setSrc(String src) {
        this.src = src;
    }

    public String getSrc() {
        return src;
    }

    @Deprecated
    public void setPublicId(String src) {
        this.src = src;
    }

    @Deprecated
    public String getPublicId() {
        return src;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getFormat() {
        return format;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public String getTransformation() {
        return transformation;
    }

    public void setTransformation(String transformation) {
        this.transformation = transformation.replaceAll("\\s+","/");
    }

    @Override
    public void setDynamicAttribute(String uri, String name, Object value) throws JspException {
        tagAttrs.put(name, value);
    }
}