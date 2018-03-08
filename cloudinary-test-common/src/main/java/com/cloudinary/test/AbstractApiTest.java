package com.cloudinary.test;

import com.cloudinary.*;
import com.cloudinary.api.ApiResponse;
import com.cloudinary.api.exceptions.BadRequest;
import com.cloudinary.api.exceptions.NotFound;
import com.cloudinary.transformation.TextLayer;
import com.cloudinary.utils.ObjectUtils;
import org.junit.*;
import org.junit.rules.TestName;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.*;
import static org.junit.Assume.assumeNotNull;

@SuppressWarnings({"rawtypes", "unchecked", "JavaDoc"})
abstract public class AbstractApiTest extends MockableTest {
    private static final String API_TEST = "api_test_" + SUFFIX;
    private static final String API_TEST_1 = API_TEST + "_1";
    private static final String API_TEST_2 = API_TEST + "_2";
    private static final String API_TEST_3 = API_TEST + "_3";
    private static final String API_TEST_5 = API_TEST + "_5";
    public static final String API_TEST_TRANSFORMATION = "api_test_transformation_" + SUFFIX;
    public static final String API_TEST_TRANSFORMATION_2 = API_TEST_TRANSFORMATION + "2";
    public static final String API_TEST_TRANSFORMATION_3 = API_TEST_TRANSFORMATION + "3";
    public static final String API_TEST_UPLOAD_PRESET = "api_test_upload_preset_" + SUFFIX;
    public static final String API_TEST_UPLOAD_PRESET_2 = API_TEST_UPLOAD_PRESET + "2";
    public static final String API_TEST_UPLOAD_PRESET_3 = API_TEST_UPLOAD_PRESET + "3";
    public static final String API_TEST_UPLOAD_PRESET_4 = API_TEST_UPLOAD_PRESET + "4";
    public static final String API_TAG = SDK_TEST_TAG + "_api";
    public static final String DIRECTION_TAG = SDK_TEST_TAG + "_api_resource_direction";
    public static final String[] UPLOAD_TAGS = {SDK_TEST_TAG, API_TAG};
    public static final String EXPLICIT_TRANSFORMATION_NAME = "c_scale,l_text:Arial_60:" + SUFFIX + ",w_100";
    public static final Transformation EXPLICIT_TRANSFORMATION = new Transformation().width(100).crop("scale").overlay(new TextLayer().text(SUFFIX).fontFamily("Arial").fontSize(60));
    public static final String UPDATE_TRANSFORMATION_NAME = "c_scale,l_text:Arial_60:" + SUFFIX + "_update,w_100";
    public static final Transformation UPDATE_TRANSFORMATION = new Transformation().width(100).crop("scale").overlay(new TextLayer().text(SUFFIX + "_update").fontFamily("Arial").fontSize(60));
    public static final String DELETE_TRANSFORMATION_NAME = "c_scale,l_text:Arial_60:" + SUFFIX + "_delete,w_100";
    public static final Transformation DELETE_TRANSFORMATION = new Transformation().width(100).crop("scale").overlay(new TextLayer().text(SUFFIX + "_delete").fontFamily("Arial").fontSize(60));
    public static final String TEST_KEY = "test-key" + SUFFIX;
    public static final String API_TEST_RESTORE = "api_test_restore" + SUFFIX;

    protected Api api;

    @BeforeClass
    public static void setUpClass() throws IOException {
        Cloudinary cloudinary = new Cloudinary();
        if (cloudinary.config.apiSecret == null) {
            System.err.println("Please setup environment for Upload test to run");
            return;
        }

        List<String> uploadAndDirectionTag = new ArrayList<String>(Arrays.asList(UPLOAD_TAGS));
        uploadAndDirectionTag.add(DIRECTION_TAG);

        Map options = ObjectUtils.asMap("public_id", API_TEST, "tags", uploadAndDirectionTag, "context", "key=value", "eager",
                Collections.singletonList(EXPLICIT_TRANSFORMATION));
        cloudinary.uploader().upload(SRC_TEST_IMAGE, options);

        options.put("public_id", API_TEST_1);
        cloudinary.uploader().upload(SRC_TEST_IMAGE, options);
        options.remove("public_id");

        options.put("eager", Collections.singletonList(UPDATE_TRANSFORMATION));
        cloudinary.uploader().upload(SRC_TEST_IMAGE, options);

        options.put("eager", Collections.singletonList(DELETE_TRANSFORMATION));
        cloudinary.uploader().upload(SRC_TEST_IMAGE, options);

        String context1 = TEST_KEY + "=alt";
        String context2 = TEST_KEY + "=alternate";

        options = ObjectUtils.asMap("public_id", "context_1" + SUFFIX, "tags", uploadAndDirectionTag, "context", context1);
        cloudinary.uploader().upload(SRC_TEST_IMAGE, options);

        options = ObjectUtils.asMap("public_id", "context_2" + SUFFIX, "tags", uploadAndDirectionTag, "context", context2);
        cloudinary.uploader().upload(SRC_TEST_IMAGE, options);
    }

    @AfterClass
    public static void tearDownClass() {
        Api api = new Cloudinary().api();
        try {
            api.deleteResourcesByTag(API_TAG, ObjectUtils.emptyMap());
        } catch (Exception ignored) {
        }
        try {
            api.deleteTransformation(API_TEST_TRANSFORMATION, ObjectUtils.emptyMap());
        } catch (Exception ignored) {
        }
        try {
            api.deleteTransformation(API_TEST_TRANSFORMATION_2, ObjectUtils.emptyMap());
        } catch (Exception ignored) {
        }
        try {
            api.deleteTransformation(API_TEST_TRANSFORMATION_3, ObjectUtils.emptyMap());
        } catch (Exception ignored) {
        }
        try {
            api.deleteUploadPreset(API_TEST_UPLOAD_PRESET, ObjectUtils.emptyMap());
        } catch (Exception ignored) {
        }
        try {
            api.deleteUploadPreset(API_TEST_UPLOAD_PRESET_2, ObjectUtils.emptyMap());
        } catch (Exception ignored) {
        }
        try {
            api.deleteUploadPreset(API_TEST_UPLOAD_PRESET_3, ObjectUtils.emptyMap());
        } catch (Exception ignored) {
        }
        try {
            api.deleteUploadPreset(API_TEST_UPLOAD_PRESET_4, ObjectUtils.emptyMap());
        } catch (Exception ignored) {
        }

    }

