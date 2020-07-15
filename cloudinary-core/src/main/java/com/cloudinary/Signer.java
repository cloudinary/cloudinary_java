package com.cloudinary;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Signer {
    private final String algorithmName;

    private Signer(String algorithmName) {
        this.algorithmName = algorithmName;
    }

    public byte[] sign(String s) {
        try {
            return MessageDigest.getInstance(algorithmName).digest(Util.getUTF8Bytes(s));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Unexpected exception", e);
        }
    }

    public static final Signer SHA1 = new Signer("SHA-1");
    public static final Signer SHA256 = new Signer("SHA-256");

    public static Signer getByName(String algorithmName) {
        if ("SHA-256".equals(algorithmName)) {
            return Signer.SHA256;
        }

        return Signer.SHA1;
    }

    @Override
    public String toString() {
        return algorithmName;
    }
}
