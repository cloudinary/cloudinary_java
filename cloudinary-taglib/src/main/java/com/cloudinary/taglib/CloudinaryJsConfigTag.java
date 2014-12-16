package com.cloudinary.taglib;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.SimpleTagSupport;

import com.cloudinary.Cloudinary;
import com.cloudinary.Singleton;

public class CloudinaryJsConfigTag  extends SimpleTagSupport {
	@SuppressWarnings("unused")
    public void doTag() throws JspException, IOException {
        Cloudinary cloudinary = Singleton.getCloudinary();
        if (cloudinary == null) {
            throw new JspException("Cloudinary config could not be located");
        }
        JspWriter out = getJspContext().getOut();
        out.println("<script language='javascript' type='text/javascript'>$.cloudinary.config({");
        String[] keys = {"api_key", "cloud_name", "cdn_subdomain"};
		String[] boolKeys = {"private_cdn", "secure_distribution"};
        
        print(out,"api_key",cloudinary.config.apiKey);
        print(out,"cloud_name",cloudinary.config.cloudName);
        print(out,"cdn_subdomain",cloudinary.config.cdnSubdomain);
        
        print(out,"private_cdn",cloudinary.config.privateCdn);
        print(out,"secure_distribution",cloudinary.config.secureDistribution);
        
        
       
        out.println("});</script>");
    }

	private void print(JspWriter out, String key,Object value) throws IOException {
		out.println(key + ": \""+value + "\",");
		
	}
}
