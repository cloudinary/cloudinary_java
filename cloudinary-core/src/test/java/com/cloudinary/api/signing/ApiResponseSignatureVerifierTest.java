package com.cloudinary.api.signing;

import com.cloudinary.Signer;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ApiResponseSignatureVerifierTest {
    @Test
    public void testVerifySignature() {
        ApiResponseSignatureVerifier verifier = new ApiResponseSignatureVerifier("X7qLTrsES31MzxxkxPPA-pAGGfU");

        boolean actual = verifier.verifySignature("tests/logo.png", "1", "08d3107a5b2ad82e7d82c0b972218fbf20b5b1e0");

        assertTrue(actual);
    }

    @Test
    public void testVerifySignatureFail() {
        ApiResponseSignatureVerifier verifier = new ApiResponseSignatureVerifier("X7qLTrsES31MzxxkxPPA-pAGGfU");

        boolean actual = verifier.verifySignature("tests/logo.png", "1", "doesNotMatchForSure");

        assertFalse(actual);
    }

    @Test
    public void testVerifySignatureSHA256() {
        ApiResponseSignatureVerifier verifier = new ApiResponseSignatureVerifier("X7qLTrsES31MzxxkxPPA-pAGGfU", Signer.SHA256);

        boolean actual = verifier.verifySignature("tests/logo.png", "1", "cc69ae4ed73303fbf4a55f2ae5fc7e34ad3a5c387724bfcde447a2957cacdfea");

        assertTrue(actual);
    }
}
