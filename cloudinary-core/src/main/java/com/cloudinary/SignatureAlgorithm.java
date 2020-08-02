package com.cloudinary;

/**
 * Defines supported algorithms for generating/verifying hashed message authentication codes (HMAC).
 */
public enum SignatureAlgorithm {
    SHA1("SHA-1"),
    SHA256("SHA-256");

    private final String algorithmId;

    SignatureAlgorithm(String algorithmId) {
        this.algorithmId = algorithmId;
    }

    public String getAlgorithmId() {
        return algorithmId;
    }
}
