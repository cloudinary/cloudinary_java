package com.cloudinary.cache;

import com.cloudinary.utils.StringUtils;

public class KeyValueCacheAdapter implements CacheAdapter<ResponsiveBreakpointPayload> {
    private final KeyValueStorage storage;

    public KeyValueCacheAdapter(KeyValueStorage storage) {
        if (storage == null) {
            throw new IllegalArgumentException("Storage and serializer cannot be null");
        }

        this.storage = storage;
    }

    @Override
    public ResponsiveBreakpointPayload get(String publicId, String type, String resourceType, String transformation, String format) {
        return deserialize(storage.get(generateKey(publicId, type, resourceType, transformation, format)));
    }

    protected ResponsiveBreakpointPayload deserialize(String s) {
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

    protected String serialize(ResponsiveBreakpointPayload value) {
        if (value == null) {
            return null;
        }
        String[] integers = new String[value.getBreakpoints().length];
        for (int i = 0; i < integers.length; i++) {
            integers[i] = String.valueOf(value.getBreakpoints()[i]);
        }

        return StringUtils.join(integers, ",");
    }

    @Override
    public void set(String publicId, String type, String resourceType, String transformation, String format, ResponsiveBreakpointPayload value) {
        storage.set(generateKey(publicId, type, resourceType, transformation, format), serialize(value));
    }

    @Override
    public void delete(String publicId, String type, String resourceType, String transformation, String format) {
        storage.delete(generateKey(publicId, type, resourceType, transformation, format));
    }

    @Override
    public boolean flushAll() {
        return storage.flushAll();
    }

    private String generateKey(String publicId, String type, String resourceType, String transformation, String format) {
        return StringUtils.sha1(StringUtils.join(new String[]{publicId, type, resourceType, transformation, format}, "/"));
    }
}
