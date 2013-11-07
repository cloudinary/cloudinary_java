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
public class CloudinaryUrl extends SimpleTagSupport {

    private String src = null;

    private String type = null;
    private String resourceType = null;
    private String format = null;

    private String transformation = null;

    public void doTag() throws JspException, IOException {
        Cloudinary cloudinary = Singleton.getCloudinary();
        if (cloudinary == null) {
            throw new JspException("Cloudinary config could not be located");
        }

        JspWriter out = getJspContext().getOut();

        Url url = cloudinary.url();
        if (transformation != null) url.transformation(new Transformation().rawTransformation(transformation));
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
}
