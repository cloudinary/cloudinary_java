package com.cloudinary.taglib;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;

import com.cloudinary.Transformation;
import com.cloudinary.Url;

public class CloudinaryVideoTag extends CloudinaryImageTag {
	private String sourceTypes;
	private Object poster;
	private Boolean autoplay;
	private Boolean controls;
	private Boolean loop;
	private Boolean muted;
	private Boolean preload;
	
	public Boolean getAutoplay() {
		return autoplay;
	}
	public void setAutoplay(Boolean autoplay) {
		this.autoplay = autoplay;
	}
	public Boolean getControls() {
		return controls;
	}
	public void setControls(Boolean controls) {
		this.controls = controls;
	}
	public Boolean getLoop() {
		return loop;
	}
	public void setLoop(Boolean loop) {
		this.loop = loop;
	}
	public Boolean getMuted() {
		return muted;
	}
	public void setMuted(Boolean muted) {
		this.muted = muted;
	}
	public Boolean getPreload() {
		return preload;
	}
	public void setPreload(Boolean preload) {
		this.preload = preload;
	}
	public Object getPoster() {
		return poster;
	}
	public void setPoster(Object poster) {
		this.poster = poster;
	}
	
	public String getSourceTypes() {
		return sourceTypes;
	}
	public void setSourceTypes(String sourceTypes) {
		this.sourceTypes = sourceTypes;
	}
	
	public void doTag() throws JspException, IOException {
        JspWriter out = getJspContext().getOut();
        Url url = this.prepareUrl();
        
        String sourceTypes[] = null;
        if (this.sourceTypes != null) {
        	sourceTypes = this.sourceTypes.split(",");
        	url.sourceTypes(sourceTypes);
        }
        
        if (this.poster != null) {
        	if (this.poster.equals("false")) {
        		url.poster(false);
        	} else {
        		url.poster(this.poster);
        	}
        }
        
        Map<String, String> attributes = prepareAttributes();
        
        if (sourceTypes == null) sourceTypes = Url.DEFAULT_VIDEO_SOURCE_TYPES;
        for (String sourceType : sourceTypes) {
        	String transformationAttribute = sourceType + "Transformation";
        	if (this.tagAttrs.containsKey(transformationAttribute)) {
        		Transformation transformation = null;
        		Object transformationAttrValue = tagAttrs.remove(transformationAttribute);
        		if (transformationAttrValue instanceof Transformation) {
        			transformation = (Transformation) transformationAttrValue;
        		} else if (transformationAttrValue instanceof Map) {
        			transformation = new Transformation().params((Map) transformationAttrValue);
        		} else {
        			transformation = new Transformation().rawTransformation((String) transformationAttrValue);
        		}
        		url.sourceTransformationFor(sourceType, transformation );
        	}
        }
        
        if (autoplay != null) attributes.put("autoplay", autoplay.toString());
        if (controls != null) attributes.put("controls", controls.toString());
        if (loop != null) attributes.put("loop", loop.toString());
        if (muted != null) attributes.put("muted", muted.toString());
        if (preload != null) attributes.put("preload", preload.toString());
        
        out.println(url.videoTag(attributes));
    }
}
