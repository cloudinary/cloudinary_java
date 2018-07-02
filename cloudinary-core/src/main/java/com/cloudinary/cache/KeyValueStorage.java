package com.cloudinary.cache;

/**
 * A class implementing this interface should be used in combination with {@link KeyValueResponsiveBreakpointsCacheAdapter}
 * To provide a simple responsive breakpoints caching mechanism.
 */
public interface KeyValueStorage {
    /**
     * Get the value associated with the given cache key.
     *
     * @param key The cache key
     * @return The value.
     */
    String get(String key);

    /**
     * Set the value for a given cache key.
     * @param key The cache key
     * @param value The value to cache.
     * @return The value.
     */
    void set(String key, String value);

    /**
     * Delete the value associated with the given cache key
     * @param key Cache key
     */
    void delete(String key);

    /**
     * Clear the cache
     * @return Success
     */
    boolean flushAll();
}

