package com.cloudinary.test;

import com.cloudinary.cache.KeyValueResponsiveBreakpointsCacheAdapter;
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
        DiskLRUCacheStore storage = new DiskLRUCacheStore(createdFolder, 10 * 1024 * 1024);
        KeyValueResponsiveBreakpointsCacheAdapter cacheAdapter = new KeyValueResponsiveBreakpointsCacheAdapter(storage);

        // Cache a payload, retrieve it and check equality
        ResponsiveBreakpointPayload value = new ResponsiveBreakpointPayload(new int[]{3, 12, 222, 400, 1000});
        cacheAdapter.set("publicId", "type", "resourceType", "transformation", "format", value);
        ResponsiveBreakpointPayload cachedValue = cacheAdapter.get("publicId", "type", "resourceType", "transformation", "format");

        // verify the value is identical:
        Assert.assertTrue(Arrays.equals(value.getBreakpoints(), cachedValue.getBreakpoints()));

        // verify the references are different:
        Assert.assertNotEquals(System.identityHashCode(value), System.identityHashCode(cachedValue));

        // verify deletion works
        cacheAdapter.delete("publicId", "type", "resourceType", "transformation", "format");
        cachedValue = cacheAdapter.get("publicId", "type", "resourceType", "transformation", "format");
        Assert.assertNull(cachedValue);
    }
}
