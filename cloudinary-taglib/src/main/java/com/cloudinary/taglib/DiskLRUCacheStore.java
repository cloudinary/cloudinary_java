package com.cloudinary.taglib;

import com.cloudinary.cache.KeyValueStorage;
import com.jakewharton.disklrucache.DiskLruCache;

import java.io.File;
import java.io.IOException;

/**
 * A simple LRU cache implementation of {@link KeyValueStorage}. Use this in combination with {@link com.cloudinary.cache.KeyValueResponsiveBreakpointsCacheAdapter}
 * And pass to Cloudinary to enable a simple responsive breakpoints cache mechanism.
 */
public class DiskLRUCacheStore implements KeyValueStorage {

    private final DiskLruCache lruCache;

    /**
     * Create a new instance of the LRU cache store.
     *
     * @param directory a writable directory to be owned by this cache.
     * @param maxSize   the maximum number of bytes this cache should use to store
     * @throws IOException When cache creation fails
     */
    public DiskLRUCacheStore(File directory, long maxSize) throws IOException {
        lruCache = DiskLruCache.open(directory, 1, 1, maxSize);
    }

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(String key) {
        try {
            lruCache.remove(key);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * {@inheritDoc}
     */
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

