package com.cloudinary.strategies;

import java.io.IOException;
import java.util.Map;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;

public abstract class AbstractUploaderStrategy {
    protected Uploader uploader;

    public void init(Uploader uploader) {
        this.uploader = uploader;
    }

    public Cloudinary cloudinary() {
        return this.uploader.cloudinary();
    }

    @SuppressWarnings("rawtypes")
    public abstract Map callApi(String action, Map<String, Object> params, Map options, Object file) throws IOException;
}
