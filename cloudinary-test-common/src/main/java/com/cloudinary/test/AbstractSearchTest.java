package com.cloudinary.test;

import com.cloudinary.Cloudinary;
import com.cloudinary.Search;
import com.cloudinary.utils.ObjectUtils;
import org.junit.*;
import org.junit.rules.TestName;

import java.lang.reflect.Field;
import java.util.*;

import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.core.AllOf.allOf;
import static org.junit.Assert.*;
import static org.junit.Assume.assumeNotNull;

@SuppressWarnings({"rawtypes", "unchecked", "JavaDoc"})
abstract public class AbstractSearchTest extends MockableTest {
    @Rule
    public TestName currentTest = new TestName();
    private static final String SEARCH_TAG = "search_test_tag_" + SUFFIX;
    public static final String[] UPLOAD_TAGS = {SDK_TEST_TAG, SEARCH_TAG};
    private static final String SEARCH_TEST = "search_test_" + SUFFIX;
    private static final String SEARCH_FOLDER = "search_folder_" + SUFFIX;
    private static final String SEARCH_TEST_1 = SEARCH_TEST + "_1";
    private static final String SEARCH_TEST_2 = SEARCH_TEST + "_2";
    private static String SEARCH_TEST_ASSET_ID_1;

    @BeforeClass
    public static void setUpClass() throws Exception {
        Cloudinary cloudinary = new Cloudinary();
        Map options = ObjectUtils.asMap("public_id", SEARCH_TEST, "tags", UPLOAD_TAGS, "context", "stage=in_review");
        cloudinary.api().deleteResourcesByTag(SEARCH_TAG, null);
        cloudinary.uploader().upload(SRC_TEST_IMAGE, options);
        options = ObjectUtils.asMap("public_id", SEARCH_TEST_1, "tags", UPLOAD_TAGS, "context", "stage=new");
        SEARCH_TEST_ASSET_ID_1 = cloudinary.uploader().upload(SRC_TEST_IMAGE, options).get("asset_id").toString();
        options = ObjectUtils.asMap("public_id", SEARCH_TEST_2, "tags", UPLOAD_TAGS, "context", "stage=validated");
        cloudinary.uploader().upload(SRC_TEST_IMAGE, options);
        try {
            Thread.sleep(5000); //wait for search indexing
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        Cloudinary cloudinary = new Cloudinary();
        cloudinary.api().deleteResourcesByTag(SEARCH_TAG, null);
        try {
            cloudinary.api().deleteFolder(SEARCH_FOLDER, null);
        } catch (Exception e){
            System.err.println(e.getMessage());
        }
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
    public void shouldFindFolders() throws Exception {
        cloudinary.api().createFolder(SEARCH_FOLDER, null);
        Map result = cloudinary.searchFolders().expression(String.format("name:%s", SEARCH_FOLDER)).execute();
        final List<Map> folders = (List) result.get("folders");
        assertThat(folders, hasItem(hasEntry("name", SEARCH_FOLDER)));
    }

    @Test
    public void shouldFindResourceByPublicId() throws Exception {
        Map result = cloudinary.search().expression(String.format("public_id:%s", SEARCH_TEST_1)).execute();
        List<Map> resources = (List<Map>) result.get("resources");
        assertEquals(1, resources.size());
    }

    @Test
    public void shouldFindResourceByAssetId() throws Exception {
        Map result = cloudinary.search().expression(String.format("asset_id:%s", SEARCH_TEST_ASSET_ID_1)).execute();
        List<Map> resources = (List<Map>) result.get("resources");
        assertEquals(1, resources.size());
    }

    @Test
    public void testShouldNotDuplicateValues() throws Exception {
        Search request = cloudinary.search().maxResults(1).
                sortBy("created_at", "asc")
                .sortBy("created_at", "desc")
                .sortBy("public_id", "asc")
                .aggregate("format")
                .aggregate("format")
                .aggregate("resource_type")
                .withField("context")
                .withField("context")
                .withField("tags");
        Field[] fields = Search.class.getDeclaredFields();
        for(Field field : fields) {
            if(field.getName() == "aggregateParam") {
                field.setAccessible(true);
                ArrayList<String> aggregateList = (ArrayList<String>) field.get(request);
                Set<String> testSet = new HashSet<String>(aggregateList);
                assertTrue(aggregateList.size() == testSet.size());
            }
            if (field.getName() == "withFieldParam") {
                field.setAccessible(true);
                ArrayList<String> withFieldList = (ArrayList<String>) field.get(request);
                Set<String> testSet = new HashSet<String>(withFieldList);
                assertTrue(withFieldList.size() == testSet.size());
            }
            if (field.getName() == "sortByParam") {
                field.setAccessible(true);
                ArrayList<HashMap<String, Object>> sortByList = (ArrayList<HashMap<String, Object>>) field.get(request);
                Set<HashMap<String, Object>> testSet = new HashSet<HashMap<String, Object>>(sortByList);
                assertTrue(sortByList.size() == testSet.size());
            }
        }
    }

    @Test
    public void shouldPaginateResourcesLimitedByTagAndOrderdByAscendingPublicId() throws Exception {
        List<Map> resources;
        Map result = cloudinary.search().maxResults(1).expression(String.format("tags:%s", SEARCH_TAG)).sortBy("public_id", "asc").execute();
        resources = (List<Map>) result.get("resources");
        assertEquals(1, resources.size());
        assertEquals(3, result.get("total_count"));
        assertEquals(SEARCH_TEST, resources.get(0).get("public_id"));


        result = cloudinary.search().maxResults(1).expression(String.format("tags:%s", SEARCH_TAG)).sortBy("public_id", "asc")
                .nextCursor(ObjectUtils.asString(result.get("next_cursor"))).execute();
        resources = (List<Map>) result.get("resources");

        assertEquals(1, resources.size());
        assertEquals(3, result.get("total_count"));
        assertEquals(SEARCH_TEST_1, resources.get(0).get("public_id"));

        result = cloudinary.search().maxResults(1).expression(String.format("tags:%s", SEARCH_TAG)).sortBy("public_id", "asc")
                .nextCursor(ObjectUtils.asString(result.get("next_cursor"))).execute();
        resources = (List<Map>) result.get("resources");

        assertEquals(1, resources.size());
        assertEquals(3, result.get("total_count"));
        assertEquals(SEARCH_TEST_2, resources.get(0).get("public_id"));
        assertNull(result.get("next_cursor"));


    }
}
