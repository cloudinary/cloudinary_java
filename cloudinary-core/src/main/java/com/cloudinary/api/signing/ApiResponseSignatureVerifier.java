package com.cloudinary.api.signing;

import com.cloudinary.Util;
import com.cloudinary.utils.ObjectUtils;
import com.cloudinary.utils.StringUtils;

import static com.cloudinary.api.signing.SigningUtils.safeString;

public class ApiResponseSignatureVerifier {
    private final String secretKey;

    public ApiResponseSignatureVerifier(String secretKey) {
        if (StringUtils.isBlank(secretKey)) {
            throw new IllegalArgumentException("Secret key is required");
        }

        this.secretKey = secretKey;
    }

    public boolean verifySignature(String publicId, String version, String signature) {
        return Util.produceSignature(ObjectUtils.asMap(
                "public_id", safeString(publicId),
                "version", safeString(version)), secretKey).equals(signature);
    }
}
