package com.cloudinary;

/**
 * Defines a callback for network operations.
 */
public interface ProgressCallback {
    /**
     * Invoked during network operation.
     * @param bytesUploaded the number of bytes uploaded so far
     * @param totalBytes the total number of byte to upload - if known
     */
    void onProgress(long bytesUploaded, long totalBytes);
}
