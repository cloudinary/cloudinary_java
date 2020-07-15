package com.cloudinary.api.signing;

import com.cloudinary.Signer;
import com.cloudinary.utils.StringUtils;

import static com.cloudinary.utils.StringUtils.emptyIfNull;

class SignedPayloadValidator {
    private final String secretKey;
    private final Signer signer;

    SignedPayloadValidator(String secretKey, Signer signatureAlgorithmType) {
        if (StringUtils.isBlank(secretKey)) {
            throw new IllegalArgumentException("Secret key is required");
        }

        this.secretKey = secretKey;
        this.signer = signatureAlgorithmType;
    }

    boolean validateSignedPayload(String signedPayload, String signature) {
        String expectedSignature =
                StringUtils.encodeHexString(signer.sign(emptyIfNull(signedPayload) + secretKey));

        return expectedSignature.equals(signature);
    }
}
