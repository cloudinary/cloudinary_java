package com.cloudinary.api;

import com.cloudinary.api.exceptions.ApiException;

public class AuthorizationRequired extends ApiException {
    private static final long serialVersionUID = 7160740370855761014L;

    public AuthorizationRequired(String message) {
        super(message);
    }
}