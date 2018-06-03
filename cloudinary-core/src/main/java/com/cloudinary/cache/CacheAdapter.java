package com.cloudinary.cache;

public interface CacheAdapter<T> {

    T get(String publicId, String type, String resourceType, String transformation, String format);

    void set(String publicId, String type, String resourceType, String transformation, String format, T value);

    void delete(String publicId, String type, String resourceType, String transformation, String format);

    boolean flushAll();
}
