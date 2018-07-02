package com.cloudinary.test;

import com.cloudinary.cache.KeyValueStorage;

import java.util.HashMap;
import java.util.Map;

public class MemoryKeyValueStore implements KeyValueStorage {

    private final Map<String, String> map;

    public MemoryKeyValueStore() {
        map = new HashMap<>();
    }

    @Override
    public String get(String key) {
        return map.get(key);
    }

    @Override
    public void set(String key, String value) {
        map.put(key, value);
    }

    @Override
    public void delete(String key) {
        map.remove(key);
    }

    @Override
    public boolean flushAll() {
        map.clear();
        return true;
    }
}