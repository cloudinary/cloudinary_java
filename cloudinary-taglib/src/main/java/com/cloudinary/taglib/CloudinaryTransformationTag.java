package com.cloudinary.taglib;

import com.cloudinary.Transformation;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.DynamicAttributes;
import javax.servlet.jsp.tagext.SimpleTagSupport;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CloudinaryTransformationTag extends SimpleTagSupport implements DynamicAttributes {
    private Map<String,Object> tagAttrs = new HashMap<String,Object>();

    public void doTag() throws JspException, IOException {
        Transformation transformation = new Transformation().params(tagAttrs);
        getJspContext().getOut().print(transformation.generate());
    }

    @Override
    public void setDynamicAttribute(String uri, String name, Object value) throws JspException {
        tagAttrs.put(name, value);
    }
}
