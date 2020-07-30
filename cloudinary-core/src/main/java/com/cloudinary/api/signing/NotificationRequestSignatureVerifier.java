package com.cloudinary.api.signing;

import com.cloudinary.SignatureAlgorithm;

import static com.cloudinary.utils.StringUtils.emptyIfNull;

/**
 * The {@code NotificationRequestSignatureVerifier} class is responsible for verifying authenticity and integrity
 * of Cloudinary Upload notifications.
 */
public class NotificationRequestSignatureVerifier {
    private final SignedPayloadValidator signedPayloadValidator;

    /**
     * Initializes new instance of {@code NotificationRequestSignatureVerifier} with secret key value.
     *
     * @param secretKey shared secret key string which is used to sign and verify authenticity of notifications
     */
    public NotificationRequestSignatureVerifier(String secretKey) {
        this.signedPayloadValidator = new SignedPayloadValidator(secretKey, SignatureAlgorithm.SHA1);
    }

    /**
     * Initializes new instance of {@code NotificationRequestSignatureVerifier} with secret key value.
     *
     * @param secretKey shared secret key string which is used to sign and verify authenticity of notifications
     * @param signatureAlgorithm type of hashing algorithm to use for calculation of HMACs
     */
    public NotificationRequestSignatureVerifier(String secretKey, SignatureAlgorithm signatureAlgorithm) {
        this.signedPayloadValidator = new SignedPayloadValidator(secretKey, signatureAlgorithm);
    }

    /**
     * Verifies signature of Cloudinary Upload notification.
     *
     * @param body      notification message body, represented as string
     * @param timestamp value of X-Cld-Timestamp custom HTTP header of notification message, representing notification
     *                  issue timestamp
     * @param signature actual signature value, usually passed via X-Cld-Signature custom HTTP header of notification
     *                  message
     * @return true if notification passed verification procedure
     */
    public boolean verifySignature(String body, String timestamp, String signature) {
        return signedPayloadValidator.validateSignedPayload(
                emptyIfNull(body) + emptyIfNull(timestamp),
                signature);
    }

    /**
     * Verifies signature of Cloudinary Upload notification.
     * <p>
     * Differs from {@link #verifySignature(String, String, String)} in additional validation which consists of making
     * sure the notification being verified is still not expired based on timestamp parameter value.
     *
     * @param body            notification message body, represented as string
     * @param timestamp       value of X-Cld-Timestamp custom HTTP header of notification message, representing notification
     *                        issue timestamp
     * @param signature       actual signature value, usually passed via X-Cld-Signature custom HTTP header of notification
     *                        message
     * @param secondsValidFor the amount of time, in seconds, the notification message is considered valid by client
     * @return true if notification passed verification procedure
     */
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
