package com.cloudinary.test;

import com.cloudinary.Api;
import com.cloudinary.Cloudinary;
import com.cloudinary.api.ApiResponse;
import com.cloudinary.utils.ObjectUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.util.List;

import static org.junit.Assert.*;
import static org.junit.Assume.assumeNotNull;

@SuppressWarnings({"rawtypes"})
abstract public class AbstractFoldersApiTest extends MockableTest {
    protected Api api;

    @Rule
    public TestName currentTest = new TestName();

    @Before
    public void setUp() {
        System.out.println("Running " + this.getClass().getName() + "." + currentTest.getMethodName());
        this.cloudinary = new Cloudinary();
        assumeNotNull(cloudinary.config.apiSecret);
        this.api = cloudinary.api();
    }

    @Test
    public void testRootFolderWithParams() throws Exception {
        String rootFolder1Name = "rootFolderWithParamsTest1" + SUFFIX;
        assertTrue((Boolean) api.createFolder(rootFolder1Name, null).get("success"));

        String rootFolder2Name = "rootFolderWithParamsTest2" + SUFFIX;
        assertTrue((Boolean) api.createFolder(rootFolder2Name, null).get("success"));

        Thread.sleep(2000);

        ApiResponse rootResponse1 = api.rootFolders(ObjectUtils.asMap("max_results", 1));
        List rootFolders1 = (List) rootResponse1.get("folders");
        assertNotNull(rootFolders1);
        assertEquals(1, rootFolders1.size());

        String nextCursor = (String) rootResponse1.get("next_cursor");
        assertNotNull(nextCursor);

        ApiResponse rootResponse2 = api.rootFolders(ObjectUtils.asMap("max_results", 1, "next_cursor", nextCursor));
        List folders2 = (List) rootResponse2.get("folders");
        assertNotNull(folders2);
        assertEquals(1, folders2.size());

        assertTrue(((List) api.deleteFolder(rootFolder1Name, null).get("deleted")).contains(rootFolder1Name));
        assertTrue(((List) api.deleteFolder(rootFolder2Name, null).get("deleted")).contains(rootFolder2Name));
    }

    @Test
    public void testSubFolderWithParams() throws Exception {
        String rootFolderName = "subfolderWithParamsTest" + SUFFIX;
        assertTrue((Boolean) api.createFolder(rootFolderName, null).get("success"));

        String subFolder1Name = rootFolderName + "/subfolder1" + SUFFIX;
        assertTrue((Boolean) api.createFolder(subFolder1Name, null).get("success"));

        String subFolder2Name = rootFolderName + "/subfolder2" + SUFFIX;
        assertTrue((Boolean) api.createFolder(subFolder2Name, null).get("success"));

        Thread.sleep(2000);

        ApiResponse response = api.subFolders(rootFolderName, ObjectUtils.asMap("max_results", 1));
        List folders = (List) response.get("folders");
        assertNotNull(folders);
        assertEquals(1, folders.size());

        String nextCursor = (String) response.get("next_cursor");
        assertNotNull(nextCursor);

        ApiResponse response2 = api.subFolders(rootFolderName, ObjectUtils.asMap("max_results", 1, "next_cursor", nextCursor));
        List folders2 = (List) response2.get("folders");
        assertNotNull(folders2);
        assertEquals(1, folders2.size());

        ApiResponse result = api.deleteFolder(rootFolderName, null);
        assertTrue(((List) result.get("deleted")).contains(rootFolderName));
    }

    @Test
    public void testDeleteFolderWithSkipBackup() throws Exception {
        //Create
        String rootFolderName = "subfolderWithParamsTest" + SUFFIX;
        assertTrue((Boolean) api.createFolder(rootFolderName, null).get("success"));

        //Delete
        ApiResponse result = api.deleteFolder(rootFolderName, ObjectUtils.asMap("skip_backup", "true"));
        assertTrue(((List) result.get("deleted")).contains(rootFolderName));


    }
}
