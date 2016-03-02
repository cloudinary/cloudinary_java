package com.cloudinary.api.exceptions;

public class AlreadyExists extends ApiException {
    private static final long serialVersionUID = 999568182896607322L;

    public AlreadyExists(String message) {
        super(message);
    }
}