    @Rule
    public TestName currentTest = new TestName();

    @Before
    public void setUp() {
        System.out.println("Running " + this.getClass().getName() + "." + currentTest.getMethodName());
        this.cloudinary = new Cloudinary();
        assumeNotNull(cloudinary.config.apiSecret);
        this.api = cloudinary.api();


    }

    public Map findByAttr(List<Map> elements, String attr, Object value) {
        for (Map element : elements) {
            if (value.equals(element.get(attr))) {
                return element;
            }
        }
        return null;
    }

    @Test
    public void test01ResourceTypes() throws Exception {
        // should allow listing resource_types
        Map result = api.resourceTypes(ObjectUtils.emptyMap());
        final List<String> resource_types = (List<String>) result.get("resource_types");
        assertThat(resource_types, hasItem("image"));
    }

    @Test
    public void test02Resources() throws Exception {
        // should allow listing resources
        Map resource = preloadResource(ObjectUtils.asMap("tags", UPLOAD_TAGS));

        final List<Map> resources = new ArrayList<Map>();
        String next_cursor = null;
        do {
            Map result = api.resources(ObjectUtils.asMap("max_results", 500, "next_cursor", next_cursor));
            resources.addAll((List) result.get("resources"));
            next_cursor = (String) result.get("next_cursor");
        } while (next_cursor != null);
        assertThat(resources, hasItem(allOf(hasEntry("public_id", (String) resource.get("public_id")), hasEntry("type", "upload"))));
    }

    @Test
    public void test03ResourcesCursor() throws Exception {
        // should allow listing resources with cursor
        Map options = new HashMap();
        options.put("max_results", 1);
        Map result = api.resources(options);
        List<Map> resources = (List<Map>) result.get("resources");
        assertNotNull(resources);
        assertEquals(1, resources.size());
        assertNotNull(result.get("next_cursor"));

        options.put("next_cursor", result.get("next_cursor"));
        Map result2 = api.resources(options);
        List<Map> resources2 = (List<Map>) result2.get("resources");
        assertNotNull(resources2);
        assertEquals(resources2.size(), 1);
        assertNotSame(resources2.get(0).get("public_id"), resources.get(0).get("public_id"));
    }

    @Test
    public void test04ResourcesByType() throws Exception {
        // should allow listing resources by type
        Map resource = preloadResource(ObjectUtils.asMap("tags", UPLOAD_TAGS));
        Map result = api.resources(ObjectUtils.asMap("type", "upload", "max_results", 500));
        List<Map> resources = (List) result.get("resources");
        assertThat(resources, hasItem(hasEntry("public_id", (String) resource.get("public_id"))));
    }

    @Test
    public void test05ResourcesByPrefix() throws Exception {
        // should allow listing resources by prefix
        Map result = api.resources(ObjectUtils.asMap("type", "upload", "prefix", API_TEST, "tags", true, "context", true));
        List<Map> resources = (List) result.get("resources");
        assertThat(resources, hasItem(hasEntry("public_id", (Object) API_TEST)));
        assertThat(resources, hasItem(hasEntry("public_id", (Object) API_TEST_1)));
//        resources = (List<Map<? extends String, ?>>) result.get("resources");
        assertThat(resources, hasItem(allOf(hasEntry("public_id", API_TEST), hasEntry("type", "upload"))));
        assertThat(resources, hasItem(hasEntry("context", ObjectUtils.asMap("custom", ObjectUtils.asMap("key", "value")))));
        assertThat(resources, hasItem(hasEntry(equalTo("tags"), hasItem(API_TAG))));
    }

    @Test
    public void testResourcesListingDirection() throws Exception {
        // should allow listing resources in both directions
        Map result = api.resourcesByTag(DIRECTION_TAG, ObjectUtils.asMap("type", "upload", "direction", "asc", "max_results", 500));
        List<Map> resources = (List<Map>) result.get("resources");
        ArrayList<String> resourceIds = new ArrayList<String>();
        for (Map resource : resources) {
            resourceIds.add((String) resource.get("public_id"));
        }
        result = api.resourcesByTag(DIRECTION_TAG, ObjectUtils.asMap("type", "upload", "direction", -1, "max_results", 500));
        List<Map> resourcesDesc = (List<Map>) result.get("resources");
        ArrayList<String> resourceIdsDesc = new ArrayList<String>();
        for (Map resource : resourcesDesc) {
            resourceIdsDesc.add((String) resource.get("public_id"));
        }
        Collections.reverse(resourceIds);
        assertEquals(resourceIds, resourceIdsDesc);
    }

    @Ignore
    public void testResourcesListingStartAt() throws Exception {
        // should allow listing resources by start date - make sure your clock
        // is set correctly!!!
        Thread.sleep(2000L);
        java.util.Date startAt = new java.util.Date();
        Thread.sleep(2000L);
        Map response = cloudinary.uploader().upload(SRC_TEST_IMAGE, ObjectUtils.asMap("tags", UPLOAD_TAGS));
        ApiResponse listResources = api.resources(ObjectUtils.asMap("type", "upload", "start_at", startAt, "direction", "asc"));
        List<Map> resources = (List<Map>) listResources.get("resources");
        assertEquals(response.get("public_id"), resources.get(0).get("public_id"));
    }

    @Test
    public void testTransformationsWithCursor() throws Exception {
        String name = "testTransformation" + SDK_TEST_TAG + System.currentTimeMillis();
        api.createTransformation(name, "c_scale,w_100", null);
        final List<Map> transformations = new ArrayList<>();
        String next_cursor = null;
        do {
            Map result = api.transformations(ObjectUtils.asMap("max_results", 500, "next_cursor", next_cursor));
            transformations.addAll((List) result.get("transformations"));
            next_cursor = (String) result.get("next_cursor");
        } while (next_cursor != null );
        assertThat(transformations, hasItem(allOf(hasEntry("name", "t_" + name))));
    }

