package com.cloudinary.utils;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

public class SignatureVerifier {
    private static final int DEFAULT_EXPIRATION_LENGTH = 7200;

    public static boolean verifyNotificationSignature(String body, long timestamp, String signature, String apiSecret) {
        return verifyNotificationSignature(body, timestamp, signature, apiSecret, DEFAULT_EXPIRATION_LENGTH);
    }

    public static boolean verifyNotificationSignature(String body, long timestamp, String signature, String apiSecret, long validFor) {

        if (System.currentTimeMillis() - timestamp > validFor) {
            return false;
        }

        boolean bodySignatureValid;
        try {
            String payload = body + timestamp + apiSecret;
            bodySignatureValid = CryptoUtil.sha1(payload).equals(signature);
        } catch (NoSuchAlgorithmException e) {
            return false;
        } catch (UnsupportedEncodingException e) {
            return false;
        }

        return bodySignatureValid;
    }
}
