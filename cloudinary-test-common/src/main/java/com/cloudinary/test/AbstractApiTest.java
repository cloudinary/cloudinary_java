package com.cloudinary.test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeNotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import com.cloudinary.Api;
import com.cloudinary.Cloudinary;
import com.cloudinary.Coordinates;
import com.cloudinary.Transformation;
import com.cloudinary.api.ApiResponse;
import com.cloudinary.api.exceptions.BadRequest;
import com.cloudinary.api.exceptions.NotFound;
import com.cloudinary.utils.ObjectUtils;

@SuppressWarnings({ "rawtypes", "unchecked" })
abstract public class AbstractApiTest {

    public static final String SRC_TEST_IMAGE = "../cloudinary-test-common/src/main/resources/old_logo.png";
    private Cloudinary cloudinary;
	protected Api api;
	private static String uniqueTag = String.format("api_test_tag_%d", new java.util.Date().getTime());

	@BeforeClass
	public static void setUpClass() throws IOException {
		Cloudinary cloudinary = new Cloudinary();
		if (cloudinary.config.apiSecret == null) {
			System.err.println("Please setup environment for Upload test to run");
			return;
		}
		Api api = cloudinary.api();
		try {
			api.deleteResources(Arrays.asList("api_test", "api_test1", "api_test2", "api_test3", "api_test5"), ObjectUtils.emptyMap());
		} catch (Exception e) {
		}
		try {
			api.deleteTransformation("api_test_transformation", ObjectUtils.emptyMap());
		} catch (Exception e) {
		}
		try {
			api.deleteTransformation("api_test_transformation2", ObjectUtils.emptyMap());
		} catch (Exception e) {
		}
		try {
			api.deleteTransformation("api_test_transformation3", ObjectUtils.emptyMap());
		} catch (Exception e) {
		}
		try {
			api.deleteUploadPreset("api_test_upload_preset", ObjectUtils.emptyMap());
		} catch (Exception e) {
		}
		try {
			api.deleteUploadPreset("api_test_upload_preset2", ObjectUtils.emptyMap());
		} catch (Exception e) {
		}
		try {
			api.deleteUploadPreset("api_test_upload_preset3", ObjectUtils.emptyMap());
		} catch (Exception e) {
		}
		try {
			api.deleteUploadPreset("api_test_upload_preset4", ObjectUtils.emptyMap());
		} catch (Exception e) {
		}
		Map options = ObjectUtils.asMap("public_id", "api_test", "tags", new String[] { "api_test_tag", uniqueTag }, "context", "key=value", "eager",
				Collections.singletonList(new Transformation().width(100).crop("scale")));
		cloudinary.uploader().upload(SRC_TEST_IMAGE, options);
		options.put("public_id", "api_test1");
		cloudinary.uploader().upload(SRC_TEST_IMAGE, options);
	}

    @Rule public TestName currentTest = new TestName();

	@Before
	public void setUp() {
		System.out.println("Running " +this.getClass().getName()+"."+ currentTest.getMethodName());
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
		assertContains("image", (Collection) result.get("resource_types"));
	}

    @Test
    public void test02Resources() throws Exception {
        // should allow listing resources
        Map result = api.resources(ObjectUtils.emptyMap());
        Map resource = findByAttr((List<Map>) result.get("resources"), "public_id", "api_test");
        assertNotNull(resource);
        assertEquals(resource.get("type"), "upload");
    }

    @Test
    public void testTimeoutParameter() throws Exception {
        // should allow listing resources
        Map<String, Object> options = new HashMap<String, Object>();
        options.put("timeout", Integer.valueOf(5000));
        Map result = api.resources(options);
        Map resource = findByAttr((List<Map>) result.get("resources"), "public_id", "api_test");
        assertNotNull(resource);
        assertEquals(resource.get("type"), "upload");
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
		Map result = api.resources(ObjectUtils.asMap("type", "upload"));
		Map resource = findByAttr((List<Map>) result.get("resources"), "public_id", "api_test");
		assertNotNull(resource);
	}