    @Test
    public void testResourcesByPublicIds() throws Exception {
        // should allow listing resources by public ids
        Map result = api.resourcesByIds(Arrays.asList(API_TEST, API_TEST_1, "bogus"), ObjectUtils.asMap("type", "upload", "tags", true, "context", true));
        List<Map> resources = (List<Map>) result.get("resources");
        assertEquals(2, resources.size());
        assertNotNull(findByAttr(resources, "public_id", API_TEST));
        assertNotNull(findByAttr(resources, "public_id", API_TEST_1));
        assertNotNull(findByAttr((List<Map>) result.get("resources"), "context", ObjectUtils.asMap("custom", ObjectUtils.asMap("key", "value"))));
        boolean found = false;
        for (Map r : resources) {
            ArrayList tags = (ArrayList) r.get("tags");
            found = found || tags.contains(API_TAG);
        }
        assertTrue(found);
    }

    @Test
    public void test06ResourcesTag() throws Exception {
        // should allow listing resources by tag
        Map result = api.resourcesByTag(API_TAG, ObjectUtils.asMap("tags", true, "context", true, "max_results", 500));
        Map resource = findByAttr((List<Map>) result.get("resources"), "public_id", API_TEST);
        assertNotNull(resource);
        resource = findByAttr((List<Map>) result.get("resources"), "context", ObjectUtils.asMap("custom", ObjectUtils.asMap("key", "value")));
        assertNotNull(resource);
        List<Map> resources = (List<Map>) result.get("resources");
        boolean found = false;
        for (Map r : resources) {
            ArrayList tags = (ArrayList) r.get("tags");
            found = found || tags.contains(API_TAG);
        }
        assertTrue(found);
    }

    @Test
    public void test07ResourceMetadata() throws Exception {
        // should allow get resource metadata
        Map resource = api.resource(API_TEST, ObjectUtils.emptyMap());
        assertNotNull(resource);
        assertEquals(API_TEST, resource.get("public_id"));
        assertEquals(3381, resource.get("bytes"));
        assertEquals(1, ((List) resource.get("derived")).size());
    }

    @Test
    public void test08DeleteDerived() throws Exception {
        // should allow deleting derived resource
        cloudinary.uploader().upload(SRC_TEST_IMAGE,
                ObjectUtils.asMap("public_id", API_TEST_3, "tags", UPLOAD_TAGS, "eager", Collections.singletonList(new Transformation().width(101).crop("scale"))));
        Map resource = api.resource(API_TEST_3, ObjectUtils.emptyMap());
        assertNotNull(resource);
        List<Map> derived = (List<Map>) resource.get("derived");
        assertEquals(derived.size(), 1);
        String derived_resource_id = (String) derived.get(0).get("id");
        api.deleteDerivedResources(Collections.singletonList(derived_resource_id), ObjectUtils.emptyMap());
        resource = api.resource(API_TEST_3, ObjectUtils.emptyMap());
        assertNotNull(resource);
        derived = (List<Map>) resource.get("derived");
        assertEquals(derived.size(), 0);
    }

    @Test()
    public void testDeleteDerivedByTransformation() throws Exception {
        // should allow deleting resources
        String public_id = "api_test_123" + SUFFIX;
        List<Transformation> transformations = new ArrayList<Transformation>();
        transformations.add(new Transformation().angle(90));
        transformations.add(new Transformation().width(120));
        cloudinary.uploader().upload(SRC_TEST_IMAGE, ObjectUtils.asMap("public_id", public_id, "tags", UPLOAD_TAGS, "eager", transformations));
        Map resource = api.resource(public_id, ObjectUtils.emptyMap());
        assertNotNull(resource);
        List derived = ((List) resource.get("derived"));
        assertTrue(derived.size() == 2);
        api.deleteDerivedByTransformation(ObjectUtils.asArray(public_id), ObjectUtils.asArray(transformations), ObjectUtils.emptyMap());

        resource = api.resource(public_id, ObjectUtils.emptyMap());
        assertNotNull(resource);
        derived = ((List) resource.get("derived"));
        assertTrue(derived.size() == 0);
    }

    @Test(expected = NotFound.class)
    public void test09DeleteResources() throws Exception {
        // should allow deleting resources
        String public_id = "api_,test3" + SUFFIX;
        cloudinary.uploader().upload(SRC_TEST_IMAGE, ObjectUtils.asMap("public_id", public_id, "tags", UPLOAD_TAGS));
        Map resource = api.resource(public_id, ObjectUtils.emptyMap());
        assertNotNull(resource);
        api.deleteResources(Arrays.asList(public_id), ObjectUtils.emptyMap());
        api.resource(public_id, ObjectUtils.emptyMap());
    }

    @Test(expected = NotFound.class)
    public void test09aDeleteResourcesByPrefix() throws Exception {
        // should allow deleting resources
        String public_id = SUFFIX + "_api_test_by_prefix";
        cloudinary.uploader().upload(SRC_TEST_IMAGE, ObjectUtils.asMap("public_id", public_id, "tags", UPLOAD_TAGS));
        Map resource = api.resource(public_id, ObjectUtils.emptyMap());
        assertNotNull(resource);
        api.deleteResourcesByPrefix(public_id.substring(0, SUFFIX.length() + 10), ObjectUtils.emptyMap());
        api.resource(public_id, ObjectUtils.emptyMap());
    }

