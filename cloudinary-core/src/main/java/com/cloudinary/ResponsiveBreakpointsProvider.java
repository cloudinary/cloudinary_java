package com.cloudinary;

import com.cloudinary.cache.CacheAdapter;
import com.cloudinary.cache.ResponsiveBreakpointPayload;

public class ResponsiveBreakpointsProvider {
    private final CacheAdapter<ResponsiveBreakpointPayload> cacheAdapter;
    private final Cloudinary cloudinary;

    public ResponsiveBreakpointsProvider(CacheAdapter<ResponsiveBreakpointPayload> cacheAdapter, Cloudinary cloudinary) {
        this.cacheAdapter = cacheAdapter;
        this.cloudinary = cloudinary;
    }

    public ResponsiveBreakpointPayload get(Url url, CacheKey cacheKey, int minWidth, int maxWidth, int bytesStepSize, int maxImages) {
        ResponsiveBreakpointPayload res = null;

        res = cacheAdapter.get(cacheKey.publicId, cacheKey.type, cacheKey.resourceType, cacheKey.transformation, cacheKey.format);

        if (res == null) {
            // try to fetch data from cloudinary:
            try {
                res = cloudinary.get().getBreakpoints(url, minWidth, maxWidth, bytesStepSize, maxImages);
                set(cacheKey, res);
            } catch (Exception e) {
                // TODO
                e.printStackTrace();
            }
        }

        return res;
    }

    public void set(CacheKey cacheKey, ResponsiveBreakpointPayload value) {
        cacheAdapter.set(cacheKey.publicId, cacheKey.type, cacheKey.resourceType, cacheKey.transformation, cacheKey.format, value);
    }

    public static final class CacheKey {
        public String publicId;
        public String type;
        public String resourceType;
        public String transformation;
        public String format;
    }
}