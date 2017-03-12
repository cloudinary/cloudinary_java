package com.cloudinary.strategies;

public interface ProgressCallback {
    void onProgress(long bytesUploaded, long totalBytes);
}
