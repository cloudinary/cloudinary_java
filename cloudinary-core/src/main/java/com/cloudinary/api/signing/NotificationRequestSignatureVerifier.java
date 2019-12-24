package com.cloudinary.api.signing;

import static com.cloudinary.api.signing.SigningUtils.safeString;

public class NotificationRequestSignatureVerifier {
    private final SignedPayloadValidator signedPayloadValidator;

    public NotificationRequestSignatureVerifier(String secretKey) {
        this.signedPayloadValidator = new SignedPayloadValidator(secretKey);
    }

    public boolean verifySignature(String body, String timestamp, String signature) {
        return signedPayloadValidator.validateSignedPayload(
                safeString(body) + safeString(timestamp),
                signature);
    }

    public boolean verifySignature(String body, String timestamp, String signature, long secondsValidFor) {
        long parsedTimestamp;
        try {
            parsedTimestamp = Long.parseLong(timestamp);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Provided timestamp is not a valid number", e);
        }

        return verifySignature(body, timestamp, signature) &&
                (System.currentTimeMillis() - parsedTimestamp <= secondsValidFor * 1000L);
    }

}