	@Test
	public void test05ResourcesByPrefix() throws Exception {
		// should allow listing resources by prefix
		Map result = api.resources(ObjectUtils.asMap("type", "upload", "prefix", "api_test", "tags", true, "context", true));
		List<Map> resources = (List<Map>) result.get("resources");
		assertNotNull(findByAttr(resources, "public_id", "api_test"));
		assertNotNull(findByAttr(resources, "public_id", "api_test1"));
		assertNotNull(findByAttr((List<Map>) result.get("resources"), "context", ObjectUtils.asMap("custom", ObjectUtils.asMap("key", "value"))));
		boolean found = false;
		for (Map r : resources) {
			ArrayList tags = (ArrayList) r.get("tags");
			found = found || tags.contains("api_test_tag");
		}
		assertTrue(found);
	}

	@Test
	public void testResourcesListingDirection() throws Exception {
		// should allow listing resources in both directions
		Map result = api.resourcesByTag(uniqueTag, ObjectUtils.asMap("type", "upload", "direction", "asc"));
		List<Map> resources = (List<Map>) result.get("resources");
		result = api.resourcesByTag(uniqueTag, ObjectUtils.asMap("type", "upload", "direction", -1));
		List<Map> resourcesDesc = (List<Map>) result.get("resources");
		Collections.reverse(resources);
		assertEquals(resources, resourcesDesc);
	}

	@Ignore
	public void testResourcesListingStartAt() throws Exception {
		// should allow listing resources by start date - make sure your clock
		// is set correctly!!!
		Thread.sleep(2000L);
		java.util.Date startAt = new java.util.Date();
		Thread.sleep(2000L);
		Map response = cloudinary.uploader().upload(SRC_TEST_IMAGE, ObjectUtils.emptyMap());
		ApiResponse listResources = api.resources(ObjectUtils.asMap("type", "upload", "start_at", startAt, "direction", "asc"));
		List<Map> resources = (List<Map>) listResources.get("resources");
		assertEquals(response.get("public_id"), resources.get(0).get("public_id"));
	}

	@Test
	public void testResourcesByPublicIds() throws Exception {
		// should allow listing resources by public ids
		Map result = api.resourcesByIds(Arrays.asList("api_test", "api_test1", "bogus"), ObjectUtils.asMap("type", "upload", "tags", true, "context", true));
		List<Map> resources = (List<Map>) result.get("resources");
		assertEquals(2, resources.size());
		assertNotNull(findByAttr(resources, "public_id", "api_test"));
		assertNotNull(findByAttr(resources, "public_id", "api_test1"));
		assertNotNull(findByAttr((List<Map>) result.get("resources"), "context", ObjectUtils.asMap("custom", ObjectUtils.asMap("key", "value"))));
		boolean found = false;
		for (Map r : resources) {
			ArrayList tags = (ArrayList) r.get("tags");
			found = found || tags.contains("api_test_tag");
		}
		assertTrue(found);
	}

	@Test
	public void test06ResourcesTag() throws Exception {
		// should allow listing resources by tag
		Map result = api.resourcesByTag("api_test_tag", ObjectUtils.asMap("tags", true, "context", true));
		Map resource = findByAttr((List<Map>) result.get("resources"), "public_id", "api_test");
		assertNotNull(resource);
		resource = findByAttr((List<Map>) result.get("resources"), "context", ObjectUtils.asMap("custom", ObjectUtils.asMap("key", "value")));
		assertNotNull(resource);
		List<Map> resources = (List<Map>) result.get("resources");
		boolean found = false;
		for (Map r : resources) {
			ArrayList tags = (ArrayList) r.get("tags");
			found = found || tags.contains("api_test_tag");
		}
		assertTrue(found);
	}

	@Test
	public void test07ResourceMetadata() throws Exception {
		// should allow get resource metadata
		Map resource = api.resource("api_test", ObjectUtils.emptyMap());
		assertNotNull(resource);
		assertEquals(resource.get("public_id"), "api_test");
		assertEquals(resource.get("bytes"), 3381);
		assertEquals(((List) resource.get("derived")).size(), 1);
	}

