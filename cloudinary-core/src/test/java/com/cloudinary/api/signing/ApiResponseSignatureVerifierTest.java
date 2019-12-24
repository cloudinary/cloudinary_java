package com.cloudinary.api.signing;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ApiResponseSignatureVerifierTest {
    @Test
    public void testVerifySignature() {
        ApiResponseSignatureVerifier impl = new ApiResponseSignatureVerifier("X7qLTrsES31MzxxkxPPA-pAGGfU");

        boolean actual = impl.verifySignature("tests/logo.png", "1", "08d3107a5b2ad82e7d82c0b972218fbf20b5b1e0");

        assertTrue(actual);
    }

    @Test
    public void testVerifySignatureFail() {
        ApiResponseSignatureVerifier impl = new ApiResponseSignatureVerifier("X7qLTrsES31MzxxkxPPA-pAGGfU");

        boolean actual = impl.verifySignature("tests/logo.png", "1", "doesNotMatchForSure");

        assertFalse(actual);
    }
}
