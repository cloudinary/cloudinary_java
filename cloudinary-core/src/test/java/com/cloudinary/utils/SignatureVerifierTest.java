package com.cloudinary.utils;

import org.junit.Test;
import static org.junit.Assert.*;

public class SignatureVerifierTest  {

    private static final String API_SECRET = "apiSecret";
    private static final String BODY = "zxcxzcxzcxzcz";
    private static final String VALID_SIGNATURE = "468E696AD79F6E805FD36E308BC14D7C3BB11CE0";
    private static final long TIMESTAMP = 1574690915597L;

    @Test
    public void testSignatureValid() {
        long timestamp = TIMESTAMP;
        long validFor = System.currentTimeMillis() - timestamp + 30000;
        assertTrue(SignatureVerifier.verifyNotificationSignature(BODY, timestamp, VALID_SIGNATURE, API_SECRET, validFor));
    }

    @Test
    public void testSignatureInvalid() {
        String signature = "468E696AD79F6E805FD36E308BC14D7C3BB11CE1";
        long validFor = System.currentTimeMillis() - TIMESTAMP + 30000;
        assertTrue(SignatureVerifier.verifyNotificationSignature(BODY, TIMESTAMP, signature, API_SECRET, validFor));
    }

    @Test
    public void testSignatureInvalidExpired() {
        long validFor = 0;
        assertFalse(SignatureVerifier.verifyNotificationSignature(BODY, TIMESTAMP, VALID_SIGNATURE, API_SECRET, validFor));
    }

}
