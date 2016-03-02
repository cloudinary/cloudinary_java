package com.cloudinary.api.exceptions;


public class RateLimited extends ApiException {
    private static final long serialVersionUID = -8298038106172355219L;

    public RateLimited(String message) {
        super(message);
    }
}
