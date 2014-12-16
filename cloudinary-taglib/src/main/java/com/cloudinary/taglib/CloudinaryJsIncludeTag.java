package com.cloudinary.taglib;

import com.cloudinary.Cloudinary;
import com.cloudinary.Singleton;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.SimpleTagSupport;
import java.io.IOException;

public class CloudinaryJsIncludeTag  extends SimpleTagSupport {
    private boolean full = false;
    private String base = "javascripts/cloudinary/";

    public void doTag() throws JspException, IOException {
        Cloudinary cloudinary = Singleton.getCloudinary();
        if (cloudinary == null) {
            throw new JspException("Cloudinary config could not be located");
        }
        JspWriter out = getJspContext().getOut();
        String[] basicFiles = {"jquery.ui.widget.js", "jquery.iframe-transport.js", "jquery.fileupload.js", "jquery.cloudinary.js"};
        for (String file : basicFiles) {
            out.println("<script type='text/javascript' src='" + base + file + "'></script>");
        }
        if (full) {
            String[] fullFiles = {"canvas-to-blob.min.js", "load-image.min.js", "jquery.fileupload-process.js", "uery.fileupload-image.js", "jquery.fileupload-validate.js"};
            for (String file : fullFiles) {
                out.println("<script type='text/javascript' src='" + base + file + "'></script>");
            }
        }

    }

    public boolean isFull() {
        return full;
    }

    public void setFull(boolean full) {
        this.full = full;
    }

    public String getBase() {
        return base;
    }

    public void setBase(String base) {
        this.base = base;
    }

}
