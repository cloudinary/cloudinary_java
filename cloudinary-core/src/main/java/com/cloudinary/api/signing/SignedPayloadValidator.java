package com.cloudinary.api.signing;

import com.cloudinary.Util;
import com.cloudinary.utils.StringUtils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static com.cloudinary.utils.StringUtils.emptyIfNull;

class SignedPayloadValidator {
    private final String secretKey;
    private final MessageDigest messageDigest;

    SignedPayloadValidator(String secretKey) {
        if (StringUtils.isBlank(secretKey)) {
            throw new IllegalArgumentException("Secret key is required");
        }

        this.secretKey = secretKey;
        this.messageDigest = acquireMessageDigest();
    }

    boolean validateSignedPayload(String signedPayload, String signature) {
        String expectedSignature =
                StringUtils.encodeHexString(
                        messageDigest.digest(Util.getUTF8Bytes(emptyIfNull(signedPayload) + secretKey)));

        return expectedSignature.equals(signature);
    }

    private static MessageDigest acquireMessageDigest() {
        try {
            return MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Unexpected exception", e);
        }
    }
}
