package com.cloudinary.cache;

/**
 * A ResponsiveBreakpointPayload cache adapter interface used by cloudinary. A class implementing this interface can
 * be passed when Creating a new Cloudinary instance to support responsive breakpoints caching.
 */
public interface ResponsiveBreakpointsCacheAdapter {
    /**
     * Get the cache payload based on all components of the url as cache key
     *
     * @param publicId
     * @param type
     * @param resourceType
     * @param transformation
     * @param format
     * @return The responsive breakpoints of the given resource with the given configuration
     */
    ResponsiveBreakpointPayload get(String publicId, String type, String resourceType, String transformation, String format);

    /**
     * Store the value based on all the parameters as cache key components.
     *
     * @param publicId
     * @param type
     * @param resourceType
     * @param transformation
     * @param format
     * @param value          The breakpoints to cache.
     */
    void set(String publicId, String type, String resourceType, String transformation, String format, ResponsiveBreakpointPayload value);

    /**
     * Delete the cache value based on the components as cache key
     *
     * @param publicId
     * @param type
     * @param resourceType
     * @param transformation
     * @param format
     */
    void delete(String publicId, String type, String resourceType, String transformation, String format);

    /**
     * Clean up all the cache.
     *
     * @return Success s
     */
    boolean flushAll();
}
