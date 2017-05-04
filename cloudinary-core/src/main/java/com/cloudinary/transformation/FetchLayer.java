package com.cloudinary.transformation;

import com.cloudinary.utils.Base64Coder;

public class FetchLayer extends AbstractLayer<FetchLayer> {

    public FetchLayer() {
        this.type = "fetch";
    }

    public FetchLayer url(String remoteUrl) {
        this.publicId = Base64Coder.encodeString(remoteUrl);
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
