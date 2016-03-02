package com.cloudinary.api.exceptions;


public class NotAllowed extends ApiException {
    private static final long serialVersionUID = 4371365822491647653L;

    public NotAllowed(String message) {
        super(message);
    }
}