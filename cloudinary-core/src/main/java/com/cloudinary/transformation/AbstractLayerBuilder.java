package com.cloudinary.transformation;

import java.util.ArrayList;

import com.cloudinary.utils.StringUtils;

public abstract class AbstractLayerBuilder<SELF extends AbstractLayerBuilder<SELF>> {
	abstract SELF self();

	protected String resourceType = null;
	protected String type = null;
	protected String publicId = null;
	protected String format = null;

	public SELF resourceType(String resourceType) {
		this.resourceType = resourceType;
		return self();
	}

	public SELF type(String type) {
		this.type = type;
		return self();
	}

	public SELF publicId(String publicId) {
		this.publicId = publicId.replace('/', ':');
		return self();
	}

	public SELF format(String format) {
		this.format = format;
		return self();
	}
	
	@Override
	public String toString() {
		ArrayList<String> components = new ArrayList<String>();

		if (this.resourceType != null && !this.resourceType.equals("image")) {
			components.add(this.resourceType);
		}

		if (this.type != null && !this.type.equals("upload")) {
			components.add(this.type);
		}

		if (this.publicId == null) {
			throw new IllegalArgumentException("Must supply publicId");
		}

		components.add(formattedPublicId());

		return StringUtils.join(components, ":");
	}

	protected String formattedPublicId() {
		String transientPublicId = this.publicId;

		if (this.format != null) {
			transientPublicId = transientPublicId + "." + this.format;
		}

		return transientPublicId;
	}
}
