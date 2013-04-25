package com.cloudinary.taglib;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.SimpleTagSupport;

public class CloudinaryFileInputTag extends SimpleTagSupport {

    public void doTag() throws JspException, IOException {
        String renderedHtml = "";
        
        getJspContext().getOut().println(renderedHtml);
    }
}
