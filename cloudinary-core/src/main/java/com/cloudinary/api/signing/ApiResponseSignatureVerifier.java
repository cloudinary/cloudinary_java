package com.cloudinary.api.signing;

import com.cloudinary.Util;
import com.cloudinary.utils.ObjectUtils;
import com.cloudinary.utils.StringUtils;

import static com.cloudinary.utils.StringUtils.emptyIfNull;

/**
 * The {@code ApiResponseSignatureVerifier} class is responsible for verifying Cloudinary Upload API response signatures.
 */
public class ApiResponseSignatureVerifier {
    private final String secretKey;

    /**
     * Initializes new instance of {@code ApiResponseSignatureVerifier} class with a secret key required to perform
     * API response signatures verification.
     *
     * @param secretKey shared secret key string which is used to sign and verify authenticity of API responses
     */
    public ApiResponseSignatureVerifier(String secretKey) {
        if (StringUtils.isBlank(secretKey)) {
            throw new IllegalArgumentException("Secret key is required");
        }

        this.secretKey = secretKey;
    }

    /**
     * Checks whether particular Cloudinary Upload API response signature matches expected signature.
     *
     * The task is performed by generating signature using same hashing algorithm as used on Cloudinary servers and
     * comparing the result with provided actual signature.
     *
     * @param publicId public id of uploaded resource as stated in upload API response
     * @param version version of uploaded resource as stated in upload API response
     * @param signature signature of upload API response, usually passed via X-Cld-Signature custom HTTP response header
     *
     * @return true if response signature passed verification procedure
     */
    public boolean verifySignature(String publicId, String version, String signature) {
        return Util.produceSignature(ObjectUtils.asMap(
                "public_id", emptyIfNull(publicId),
                "version", emptyIfNull(version)), secretKey).equals(signature);
    }
}
