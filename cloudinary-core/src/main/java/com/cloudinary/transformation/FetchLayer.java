package com.cloudinary.transformation;

import com.cloudinary.utils.Base64Coder;

public class FetchLayer extends AbstractLayer<FetchLayer> {

    public FetchLayer() {
        this.type = "fetch";
    }

    public FetchLayer publicId(String publicId) {
        this.publicId = Base64Coder.encodeString(publicId);
        return this;
    }

    @Override
    public FetchLayer type(String type) {
        throw new UnsupportedOperationException("Cannot modify type for fetch layers");
    }

    @Override
    FetchLayer getThis() {
        return this;
    }
}
