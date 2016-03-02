package com.cloudinary.api.exceptions;


public class NotFound extends ApiException {
    private static final long serialVersionUID = -2072640462778940357L;

    public NotFound(String message) {
        super(message);
    }
}