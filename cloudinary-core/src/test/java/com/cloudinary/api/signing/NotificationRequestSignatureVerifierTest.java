package com.cloudinary.api.signing;

import com.cloudinary.Signer;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class NotificationRequestSignatureVerifierTest {
    @Test
    public void testVerifySignature() {
        NotificationRequestSignatureVerifier verifier = new NotificationRequestSignatureVerifier("someApiSecret");

        boolean actual = verifier.verifySignature(
                "{}",
                "0",
                "f9aa4471d2a88ff244424cca2444edf7d7ac3596");

        assertTrue(actual);
    }

    @Test
    public void testVerifySignatureFailWhenSignatureDoesntMatch() {
        NotificationRequestSignatureVerifier verifier = new NotificationRequestSignatureVerifier("someApiSecret");

        boolean actual = verifier.verifySignature(
                "{}",
                "0",
                "notMatchingForSure");

        assertFalse(actual);
    }

    @Test
    public void testVerifySignatureFailWhenTooOld() {
        NotificationRequestSignatureVerifier verifier = new NotificationRequestSignatureVerifier("someApiSecret");

        boolean actual = verifier.verifySignature(
                "{}",
                "0",
                "f9aa4471d2a88ff244424cca2444edf7d7ac3596",
                1000L);

        assertFalse(actual);
    }

    @Test
    public void testVerifySignaturePassWhenStillValid() {
        NotificationRequestSignatureVerifier verifier = new NotificationRequestSignatureVerifier("someApiSecret");

        boolean actual = verifier.verifySignature(
                "{}",
                "0",
                "f9aa4471d2a88ff244424cca2444edf7d7ac3596",
                Long.MAX_VALUE / 1000L);

        assertTrue(actual);
    }

    @Test
    public void testVerifySignatureFailWhenStillValidButSignatureDoesntMatch() {
        NotificationRequestSignatureVerifier verifier = new NotificationRequestSignatureVerifier("someApiSecret");

        boolean actual = verifier.verifySignature(
                "{}",
                "0",
                "notMatchingForSure",
                Long.MAX_VALUE / 1000L);

        assertFalse(actual);
    }

    @Test
    public void testVerifySignatureSHA256() {
        NotificationRequestSignatureVerifier verifier = new NotificationRequestSignatureVerifier("someApiSecret", Signer.SHA256);

        boolean actual = verifier.verifySignature(
                "{}",
                "0",
                "d5497e1a206ad0ba29ad09a7c0c5f22e939682d15009c15ab3199f62fefbd14b");

        assertTrue(actual);
    }
}
