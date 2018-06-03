package com.cloudinary.test;

import com.cloudinary.cache.KeyValueCacheAdapter;
import com.cloudinary.cache.ResponsiveBreakpointPayload;
import com.cloudinary.taglib.DiskLRUCacheStore;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class FileStoreTest {
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void testDiskLRUCacheStore() throws IOException {
        File createdFolder = folder.newFolder("cache");
        DiskLRUCacheStore storage = new DiskLRUCacheStore(createdFolder, 1, 10 * 1024 * 1024);
        KeyValueCacheAdapter cacheAdapter = new KeyValueCacheAdapter(storage);
        ResponsiveBreakpointPayload value = new ResponsiveBreakpointPayload(new int[]{3, 12, 222, 400, 1000});
        cacheAdapter.set("publicId", "type", "resourceType", "transformation", "format", value);
        ResponsiveBreakpointPayload cachedValue = cacheAdapter.get("publicId", "type", "resourceType", "transformation", "format");
        Assert.assertTrue(Arrays.equals(value.getBreakpoints(), cachedValue.getBreakpoints()));

        cacheAdapter.delete("publicId", "type", "resourceType", "transformation", "format");
        cachedValue = cacheAdapter.get("publicId", "type", "resourceType", "transformation", "format");
        Assert.assertNull(cachedValue);

        value = new ResponsiveBreakpointPayload(new int[]{30, 40, 50, 1000});
        cacheAdapter.set("publicId2", "type", "resourceType", "transformation", "format", value);
        cachedValue = cacheAdapter.get("publicId2", "type", "resourceType", "transformation", "format");
        Assert.assertTrue(Arrays.equals(value.getBreakpoints(), cachedValue.getBreakpoints()));
    }
}
