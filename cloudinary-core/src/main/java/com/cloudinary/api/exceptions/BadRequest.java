package com.cloudinary.api.exceptions;


public class BadRequest extends ApiException {
    private static final long serialVersionUID = 1410136354253339531L;

    public BadRequest(String message) {
        super(message);
    }
}