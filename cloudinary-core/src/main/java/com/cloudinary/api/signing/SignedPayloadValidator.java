package com.cloudinary.api.signing;

import com.cloudinary.SignatureAlgorithm;
import com.cloudinary.Util;
import com.cloudinary.utils.StringUtils;

import static com.cloudinary.utils.StringUtils.emptyIfNull;

class SignedPayloadValidator {
    private final String secretKey;
    private final SignatureAlgorithm signatureAlgorithm;

    SignedPayloadValidator(String secretKey, SignatureAlgorithm signatureAlgorithm) {
        if (StringUtils.isBlank(secretKey)) {
            throw new IllegalArgumentException("Secret key is required");
        }

        this.secretKey = secretKey;
        this.signatureAlgorithm = signatureAlgorithm;
    }

    boolean validateSignedPayload(String signedPayload, String signature) {
        String expectedSignature =
                StringUtils.encodeHexString(Util.hash(emptyIfNull(signedPayload) + secretKey,
                        signatureAlgorithm));

        return expectedSignature.equals(signature);
    }
}
