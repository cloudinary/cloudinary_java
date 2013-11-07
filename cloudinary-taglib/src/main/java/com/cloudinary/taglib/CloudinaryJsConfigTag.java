package com.cloudinary.taglib;

import com.cloudinary.Cloudinary;
import com.cloudinary.Singleton;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.SimpleTagSupport;
import java.io.IOException;

public class CloudinaryJsConfigTag  extends SimpleTagSupport {
    public void doTag() throws JspException, IOException {
        Cloudinary cloudinary = Singleton.getCloudinary();
        if (cloudinary == null) {
            throw new JspException("Cloudinary config could not be located");
        }
        JspWriter out = getJspContext().getOut();
        out.println("<script language='javascript' type='text/javascript'>$.cloudinary.config({");
        String[] keys = {"api_key", "cloud_name", "cdn_subdomain"};
        String[] boolKeys = {"private_cdn", "secure_distribution"};
        for (String key : keys) {
            if (cloudinary.getStringConfig(key) != null && !cloudinary.getStringConfig(key).isEmpty()) {
                out.println(key + ": \""+cloudinary.getStringConfig(key) + "\",");
            }
        }
        for (String key : boolKeys) {
            if (cloudinary.getStringConfig(key) != null && !cloudinary.getStringConfig(key).isEmpty()) {
                out.println(key + ": "+cloudinary.getStringConfig(key) + ",");
            }
        }
        out.println("});</script>");
    }
}
