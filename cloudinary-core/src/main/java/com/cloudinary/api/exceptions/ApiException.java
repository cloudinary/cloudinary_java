package com.cloudinary.api.exceptions;

public class ApiException extends Exception {
    private static final long serialVersionUID = 4416861825144420038L;

    public ApiException(String message) {
        super(message);
    }
}
