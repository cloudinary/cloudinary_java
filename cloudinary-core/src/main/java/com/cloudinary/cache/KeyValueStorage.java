package com.cloudinary.cache;

public interface KeyValueStorage {
    String get(String key);

    void set(String key, String value);

    void delete(String key);

    boolean flushAll();
}