    @Test(expected = NotFound.class)
    public void test09aDeleteResourcesByTags() throws Exception {
        // should allow deleting resources
        String tag = "api_test_tag_for_delete" + SUFFIX;
        cloudinary.uploader().upload(SRC_TEST_IMAGE, ObjectUtils.asMap("public_id", API_TEST + "_4", "tags", Collections.singletonList(tag)));
        Map resource = api.resource(API_TEST + "_4", ObjectUtils.emptyMap());
        assertNotNull(resource);
        api.deleteResourcesByTag(tag, ObjectUtils.emptyMap());
        api.resource(API_TEST + "_4", ObjectUtils.emptyMap());
    }

    @Test
    public void test10Tags() throws Exception {
        // should allow listing tags
        Map result = api.tags(ObjectUtils.asMap("max_results", 500));
        List<String> tags = (List<String>) result.get("tags");
        assertThat(tags, hasItem(API_TAG));
    }

    @Test
    public void test11TagsPrefix() throws Exception {
        // should allow listing tag by prefix
        Map result = api.tags(ObjectUtils.asMap("prefix", API_TAG.substring(0, API_TAG.length() - 1)));
        List<String> tags = (List<String>) result.get("tags");
        assertThat(tags, hasItem(API_TAG));
        result = api.tags(ObjectUtils.asMap("prefix", "api_test_no_such_tag"));
        tags = (List<String>) result.get("tags");
        assertEquals(0, tags.size());
    }

    @Test
    public void test12Transformations() throws Exception {
        // should allow listing transformations
        final Transformation listTest = new Transformation().width(25).crop("scale").overlay(new TextLayer().text(SUFFIX + "_testListTransformations").fontFamily("Arial").fontSize(60));
        preloadResource(ObjectUtils.asMap("tags", UPLOAD_TAGS, "eager", Collections.singletonList(listTest)));
        Map result = api.transformations(ObjectUtils.asMap("max_results", 500));
        Map transformation = findByAttr((List<Map>) result.get("transformations"), "name", listTest.generate());

        assertNotNull(transformation);
        assertTrue((Boolean) transformation.get("used"));
    }

    @Test
    public void test13TransformationMetadata() throws Exception {
        // should allow getting transformation metadata
        preloadResource(ObjectUtils.asMap("tags", UPLOAD_TAGS, "eager", Collections.singletonList(EXPLICIT_TRANSFORMATION)));
        Map transformation = api.transformation(EXPLICIT_TRANSFORMATION_NAME, ObjectUtils.asMap("max_results", 500));
        assertNotNull(transformation);
        assertEquals(new Transformation((List<Map>) transformation.get("info")).generate(), EXPLICIT_TRANSFORMATION.generate());
    }

    @Test
    public void test14TransformationUpdate() throws Exception {
        // should allow updating transformation allowed_for_strict
        api.updateTransformation(UPDATE_TRANSFORMATION_NAME, ObjectUtils.asMap("allowed_for_strict", true), ObjectUtils.emptyMap());
        Map transformation = api.transformation(UPDATE_TRANSFORMATION_NAME, ObjectUtils.emptyMap());
        assertNotNull(transformation);
        assertEquals(transformation.get("allowed_for_strict"), true);
        api.updateTransformation(UPDATE_TRANSFORMATION_NAME, ObjectUtils.asMap("allowed_for_strict", false), ObjectUtils.emptyMap());
        transformation = api.transformation(UPDATE_TRANSFORMATION_NAME, ObjectUtils.emptyMap());
        assertNotNull(transformation);
        assertEquals(transformation.get("allowed_for_strict"), false);
    }

    @Test
    public void test15TransformationCreate() throws Exception {
        // should allow creating named transformation
        api.createTransformation(API_TEST_TRANSFORMATION, new Transformation().crop("scale").width(102).generate(), ObjectUtils.emptyMap());
        Map transformation = api.transformation(API_TEST_TRANSFORMATION, ObjectUtils.emptyMap());
        assertNotNull(transformation);
        assertEquals(transformation.get("allowed_for_strict"), true);
        assertEquals(new Transformation((List<Map>) transformation.get("info")).generate(), new Transformation().crop("scale").width(102).generate());
        assertEquals(transformation.get("used"), false);
    }

    @Test
    public void test15aTransformationUnsafeUpdate() throws Exception {
        // should allow unsafe update of named transformation
        api.createTransformation(API_TEST_TRANSFORMATION_3, new Transformation().crop("scale").width(102).generate(), ObjectUtils.emptyMap());
        api.updateTransformation(API_TEST_TRANSFORMATION_3, ObjectUtils.asMap("unsafe_update", new Transformation().crop("scale").width(103).generate()),
                ObjectUtils.emptyMap());
        Map transformation = api.transformation(API_TEST_TRANSFORMATION_3, ObjectUtils.emptyMap());
        assertNotNull(transformation);
        assertEquals(new Transformation((List<Map>) transformation.get("info")).generate(), new Transformation().crop("scale").width(103).generate());
        assertEquals(transformation.get("used"), false);
    }

    @Test(expected = NotFound.class)
    public void test16aTransformationDelete() throws Exception {
        // should allow deleting named transformation
        api.createTransformation(API_TEST_TRANSFORMATION_2, new Transformation().crop("scale").width(103).generate(), ObjectUtils.emptyMap());
        api.transformation(API_TEST_TRANSFORMATION_2, ObjectUtils.emptyMap());
        ApiResponse res = api.deleteTransformation(API_TEST_TRANSFORMATION_2, ObjectUtils.emptyMap());
        assertEquals("deleted", res.get("message"));
        api.transformation(API_TEST_TRANSFORMATION_2, ObjectUtils.emptyMap());
    }

    @Test(expected = NotFound.class)
    public void test17aTransformationDeleteImplicit() throws Exception {
        // should allow deleting implicit transformation
        api.transformation(DELETE_TRANSFORMATION_NAME, ObjectUtils.emptyMap());
        ApiResponse res = api.deleteTransformation(DELETE_TRANSFORMATION_NAME, ObjectUtils.emptyMap());
        assertEquals("deleted", res.get("message"));
        api.deleteTransformation(DELETE_TRANSFORMATION_NAME, ObjectUtils.emptyMap());
    }

