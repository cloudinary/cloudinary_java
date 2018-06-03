package com.cloudinary.taglib;

import com.cloudinary.cache.KeyValueStorage;
import com.jakewharton.disklrucache.DiskLruCache;

import java.io.File;
import java.io.IOException;

public class DiskLRUCacheStore implements KeyValueStorage {

    private final DiskLruCache lruCache;

    public DiskLRUCacheStore(File directory, int appVersion, long maxSize) throws IOException {
        lruCache = DiskLruCache.open(directory, appVersion, 1, maxSize);
    }

    @Override
    public String get(String key) {
        try {
            DiskLruCache.Snapshot snapshot = lruCache.get(key);
            if (snapshot != null) {
                return snapshot.getString(0);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void set(String key, String value) {
        try {
            DiskLruCache.Editor edit = lruCache.edit(key);
            edit.set(0, value);
            edit.commit();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void delete(String key) {
        try {
            lruCache.remove(key);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean flushAll() {
        try {
            lruCache.delete();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }
}

