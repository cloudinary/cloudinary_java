package com.cloudinary.taglib;

import com.cloudinary.Srcset;
import com.cloudinary.TagOptions;
import com.cloudinary.Url;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Generates an image html tag.<br>
 * For example,<br>
 * {@code <cl:img source='test' height='101' width='100' crop="crop" />}
 * <br>is equivalent to:<br>
 * <pre>{@code
 * Transformation transformation = new Transformation()
 *      .width(100)
 *      .height(101)
 *      .crop("crop");
 * String result = cloudinary.url()
 *      .transformation(transformation)
 *      .imageTag("test", Cloudinary.asMap("alt", "my image"));
 * }</pre>
 * <br>
 * Both code segments above produce the following tag:<br>
 * {@code <img src='http://res.cloudinary.com/test123/image/upload/c_crop,h_101,w_100/test' alt='my image'
 * height='101' width='100'/> }
 * <br>
 * @author jpollak
 * 
 */
public class CloudinaryImageTag extends CloudinaryUrl {

    private String id = null;
    private String extraClasses = null;
    private Srcset srcset;

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
        TagOptions tagOptions = new TagOptions().attributes(prepareAttributes()).srcset(srcset);
        out.println(url.imageTag(null, tagOptions));
    }

    /**
     * Set the srcset configuration instance for the tag. See {@link Srcset} for details
     * @param srcset
     */
    public void setSrcset(Srcset srcset){
        this.srcset = srcset;
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