	@Test
	public void test08DeleteDerived() throws Exception {
		// should allow deleting derived resource
		cloudinary.uploader().upload(SRC_TEST_IMAGE,
				ObjectUtils.asMap("public_id", "api_test3", "eager", Collections.singletonList(new Transformation().width(101).crop("scale"))));
		Map resource = api.resource("api_test3", ObjectUtils.emptyMap());
		assertNotNull(resource);
		List<Map> derived = (List<Map>) resource.get("derived");
		assertEquals(derived.size(), 1);
		String derived_resource_id = (String) derived.get(0).get("id");
		api.deleteDerivedResources(Arrays.asList(derived_resource_id), ObjectUtils.emptyMap());
		resource = api.resource("api_test3", ObjectUtils.emptyMap());
		assertNotNull(resource);
		derived = (List<Map>) resource.get("derived");
		assertEquals(derived.size(), 0);
	}

	@Test(expected = NotFound.class)
	public void test09DeleteResources() throws Exception {
		// should allow deleting resources
		cloudinary.uploader().upload(SRC_TEST_IMAGE, ObjectUtils.asMap("public_id", "api_test3"));
		Map resource = api.resource("api_test3", ObjectUtils.emptyMap());
		assertNotNull(resource);
		api.deleteResources(Arrays.asList("apit_test", "api_test2", "api_test3"), ObjectUtils.emptyMap());
		api.resource("api_test3", ObjectUtils.emptyMap());
	}

	@Test(expected = NotFound.class)
	public void test09aDeleteResourcesByPrefix() throws Exception {
		// should allow deleting resources
		cloudinary.uploader().upload(SRC_TEST_IMAGE, ObjectUtils.asMap("public_id", "api_test_by_prefix"));
		Map resource = api.resource("api_test_by_prefix", ObjectUtils.emptyMap());
		assertNotNull(resource);
		api.deleteResourcesByPrefix("api_test_by", ObjectUtils.emptyMap());
		api.resource("api_test_by_prefix", ObjectUtils.emptyMap());
	}

	@Test(expected = NotFound.class)
	public void test09aDeleteResourcesByTags() throws Exception {
		// should allow deleting resources
		cloudinary.uploader().upload(SRC_TEST_IMAGE,
				ObjectUtils.asMap("public_id", "api_test4", "tags", Arrays.asList("api_test_tag_for_delete")));
		Map resource = api.resource("api_test4", ObjectUtils.emptyMap());
		assertNotNull(resource);
		api.deleteResourcesByTag("api_test_tag_for_delete", ObjectUtils.emptyMap());
		api.resource("api_test4", ObjectUtils.emptyMap());
	}

	@Test
	public void test10Tags() throws Exception {
		// should allow listing tags
		Map result = api.tags(ObjectUtils.emptyMap());
		List<String> tags = (List<String>) result.get("tags");
		assertContains("api_test_tag", tags);
	}

	@Test
	public void test11TagsPrefix() throws Exception {
		// should allow listing tag by prefix
		Map result = api.tags(ObjectUtils.asMap("prefix", "api_test"));
		List<String> tags = (List<String>) result.get("tags");
		assertContains("api_test_tag", tags);
		result = api.tags(ObjectUtils.asMap("prefix", "api_test_no_such_tag"));
		tags = (List<String>) result.get("tags");
		assertEquals(0, tags.size());
	}

	@Test
	public void test12Transformations() throws Exception {
		// should allow listing transformations
		Map result = api.transformations(ObjectUtils.emptyMap());
		Map transformation = findByAttr((List<Map>) result.get("transformations"), "name", "c_scale,w_100");

		assertNotNull(transformation);
		assertTrue((Boolean) transformation.get("used"));
	}

	@Test
	public void test13TransformationMetadata() throws Exception {
		// should allow getting transformation metadata
		Map transformation = api.transformation("c_scale,w_100", ObjectUtils.emptyMap());
		assertNotNull(transformation);
		assertEquals(new Transformation((List<Map>) transformation.get("info")).generate(), new Transformation().crop("scale").width(100).generate());
	}

