package com.cloudinary.taglib;

import java.util.Map;

import com.cloudinary.Uploader;

public class CloudinaryUnsignedUploadTag extends CloudinaryUploadTag {
	public CloudinaryUnsignedUploadTag() {
		super();
		this.unsigned = true;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected String uploadTag(Uploader uploader, Map options, Map htmlOptions) {
		return uploader.unsignedImageUploadTag(fieldName, uploadPreset, options, htmlOptions);
	}
}
