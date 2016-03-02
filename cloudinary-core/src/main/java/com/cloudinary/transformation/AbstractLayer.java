package com.cloudinary.transformation;

import java.util.ArrayList;

import com.cloudinary.utils.StringUtils;

public abstract class AbstractLayer<T extends AbstractLayer<T>> {
    abstract T getThis();

    protected String resourceType = null;
    protected String type = null;
    protected String publicId = null;
    protected String format = null;

    public T resourceType(String resourceType) {
        this.resourceType = resourceType;
        return getThis();
    }

    public T type(String type) {
        this.type = type;
        return getThis();
    }

    public T publicId(String publicId) {
        this.publicId = publicId.replace('/', ':');
        return getThis();
    }

    public T format(String format) {
        this.format = format;
        return getThis();
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
