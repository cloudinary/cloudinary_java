package com.cloudinary.test;

import com.cloudinary.cache.KeyValueCacheAdapter;
import com.cloudinary.cache.KeyValueStorage;

import java.util.HashMap;
import java.util.Map;

public class MockMemCache extends KeyValueCacheAdapter {

    MockMemCache() {
        super(new KeyValueStorage() {
            Map<String, String> map = new HashMap<>();

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
        });
    }
}