    @Test
    public void test20ResourcesContext() throws Exception {
        Map result = api.resourcesByContext(TEST_KEY, ObjectUtils.emptyMap());

        List<Map> resources = (List<Map>) result.get("resources");
        assertEquals(2, resources.size());
        result = api.resourcesByContext(TEST_KEY, "alt", ObjectUtils.emptyMap());

        resources = (List<Map>) result.get("resources");
        assertEquals(1, resources.size());
    }

    @Test
    public void test18Usage() throws Exception {
        // should support usage API call
        Map result = api.usage(ObjectUtils.emptyMap());
        assertNotNull(result.get("last_updated"));
    }

    @Test
    public void testRateLimitWithNonEnglishLocale() throws Exception {
        Locale.setDefault(new Locale("de", "DE"));
        ApiResponse result = cloudinary.api().usage(new HashMap());
        Assert.assertNotNull(result.apiRateLimit().getReset());
    }

    @Test
    public void test19Ping() throws Exception {
        // should support ping API call
        Map result = api.ping(ObjectUtils.emptyMap());
        assertEquals(result.get("status"), "ok");
    }

    // This test must be last because it deletes (potentially) all dependent
    // transformations which some tests rely on.
    // Add @Test if you really want to test it - This test deletes derived
    // resources!
    public void testDeleteAllResources() throws Exception {
        // should allow deleting all resources
        cloudinary.uploader().upload(SRC_TEST_IMAGE,
                ObjectUtils.asMap("public_id", API_TEST_5, "tags", UPLOAD_TAGS, "eager", Collections.singletonList(new Transformation().crop("scale").width(2.0))));
        Map result = api.resource(API_TEST_5, ObjectUtils.emptyMap());
        assertEquals(1, ((org.cloudinary.json.JSONArray) result.get("derived")).length());
        api.deleteAllResources(ObjectUtils.asMap("keep_original", true));
        result = api.resource(API_TEST_5, ObjectUtils.emptyMap());
        // assertEquals(0, ((org.cloudinary.json.JSONArray)
        // result.get("derived")).size());
    }

    @Test
    public void testManualModeration() throws Exception {
        // should support setting manual moderation status
        Map uploadResult = cloudinary.uploader().upload(SRC_TEST_IMAGE, ObjectUtils.asMap("moderation", "manual", "tags", UPLOAD_TAGS));
        Map apiResult = api.update((String) uploadResult.get("public_id"), ObjectUtils.asMap("moderation_status", "approved", "tags", UPLOAD_TAGS));
        assertEquals("approved", ((Map) ((List<Map>) apiResult.get("moderation")).get(0)).get("status"));
    }

    @Test
    public void testOcrUpdate() {
        // should support requesting ocr info
        try {
            Map uploadResult = cloudinary.uploader().upload(SRC_TEST_IMAGE, ObjectUtils.asMap("tags", UPLOAD_TAGS));
            api.update((String) uploadResult.get("public_id"), ObjectUtils.asMap("ocr", "illegal"));
        } catch (Exception e) {
            assertTrue(e instanceof BadRequest);
            assertTrue(e.getMessage().matches("^Illegal value(.*)"));
        }
    }

    @Test
    public void testRawConvertUpdate() {
        // should support requesting raw conversion
        try {
            Map uploadResult = cloudinary.uploader().upload(SRC_TEST_IMAGE, ObjectUtils.asMap("tags", UPLOAD_TAGS));
            api.update((String) uploadResult.get("public_id"), ObjectUtils.asMap("raw_convert", "illegal"));
        } catch (Exception e) {
            assertTrue(e instanceof BadRequest);
            assertTrue(e.getMessage().matches("^Illegal value(.*)"));
        }
    }

    @Test
    public void testCategorizationUpdate() {
        // should support requesting categorization
        try {
            Map uploadResult = cloudinary.uploader().upload(SRC_TEST_IMAGE, ObjectUtils.asMap("tags", UPLOAD_TAGS));
            api.update((String) uploadResult.get("public_id"), ObjectUtils.asMap("categorization", "illegal"));
        } catch (Exception e) {
            assertTrue(e instanceof BadRequest);
            assertTrue(e.getMessage().matches("^Illegal value(.*)"));
        }
    }

    @Test
    public void testDetectionUpdate() {
        // should support requesting detection
        try {
            Map uploadResult = cloudinary.uploader().upload(SRC_TEST_IMAGE, ObjectUtils.asMap("tags", UPLOAD_TAGS));
            api.update((String) uploadResult.get("public_id"), ObjectUtils.asMap("detection", "illegal"));
        } catch (Exception e) {
            assertTrue(e instanceof BadRequest);
            assertTrue(e.getMessage().matches("^Illegal value(.*)"));
        }
    }

    @Test
    public void testSimilaritySearchUpdate() {
        // should support requesting similarity search
        try {
            Map uploadResult = cloudinary.uploader().upload(SRC_TEST_IMAGE, ObjectUtils.asMap("tags", UPLOAD_TAGS));
            api.update((String) uploadResult.get("public_id"), ObjectUtils.asMap("similarity_search", "illegal"));
        } catch (Exception e) {
            assertTrue(e instanceof BadRequest);
            assertTrue(e.getMessage().matches("^Illegal value(.*)"));
        }
    }

