package com.cloudinary.api.exceptions;

public class GeneralError extends ApiException {
    private static final long serialVersionUID = 4553362706625067182L;

    public GeneralError(String message) {
        super(message);
    }
}