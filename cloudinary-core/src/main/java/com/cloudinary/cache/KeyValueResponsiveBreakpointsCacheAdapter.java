package com.cloudinary.cache;

import com.cloudinary.utils.StringUtils;

/**
 * A helper class to facilitate the use of breakpoints cache. Initialized with any key/value store this class
 * can be passed to Cloudinary as the breakpoints cache adapter.
 */
public final class KeyValueResponsiveBreakpointsCacheAdapter implements ResponsiveBreakpointsCacheAdapter {
    private final KeyValueStorage storage;

    public KeyValueResponsiveBreakpointsCacheAdapter(KeyValueStorage storage) {
        this.storage = storage;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponsiveBreakpointPayload get(String publicId, String type, String resourceType, String transformation, String format) {
        return deserialize(storage.get(generateKey(publicId, type, resourceType, transformation, format)));
    }

    private ResponsiveBreakpointPayload deserialize(String s) {
        if (StringUtils.isBlank(s)) {
            return null;
        }

        String[] split = s.split(",");
        int[] breakpoints = new int[split.length];
        for (int i = 0; i < breakpoints.length; i++) {
            breakpoints[i] = Integer.parseInt(split[i]);
        }

        return new ResponsiveBreakpointPayload(breakpoints);
    }

    private String serialize(ResponsiveBreakpointPayload value) {
        if (value == null) {
            return null;
        }
        String[] integers = new String[value.getBreakpoints().length];
        for (int i = 0; i < integers.length; i++) {
            integers[i] = String.valueOf(value.getBreakpoints()[i]);
        }

        return StringUtils.join(integers, ",");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void set(String publicId, String type, String resourceType, String transformation, String format, ResponsiveBreakpointPayload value) {
        storage.set(generateKey(publicId, type, resourceType, transformation, format), serialize(value));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(String publicId, String type, String resourceType, String transformation, String format) {
        storage.delete(generateKey(publicId, type, resourceType, transformation, format));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean flushAll() {
        return storage.flushAll();
    }

    private String generateKey(String publicId, String type, String resourceType, String transformation, String format) {
        return StringUtils.sha1(StringUtils.join(new String[]{publicId, type, resourceType, transformation, format}, "/"));
    }
}
