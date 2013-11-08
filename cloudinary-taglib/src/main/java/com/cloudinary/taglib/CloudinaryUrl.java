package com.cloudinary.taglib;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.DynamicAttributes;
import javax.servlet.jsp.tagext.SimpleTagSupport;

import com.cloudinary.*;

/**
 * <cl:url source='test' height='101' width='100' crop="crop" />
 * http://res.cloudinary.com/test123/image/upload/c_crop,h_101,w_100/test
 *
 */
public class CloudinaryUrl extends SimpleTagSupport implements DynamicAttributes {

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

        Url url = cloudinary.url();
        Transformation baseTransformation = new Transformation().params(tagAttrs);
        url.transformation(baseTransformation.chain().rawTransformation(transformation));
        if (format != null) url.format(format);
        if (type != null) url.type(type);
        if (resourceType != null) url.resourceType(resourceType);

        out.println(url.generate(src));
    }

    public void setSrc(String src) {
        this.src = src;
    }

    public String getSrc() {
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
        this.transformation = transformation.replaceAll("\\s","/");
    }

    @Override
    public void setDynamicAttribute(String uri, String name, Object value) throws JspException {
        tagAttrs.put(name, value);
    }
}
