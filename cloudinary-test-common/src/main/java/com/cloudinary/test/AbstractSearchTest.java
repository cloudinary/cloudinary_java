package com.cloudinary.test;

import com.cloudinary.Api;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.junit.*;
import org.junit.rules.TestName;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assume.assumeNotNull;

@SuppressWarnings({"rawtypes", "unchecked", "JavaDoc"})
abstract public class AbstractSearchTest extends MockableTest {
    @Rule
    public TestName currentTest = new TestName();
    private static final String SEARCH_TAG = "search_test" + SUFFIX;
    private static final String API_TEST = "api_test_" + SUFFIX;
    private static final String API_TEST_1 = API_TEST + "_1";
    private static final String API_TEST_2 = API_TEST + "_2";

    @BeforeClass
    public static void setUpClass() throws Exception {
        Cloudinary cloudinary = new Cloudinary();
        Map options = ObjectUtils.asMap("public_id", API_TEST, "tags", new String[]{SDK_TEST_TAG, SEARCH_TAG, uniqueTag}, "context", "stage=in_review");
        cloudinary.api().deleteResourcesByTag(SEARCH_TAG, null);
        cloudinary.uploader().upload(SRC_TEST_IMAGE, options);
        options = ObjectUtils.asMap("public_id", API_TEST_1, "tags", new String[]{SDK_TEST_TAG, SEARCH_TAG, uniqueTag}, "context", "stage=new");
        cloudinary.uploader().upload(SRC_TEST_IMAGE, options);
        options = ObjectUtils.asMap("public_id", API_TEST_2, "tags", new String[]{SDK_TEST_TAG, SEARCH_TAG, uniqueTag}, "context", "stage=validated");
        cloudinary.uploader().upload(SRC_TEST_IMAGE, options);
        try {
            Thread.sleep(3000); //wait for search indexing
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        Api api = MockableTest.cleanUp();
        Cloudinary cloudinary = new Cloudinary();
        cloudinary.api().deleteResourcesByTag(SEARCH_TAG, null);
    }

    @Before
    public void setUp() {
        System.out.println("Running " + this.getClass().getName() + "." + currentTest.getMethodName());
        this.cloudinary = new Cloudinary();
        assumeNotNull(cloudinary.config.apiSecret);
    }

    @Test
    public void shouldFindResourcesByTag() throws Exception {
        Map result = cloudinary.search().expression(String.format("tags:%s", SEARCH_TAG)).execute();
        List<Map> resources = (List<Map>) result.get("resources");
        assertEquals(3, resources.size());
    }

    @Test
    public void shouldFindResourceByPublicId() throws Exception {
        Map result = cloudinary.search().expression(String.format("public_id:%s", API_TEST_1)).execute();
        List<Map> resources = (List<Map>) result.get("resources");
        assertEquals(1, resources.size());
    }

    @Test
    public void shouldPaginateResourcesLimitedByTagAndOrderdByAscendingPublicId() throws Exception {
        List<Map> resources;
        Map result = cloudinary.search().maxResults(1).expression(String.format("tags:%s", SEARCH_TAG)).sortBy("public_id", "asc").execute();
        resources = (List<Map>) result.get("resources");

        assertEquals(1, resources.size());
        assertEquals(3, result.get("total_count"));
        assertEquals(API_TEST, resources.get(0).get("public_id"));


        result = cloudinary.search().maxResults(1).expression(String.format("tags:%s", SEARCH_TAG)).sortBy("public_id", "asc")
                .nextCursor(ObjectUtils.asString(result.get("next_cursor"))).execute();
        resources = (List<Map>) result.get("resources");

        assertEquals(1, resources.size());
        assertEquals(3, result.get("total_count"));
        assertEquals(API_TEST_1, resources.get(0).get("public_id"));

        result = cloudinary.search().maxResults(1).expression(String.format("tags:%s", SEARCH_TAG)).sortBy("public_id", "asc")
                .nextCursor(ObjectUtils.asString(result.get("next_cursor"))).execute();
        resources = (List<Map>) result.get("resources");

        assertEquals(1, resources.size());
        assertEquals(3, result.get("total_count"));
        assertEquals(API_TEST_2, resources.get(0).get("public_id"));
        assertNull(result.get("next_cursor"));


    }
}