	@Test
	public void test14TransformationUpdate() throws Exception {
		// should allow updating transformation allowed_for_strict
		api.updateTransformation("c_scale,w_100", ObjectUtils.asMap("allowed_for_strict", true), ObjectUtils.emptyMap());
		Map transformation = api.transformation("c_scale,w_100", ObjectUtils.emptyMap());
		assertNotNull(transformation);
		assertEquals(transformation.get("allowed_for_strict"), true);
		api.updateTransformation("c_scale,w_100", ObjectUtils.asMap("allowed_for_strict", false), ObjectUtils.emptyMap());
		transformation = api.transformation("c_scale,w_100", ObjectUtils.emptyMap());
		assertNotNull(transformation);
		assertEquals(transformation.get("allowed_for_strict"), false);
	}

	@Test
	public void test15TransformationCreate() throws Exception {
		// should allow creating named transformation
		api.createTransformation("api_test_transformation", new Transformation().crop("scale").width(102).generate(), ObjectUtils.emptyMap());
		Map transformation = api.transformation("api_test_transformation", ObjectUtils.emptyMap());
		assertNotNull(transformation);
		assertEquals(transformation.get("allowed_for_strict"), true);
		assertEquals(new Transformation((List<Map>) transformation.get("info")).generate(), new Transformation().crop("scale").width(102).generate());
		assertEquals(transformation.get("used"), false);
	}

	@Test
	public void test15aTransformationUnsafeUpdate() throws Exception {
		// should allow unsafe update of named transformation
		api.createTransformation("api_test_transformation3", new Transformation().crop("scale").width(102).generate(), ObjectUtils.emptyMap());
		api.updateTransformation("api_test_transformation3", ObjectUtils.asMap("unsafe_update", new Transformation().crop("scale").width(103).generate()),
				ObjectUtils.emptyMap());
		Map transformation = api.transformation("api_test_transformation3", ObjectUtils.emptyMap());
		assertNotNull(transformation);
		assertEquals(new Transformation((List<Map>) transformation.get("info")).generate(), new Transformation().crop("scale").width(103).generate());
		assertEquals(transformation.get("used"), false);
	}

	@Test
	public void test16aTransformationDelete() throws Exception {
		// should allow deleting named transformation
		api.createTransformation("api_test_transformation2", new Transformation().crop("scale").width(103).generate(), ObjectUtils.emptyMap());
		api.transformation("api_test_transformation2", ObjectUtils.emptyMap());
		api.deleteTransformation("api_test_transformation2", ObjectUtils.emptyMap());
	}

	@Test(expected = NotFound.class)
	public void test16bTransformationDelete() throws Exception {
		api.transformation("api_test_transformation2", ObjectUtils.emptyMap());
	}

	@Test
	public void test17aTransformationDeleteImplicit() throws Exception {
		// should allow deleting implicit transformation
		api.transformation("c_scale,w_100", ObjectUtils.emptyMap());
		api.deleteTransformation("c_scale,w_100", ObjectUtils.emptyMap());
	}

	/**
	 * @throws Exception
	 * @expectedException \Cloudinary\Api\NotFound
	 */
	@Test(expected = NotFound.class)
	public void test17bTransformationDeleteImplicit() throws Exception {
		api.transformation("c_scale,w_100", ObjectUtils.emptyMap());
	}

