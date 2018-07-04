package com.cloudinary;

import com.cloudinary.cache.ResponsiveBreakpointPayload;
import com.cloudinary.cache.ResponsiveBreakpointsCacheAdapter;

/**
 * This (internal) class gets smart breakpoints from cloudinary, to use as input for the srcset generation.
 * The provider uses a user provided cache adapter and fallbacks to client-side calcluation in case the breakpoints
 * could not be retrieved from Cloudinary.
 */
class ResponsiveBreakpointsProvider {
    private final ResponsiveBreakpointsCacheAdapter cacheAdapter;
    private final Cloudinary cloudinary;

    /**
     * Create a new provider.
     *
     * @param cacheAdapter Cache adapter in case breakpoints caching is desired.
     * @param cloudinary   The cloudinary instance containig the configuration (mainly, the cache adapter, if desired).
     */
    ResponsiveBreakpointsProvider(ResponsiveBreakpointsCacheAdapter cacheAdapter, Cloudinary cloudinary) {
        this.cacheAdapter = cacheAdapter;
        this.cloudinary = cloudinary;
    }

    /**
     * Get the responsive breakpoints for the given resource
     *
     * @param url           The base url to work from, containing all transformations and modifications required to display the image
     * @param cacheKey      Used as the cache key to store the breakpoints data, if a cache is used.
     * @param minWidth      Minimum image width to use for the breakpoints generation
     * @param maxWidth      Maximum image width to use for the breakpoints generation
     * @param bytesStepSize Minimum byte step size to use for the breakpoints generation.
     * @param maxImages     Maximum image count to generate.
     * @return The responsive breakpoints payload (i.e. an array of integers).
     */
    ResponsiveBreakpointPayload get(Url url, CacheKey cacheKey, int minWidth, int maxWidth, int bytesStepSize, int maxImages) {
        ResponsiveBreakpointPayload res = null;

        if (url.cloudinary.config.useResponsiveBreakpointsCache) {
            res = cacheAdapter.get(cacheKey.publicId, cacheKey.type, cacheKey.resourceType, cacheKey.transformation, cacheKey.format);

            if (res == null) {
                // try to fetch data from cloudinary:
                try {
                    res = cloudinary.get().getBreakpoints(url, minWidth, maxWidth, bytesStepSize, maxImages);
                    if (res != null) {
                        set(cacheKey, res);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        if (res == null) {
            // fallback to simple client-side breakpoint calculation
            res = new ResponsiveBreakpointPayload(calculateBreakpoints(minWidth, maxWidth, maxImages));
        }

        return res;
    }

    /**
     * Calculate the breakpoints based on the given constraints. Note: this calculation does not employ Cloudinary's
     * smart breakpoints, it's a local width distribution (max-min)/maxImages). Used as fallback.
     *
     * @param minWidth
     * @param maxWidth
     * @param maxImages
     * @return The array of image widths
     */
    private int[] calculateBreakpoints(int minWidth, int maxWidth, int maxImages) {
        int pixelStepSize = (int) Math.round(Math.ceil((float) (maxWidth - minWidth)) /
                (maxImages > 1 ? maxImages - 1 : maxImages));
        int curr = minWidth;
        int[] breakpoints = new int[maxImages];
        for (int i = 0; i < maxImages; i++) {
            breakpoints[i] = curr;
            curr += pixelStepSize;
        }

        return breakpoints;
    }

    /**
     * Store the payload using the provided cache adapter.
     *
     * @param cacheKey The cache key to use
     * @param value    The payload
     */
    private void set(CacheKey cacheKey, ResponsiveBreakpointPayload value) {
        cacheAdapter.set(cacheKey.publicId, cacheKey.type, cacheKey.resourceType, cacheKey.transformation, cacheKey.format, value);
    }

    static final class CacheKey {
        public String publicId;
        public String type;
        public String resourceType;
        public String transformation;
        public String format;
    }
}