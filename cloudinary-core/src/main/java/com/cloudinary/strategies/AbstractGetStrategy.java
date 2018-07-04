package com.cloudinary.strategies;

import com.cloudinary.Cloudinary;

import java.io.IOException;

public abstract class AbstractGetStrategy {
    protected Cloudinary cloudinary;

    public void init(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    public abstract String get(String url) throws IOException;
}