    @Test
    public void testUpdateCustomCoordinates() throws IOException, Exception {
        // should update custom coordinates
        Coordinates coordinates = new Coordinates("121,31,110,151");
        Map uploadResult = cloudinary.uploader().upload(SRC_TEST_IMAGE, ObjectUtils.asMap("tags", UPLOAD_TAGS));
        cloudinary.api().update(uploadResult.get("public_id").toString(), ObjectUtils.asMap("custom_coordinates", coordinates));
        Map result = cloudinary.api().resource(uploadResult.get("public_id").toString(), ObjectUtils.asMap("coordinates", true));
        int[] expected = new int[]{121, 31, 110, 151};
        ArrayList actual = (ArrayList) ((ArrayList) ((Map) result.get("coordinates")).get("custom")).get(0);
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], actual.get(i));
        }
    }

    @Test
    public void testUpdateAccessControl() throws Exception {
        // should update access control
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
        final Date start = simpleDateFormat.parse("2019-02-22 16:20:57 +0200");
        final Date end = simpleDateFormat.parse("2019-03-22 00:00:00 +0200");
        AccessControlRule acl = AccessControlRule.anonymous(start, end);
        Map uploadResult = cloudinary.uploader().upload(SRC_TEST_IMAGE, ObjectUtils.asMap("tags", UPLOAD_TAGS));
        ApiResponse res = cloudinary.api().update(uploadResult.get("public_id").toString(), ObjectUtils.asMap("access_control", acl));
        Map result = cloudinary.api().resource(uploadResult.get("public_id").toString(), ObjectUtils.asMap("access_control", true));

        Map accessControlResult = (Map) ((List) result.get("access_control")).get(0);

        assertEquals("anonymous", accessControlResult.get("access_type"));
        assertEquals("2019-02-22T14:20:57Z", accessControlResult.get("start"));
        assertEquals("2019-03-21T22:00:00Z", accessControlResult.get("end"));
    }

    @Test
    public void testListUploadPresets() throws Exception {
        // should allow creating and listing upload_presets
        api.createUploadPreset(ObjectUtils.asMap("name", API_TEST_UPLOAD_PRESET, "folder", "folder"));
        api.createUploadPreset(ObjectUtils.asMap("name", API_TEST_UPLOAD_PRESET_2, "folder", "folder2"));
        api.createUploadPreset(ObjectUtils.asMap("name", API_TEST_UPLOAD_PRESET_3, "folder", "folder3"));

        ArrayList<Map> presets = (ArrayList) (api.uploadPresets(ObjectUtils.emptyMap()).get("presets"));

        assertThat(presets, hasItem(hasEntry("name", API_TEST_UPLOAD_PRESET)));
        assertThat(presets, hasItem(hasEntry("name", API_TEST_UPLOAD_PRESET_2)));
        assertThat(presets, hasItem(hasEntry("name", API_TEST_UPLOAD_PRESET_3)));

        api.deleteUploadPreset(API_TEST_UPLOAD_PRESET, ObjectUtils.emptyMap());
        api.deleteUploadPreset(API_TEST_UPLOAD_PRESET_2, ObjectUtils.emptyMap());
        api.deleteUploadPreset(API_TEST_UPLOAD_PRESET_3, ObjectUtils.emptyMap());
    }

    @Test
    public void testGetUploadPreset() throws Exception {
        // should allow getting a single upload_preset
        String[] tags = {"a", "b", "c"};
        Map context = ObjectUtils.asMap("a", "b", "c", "d");
        Map result = api.createUploadPreset(ObjectUtils.asMap("unsigned", true, "folder", "folder", "transformation", EXPLICIT_TRANSFORMATION, "tags", tags, "context",
                context));
        String name = result.get("name").toString();
        Map preset = api.uploadPreset(name, ObjectUtils.emptyMap());
        assertEquals(preset.get("name"), name);
        assertEquals(Boolean.TRUE, preset.get("unsigned"));
        Map settings = (Map) preset.get("settings");
        assertEquals(settings.get("folder"), "folder");
        Map outTransformation = (Map) ((java.util.ArrayList) settings.get("transformation")).get(0);
        assertEquals(outTransformation.get("width"), 100);
        assertEquals(outTransformation.get("crop"), "scale");
        Object[] outTags = ((java.util.ArrayList) settings.get("tags")).toArray();
        assertArrayEquals(tags, outTags);
        Map outContext = (Map) settings.get("context");
        assertEquals(context, outContext);
    }

    @Test
    public void testDeleteUploadPreset() throws Exception {
        // should allow deleting upload_presets", :upload_preset => true do
        api.createUploadPreset(ObjectUtils.asMap("name", API_TEST_UPLOAD_PRESET_4, "folder", "folder"));
        api.uploadPreset(API_TEST_UPLOAD_PRESET_4, ObjectUtils.emptyMap());
        api.deleteUploadPreset(API_TEST_UPLOAD_PRESET_4, ObjectUtils.emptyMap());
        boolean error = false;
        try {
            api.uploadPreset(API_TEST_UPLOAD_PRESET_4, ObjectUtils.emptyMap());
        } catch (Exception e) {
            error = true;
        }
        assertTrue(error);
    }

    @Test
    public void testUpdateUploadPreset() throws Exception {
        // should allow updating upload_presets
        String name = api.createUploadPreset(ObjectUtils.asMap("folder", "folder")).get("name").toString();
        Map preset = api.uploadPreset(name, ObjectUtils.emptyMap());
        Map settings = (Map) preset.get("settings");
        settings.putAll(ObjectUtils.asMap("colors", true, "unsigned", true, "disallow_public_id", true));
        api.updateUploadPreset(name, settings);
        settings.remove("unsigned");
        preset = api.uploadPreset(name, ObjectUtils.emptyMap());
        assertEquals(name, preset.get("name"));
        assertEquals(Boolean.TRUE, preset.get("unsigned"));
        assertEquals(settings, preset.get("settings"));
        api.deleteUploadPreset(name, ObjectUtils.emptyMap());
    }

    @Test
    public void testListByModerationUpdate() throws Exception {
        // "should support listing by moderation kind and value
        List<Map> resources;

        Map result1 = cloudinary.uploader().upload(SRC_TEST_IMAGE, ObjectUtils.asMap("moderation", "manual", "tags", UPLOAD_TAGS));
        Map result2 = cloudinary.uploader().upload(SRC_TEST_IMAGE, ObjectUtils.asMap("moderation", "manual", "tags", UPLOAD_TAGS));
        Map result3 = cloudinary.uploader().upload(SRC_TEST_IMAGE, ObjectUtils.asMap("moderation", "manual", "tags", UPLOAD_TAGS));
        api.update((String) result1.get("public_id"), ObjectUtils.asMap("moderation_status", "approved"));
        api.update((String) result2.get("public_id"), ObjectUtils.asMap("moderation_status", "rejected"));
        Map approved = api.resourcesByModeration("manual", "approved", ObjectUtils.asMap("max_results", 1000));
        Map rejected = api.resourcesByModeration("manual", "rejected", ObjectUtils.asMap("max_results", 1000));
        Map pending = api.resourcesByModeration("manual", "pending", ObjectUtils.asMap("max_results", 1000));

        resources = (List<Map>) approved.get("resources");
        assertThat(resources, hasItem(hasEntry("public_id", result1.get("public_id"))));
        assertThat(resources, not(hasItem(hasEntry("public_id", result2.get("public_id")))));
        assertThat(resources, not(hasItem(hasEntry("public_id", result3.get("public_id")))));

        resources = (List<Map>) rejected.get("resources");
        assertThat(resources, not(hasItem(hasEntry("public_id", result1.get("public_id")))));
        assertThat(resources, hasItem(hasEntry("public_id", result2.get("public_id"))));
        assertThat(resources, not(hasItem(hasEntry("public_id", result3.get("public_id")))));

        resources = (List<Map>) pending.get("resources");
        assertThat(resources, not(hasItem(hasEntry("public_id", result1.get("public_id")))));
        assertThat(resources, not(hasItem(hasEntry("public_id", result2.get("public_id")))));
        assertThat(resources, hasItem(hasEntry("public_id", result3.get("public_id"))));
    }

    // For this test to work, "Auto-create folders" should be enabled in the
    // Upload Settings.
    // Uncomment @Test if you really want to test it.
    // @Test
    public void testFolderApi() throws Exception {
        // should allow deleting all resources
        cloudinary.uploader().upload(SRC_TEST_IMAGE, ObjectUtils.asMap("public_id", "test_folder1/item", "tags", UPLOAD_TAGS));
        cloudinary.uploader().upload(SRC_TEST_IMAGE, ObjectUtils.asMap("public_id", "test_folder2/item", "tags", UPLOAD_TAGS));
        cloudinary.uploader().upload(SRC_TEST_IMAGE, ObjectUtils.asMap("public_id", "test_folder1/test_subfolder1/item", "tags", UPLOAD_TAGS));
        cloudinary.uploader().upload(SRC_TEST_IMAGE, ObjectUtils.asMap("public_id", "test_folder1/test_subfolder2/item", "tags", UPLOAD_TAGS));
        Map result = api.rootFolders(null);
        assertEquals("test_folder1", ((Map) ((org.cloudinary.json.JSONArray) result.get("folders")).get(0)).get("name"));
        assertEquals("test_folder2", ((Map) ((org.cloudinary.json.JSONArray) result.get("folders")).get(1)).get("name"));
        result = api.subFolders("test_folder1", null);
        assertEquals("test_folder1/test_subfolder1", ((Map) ((org.cloudinary.json.JSONArray) result.get("folders")).get(0)).get("path"));
        assertEquals("test_folder1/test_subfolder2", ((Map) ((org.cloudinary.json.JSONArray) result.get("folders")).get(1)).get("path"));
        try {
            api.subFolders("test_folder", null);
        } catch (Exception e) {
            assertTrue(e instanceof NotFound);
        }
        api.deleteResourcesByPrefix("test_folder", ObjectUtils.emptyMap());
    }

    @Test
    public void testRestore() throws Exception {
        // should support restoring resources
        cloudinary.uploader().upload(SRC_TEST_IMAGE,
                ObjectUtils.asMap("public_id", API_TEST_RESTORE, "backup", true, "tags", UPLOAD_TAGS));
        Map resource = api.resource(API_TEST_RESTORE, ObjectUtils.emptyMap());
        assertEquals(resource.get("bytes"), 3381);
        api.deleteResources(Collections.singletonList(API_TEST_RESTORE), ObjectUtils.emptyMap());
        resource = api.resource(API_TEST_RESTORE, ObjectUtils.emptyMap());
        assertEquals(resource.get("bytes"), 0);
        assertTrue((Boolean) resource.get("placeholder"));
        Map response = api.restore(Collections.singletonList(API_TEST_RESTORE), ObjectUtils.emptyMap());
        Map info = (Map) response.get(API_TEST_RESTORE);
        assertNotNull(info);
        assertEquals(info.get("bytes"), 3381);
        resource = api.resource(API_TEST_RESTORE, ObjectUtils.emptyMap());
        assertEquals(resource.get("bytes"), 3381);
    }

    @Test
    public void testUploadMapping() throws Exception {
        String aptTestUploadMapping = "api_test_upload_mapping" + SUFFIX;
        try {
            api.deleteUploadMapping(aptTestUploadMapping, ObjectUtils.emptyMap());
        } catch (Exception ignored) {

        }
        api.createUploadMapping(aptTestUploadMapping, ObjectUtils.asMap("template", "http://cloudinary.com"));
        Map result = api.uploadMapping(aptTestUploadMapping, ObjectUtils.emptyMap());
        assertEquals(result.get("template"), "http://cloudinary.com");
        api.updateUploadMapping(aptTestUploadMapping, ObjectUtils.asMap("template", "http://res.cloudinary.com"));
        result = api.uploadMapping(aptTestUploadMapping, ObjectUtils.emptyMap());
        assertEquals(result.get("template"), "http://res.cloudinary.com");
        result = api.uploadMappings(ObjectUtils.emptyMap());
        ListIterator mappings = ((ArrayList) result.get("mappings")).listIterator();
        boolean found = false;
        while (mappings.hasNext()) {
            Map mapping = (Map) mappings.next();
            if (mapping.get("folder").equals(aptTestUploadMapping)
                    && mapping.get("template").equals("http://res.cloudinary.com")) {
                found = true;
                break;
            }
        }
        assertTrue(found);
        api.deleteUploadMapping(aptTestUploadMapping, ObjectUtils.emptyMap());
        result = api.uploadMappings(ObjectUtils.emptyMap());
        found = false;
        while (mappings.hasNext()) {
            Map mapping = (Map) mappings.next();
            if (mapping.get("folder").equals(aptTestUploadMapping)
                    && mapping.get("template").equals("http://res.cloudinary.com")) {
                found = true;
                break;
            }
        }
        assertTrue(!found);
    }

    @Test
    public void testPublishByIds() throws Exception {
        Map response = cloudinary.uploader().upload(SRC_TEST_IMAGE, ObjectUtils.asMap("tags", UPLOAD_TAGS, "type", "authenticated"));
        String publicId = (String) response.get("public_id");
        response = cloudinary.api().publishByIds(Arrays.asList(publicId), null);
        List published = (List) response.get("published");
        assertNotNull(published);
        assertEquals(published.size(), 1);
        Map resource = (Map) published.get(0);
        assertEquals(resource.get("public_id"), publicId);
        assertNotNull(resource.get("url"));
        cloudinary.uploader().destroy(publicId, null);
    }

    @Test
    public void testPublishWithType() throws Exception {
        Map response = cloudinary.uploader().upload(SRC_TEST_IMAGE, ObjectUtils.asMap("tags", UPLOAD_TAGS, "type", "authenticated"));
        String publicId = (String) response.get("public_id");

        // publish with wrong type - verify publish fails
        response = cloudinary.api().publishByIds(Arrays.asList(publicId), ObjectUtils.asMap("type", "private"));
        List published = (List) response.get("published");
        List failed = (List) response.get("failed");
        assertNotNull(published);
        assertNotNull(failed);
        assertEquals(published.size(), 0);
        assertEquals(failed.size(), 1);

        // publish with correct type - verify publish succeeds
        response = cloudinary.api().publishByIds(Arrays.asList(publicId), ObjectUtils.asMap("type", "authenticated"));
        published = (List) response.get("published");
        failed = (List) response.get("failed");
        assertNotNull(published);
        assertNotNull(failed);
        assertEquals(published.size(), 1);
        assertEquals(failed.size(), 0);

        Map resource = (Map) published.get(0);
        assertEquals(resource.get("public_id"), publicId);
        assertNotNull(resource.get("url"));
        cloudinary.uploader().destroy(publicId, null);
    }

    @Test
    public void testPublishByPrefix() throws Exception {
        Map response = cloudinary.uploader().upload(SRC_TEST_IMAGE, ObjectUtils.asMap("tags", UPLOAD_TAGS, "type", "authenticated"));
        String publicId = (String) response.get("public_id");
        response = cloudinary.api().publishByPrefix(publicId.substring(0, publicId.length() - 2), null);
        List published = (List) response.get("published");
        assertNotNull(published);
        assertEquals(published.size(), 1);
        Map resource = (Map) published.get(0);
        assertEquals(resource.get("public_id"), publicId);
        assertNotNull(resource.get("url"));
        cloudinary.uploader().destroy(publicId, null);
    }

    @Test
    public void testPublishByTag() throws Exception {
        Map response = cloudinary.uploader().upload(SRC_TEST_IMAGE, ObjectUtils.asMap("tags", Arrays.asList(API_TAG, API_TAG + "1"), "type", "authenticated"));
        String publicId = (String) response.get("public_id");
        response = cloudinary.api().publishByTag(API_TAG + "1", null);
        List published = (List) response.get("published");
        assertNotNull(published);
        assertEquals(published.size(), 1);
        Map resource = (Map) published.get(0);
        assertEquals(resource.get("public_id"), publicId);
        assertNotNull(resource.get("url"));
        cloudinary.uploader().destroy(publicId, null);
    }

    @Test
    public void testUpdateResourcesAccessModeByIds() throws Exception {
        Map response = cloudinary.uploader().upload(SRC_TEST_IMAGE, ObjectUtils.asMap("tags", UPLOAD_TAGS, "access_mode", "authenticated"));
        String publicId = (String) response.get("public_id");
        assertEquals(response.get("access_mode"), "authenticated");
        response = cloudinary.api().updateResourcesAccessModeByIds("public", Arrays.asList(publicId), null);
        List updated = (List) response.get("updated");
        assertNotNull(updated);
        assertEquals(updated.size(), 1);
        Map resource = (Map) updated.get(0);
        assertEquals(resource.get("public_id"), publicId);
        assertEquals(resource.get("access_mode"), "public");
        cloudinary.uploader().destroy(publicId, null);
    }

    @Test
    public void testUpdateResourcesAccessModeByPrefix() throws Exception {
        Map response = cloudinary.uploader().upload(SRC_TEST_IMAGE, ObjectUtils.asMap("tags", UPLOAD_TAGS, "access_mode", "authenticated"));
        String publicId = (String) response.get("public_id");
        assertEquals(response.get("access_mode"), "authenticated");
        response = cloudinary.api().updateResourcesAccessModeByPrefix("public", publicId.substring(0, publicId.length() - 2), null);
        List updated = (List) response.get("updated");
        assertNotNull(updated);
        assertEquals(updated.size(), 1);
        Map resource = (Map) updated.get(0);
        assertEquals(resource.get("public_id"), publicId);
        assertEquals(resource.get("access_mode"), "public");
        cloudinary.uploader().destroy(publicId, null);
    }

    @Test
    public void testUpdateResourcesAccessModeByTag() throws Exception {
        Map response = cloudinary.uploader().upload(SRC_TEST_IMAGE, ObjectUtils.asMap("tags", Arrays.asList(API_TAG, API_TAG + "2"), "access_mode", "authenticated"));
        String publicId = (String) response.get("public_id");
        assertEquals(response.get("access_mode"), "authenticated");
        response = cloudinary.api().updateResourcesAccessModeByTag("public", API_TAG + "2", null);
        List updated = (List) response.get("updated");
        assertNotNull(updated);
        assertEquals(updated.size(), 1);
        Map resource = (Map) updated.get(0);
        assertEquals(resource.get("public_id"), publicId);
        assertEquals(resource.get("access_mode"), "public");
        cloudinary.uploader().destroy(publicId, null);
    }
}