	@Test
	public void test18Usage() throws Exception {
		// should support usage API call
		Map result = api.usage(ObjectUtils.emptyMap());
		assertNotNull(result.get("last_updated"));
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
				ObjectUtils.asMap("public_id", "api_test5", "eager", Collections.singletonList(new Transformation().crop("scale").width(2.0))));
		Map result = api.resource("api_test5", ObjectUtils.emptyMap());
		assertEquals(1, ((org.cloudinary.json.JSONArray) result.get("derived")).length());
		api.deleteAllResources(ObjectUtils.asMap("keep_original", true));
		result = api.resource("api_test5", ObjectUtils.emptyMap());
		// assertEquals(0, ((org.cloudinary.json.JSONArray)
		// result.get("derived")).size());
	}

	@Test
	public void testManualModeration() throws Exception {
		// should support setting manual moderation status
		Map uploadResult = cloudinary.uploader().upload(SRC_TEST_IMAGE, ObjectUtils.asMap("moderation", "manual"));
		Map apiResult = api.update((String) uploadResult.get("public_id"), ObjectUtils.asMap("moderation_status", "approved"));
		assertEquals("approved", ((Map) ((List<Map>) apiResult.get("moderation")).get(0)).get("status"));
	}

	@Test
	public void testOcrUpdate() {
		// should support requesting ocr info
		try {
			Map uploadResult = cloudinary.uploader().upload(SRC_TEST_IMAGE, ObjectUtils.emptyMap());
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
			Map uploadResult = cloudinary.uploader().upload(SRC_TEST_IMAGE, ObjectUtils.emptyMap());
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
			Map uploadResult = cloudinary.uploader().upload(SRC_TEST_IMAGE, ObjectUtils.emptyMap());
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
			Map uploadResult = cloudinary.uploader().upload(SRC_TEST_IMAGE, ObjectUtils.emptyMap());
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
			Map uploadResult = cloudinary.uploader().upload(SRC_TEST_IMAGE, ObjectUtils.emptyMap());
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
		Map uploadResult = cloudinary.uploader().upload(SRC_TEST_IMAGE, ObjectUtils.emptyMap());
		cloudinary.api().update(uploadResult.get("public_id").toString(), ObjectUtils.asMap("custom_coordinates", coordinates));
		Map result = cloudinary.api().resource(uploadResult.get("public_id").toString(), ObjectUtils.asMap("coordinates", true));
		int[] expected =  new int[] { 121, 31, 110, 151};
		ArrayList actual = (ArrayList) ((ArrayList)((Map) result.get("coordinates")).get("custom")).get(0);
		for (int i = 0; i < expected.length; i++) {
			assertEquals(expected[i], actual.get(i));
		}
	}

	@Test
	public void testApiLimits() throws Exception {
		// should support reporting the current API limits found in the response
		// header
		ApiResponse result1 = api.transformations(ObjectUtils.emptyMap());
		ApiResponse result2 = api.transformations(ObjectUtils.emptyMap());
		assertNotNull(result1.apiRateLimit());
		assertNotNull(result2.apiRateLimit());
		assertEquals(result1.apiRateLimit().getRemaining() - 1, result2.apiRateLimit().getRemaining());
		assertTrue(result2.apiRateLimit().getLimit() > result2.apiRateLimit().getRemaining());
		assertEquals(result1.apiRateLimit().getLimit(), result2.apiRateLimit().getLimit());
		assertEquals(result1.apiRateLimit().getReset(), result2.apiRateLimit().getReset());
		assertTrue(result2.apiRateLimit().getReset().after(new java.util.Date()));
	}

	@Test
	public void testListUploadPresets() throws Exception {
		// should allow creating and listing upload_presets
		api.createUploadPreset(ObjectUtils.asMap("name", "api_test_upload_preset", "folder", "folder"));
		api.createUploadPreset(ObjectUtils.asMap("name", "api_test_upload_preset2", "folder", "folder2"));
		api.createUploadPreset(ObjectUtils.asMap("name", "api_test_upload_preset3", "folder", "folder3"));

		ArrayList presets = (ArrayList) (api.uploadPresets(ObjectUtils.emptyMap()).get("presets"));
		assertEquals(((Map) presets.get(0)).get("name"), "api_test_upload_preset3");
		assertEquals(((Map) presets.get(1)).get("name"), "api_test_upload_preset2");
		assertEquals(((Map) presets.get(2)).get("name"), "api_test_upload_preset");
		api.deleteUploadPreset("api_test_upload_preset", ObjectUtils.emptyMap());
		api.deleteUploadPreset("api_test_upload_preset2", ObjectUtils.emptyMap());
		api.deleteUploadPreset("api_test_upload_preset3", ObjectUtils.emptyMap());
	}

	@Test
	public void testGetUploadPreset() throws Exception {
		// should allow getting a single upload_preset
		String[] tags = { "a", "b", "c" };
		Map context = ObjectUtils.asMap("a", "b", "c", "d");
		Transformation transformation = new Transformation();
		transformation.width(100).crop("scale");
		Map result = api.createUploadPreset(ObjectUtils.asMap("unsigned", true, "folder", "folder", "transformation", transformation, "tags", tags, "context",
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
		api.createUploadPreset(ObjectUtils.asMap("name", "api_test_upload_preset4", "folder", "folder"));
		api.uploadPreset("api_test_upload_preset4", ObjectUtils.emptyMap());
		api.deleteUploadPreset("api_test_upload_preset4", ObjectUtils.emptyMap());
		boolean error = false;
		try {
			api.uploadPreset("api_test_upload_preset4", ObjectUtils.emptyMap());
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
		Map result1 = cloudinary.uploader().upload(SRC_TEST_IMAGE, ObjectUtils.asMap("moderation", "manual"));
		Map result2 = cloudinary.uploader().upload(SRC_TEST_IMAGE, ObjectUtils.asMap("moderation", "manual"));
		Map result3 = cloudinary.uploader().upload(SRC_TEST_IMAGE, ObjectUtils.asMap("moderation", "manual"));
		api.update((String) result1.get("public_id"), ObjectUtils.asMap("moderation_status", "approved"));
		api.update((String) result2.get("public_id"), ObjectUtils.asMap("moderation_status", "rejected"));
		Map approved = api.resourcesByModeration("manual", "approved", ObjectUtils.asMap("max_results", 1000));
		Map rejected = api.resourcesByModeration("manual", "rejected", ObjectUtils.asMap("max_results", 1000));
		Map pending = api.resourcesByModeration("manual", "pending", ObjectUtils.asMap("max_results", 1000));
		assertNotNull(findByAttr((List<Map>) approved.get("resources"), "public_id", (String) result1.get("public_id")));
		assertNull(findByAttr((List<Map>) approved.get("resources"), "public_id", (String) result2.get("public_id")));
		assertNull(findByAttr((List<Map>) approved.get("resources"), "public_id", (String) result2.get("public_id")));
		assertNotNull(findByAttr((List<Map>) rejected.get("resources"), "public_id", (String) result2.get("public_id")));
		assertNull(findByAttr((List<Map>) rejected.get("resources"), "public_id", (String) result1.get("public_id")));
		assertNull(findByAttr((List<Map>) rejected.get("resources"), "public_id", (String) result3.get("public_id")));
		assertNotNull(findByAttr((List<Map>) pending.get("resources"), "public_id", (String) result3.get("public_id")));
		assertNull(findByAttr((List<Map>) pending.get("resources"), "public_id", (String) result1.get("public_id")));
		assertNull(findByAttr((List<Map>) pending.get("resources"), "public_id", (String) result2.get("public_id")));
	}

	// For this test to work, "Auto-create folders" should be enabled in the
	// Upload Settings.
	// Uncomment @Test if you really want to test it.
	// @Test
	public void testFolderApi() throws Exception {
		// should allow deleting all resources
		cloudinary.uploader().upload(SRC_TEST_IMAGE, ObjectUtils.asMap("public_id", "test_folder1/item"));
		cloudinary.uploader().upload(SRC_TEST_IMAGE, ObjectUtils.asMap("public_id", "test_folder2/item"));
		cloudinary.uploader().upload(SRC_TEST_IMAGE, ObjectUtils.asMap("public_id", "test_folder1/test_subfolder1/item"));
		cloudinary.uploader().upload(SRC_TEST_IMAGE, ObjectUtils.asMap("public_id", "test_folder1/test_subfolder2/item"));
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

	private void assertContains(Object object, Collection list) {
		assertTrue(list.contains(object));
	}
}
