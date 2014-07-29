package com.cloudinary.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assume.assumeNotNull;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.cloudinary.Api;
import com.cloudinary.Api.ApiResponse;
import com.cloudinary.Cloudinary;
import com.cloudinary.Coordinates;
import com.cloudinary.Transformation;

@SuppressWarnings({"rawtypes", "unchecked"})
public class ApiTest {

    private Cloudinary cloudinary;
    private Api api;
    private static String uniqueTag = String.format("api_test_tag_%d", new java.util.Date().getTime());

    @BeforeClass
    public static void setUpClass() throws IOException {
        Cloudinary cloudinary = new Cloudinary();
        if (cloudinary.getStringConfig("api_secret") == null) {
            System.err.println("Please setup environment for Upload test to run");
            return;
        }
        Api api = cloudinary.api();
        try {
            api.deleteResources(Arrays.asList("api_test", "api_test1", "api_test2", "api_test3", "api_test5"), Cloudinary.emptyMap());
        } catch (Exception e) {
        }
        try {
            api.deleteTransformation("api_test_transformation", Cloudinary.emptyMap());
        } catch (Exception e) {
        }
        try {
            api.deleteTransformation("api_test_transformation2", Cloudinary.emptyMap());
        } catch (Exception e) {
        }
        try {
            api.deleteTransformation("api_test_transformation3", Cloudinary.emptyMap());
        } catch (Exception e) {
        }
        try{api.deleteUploadPreset("api_test_upload_preset", Cloudinary.emptyMap());}catch (Exception e) {}
        try{api.deleteUploadPreset("api_test_upload_preset2", Cloudinary.emptyMap());}catch (Exception e) {}
        try{api.deleteUploadPreset("api_test_upload_preset3", Cloudinary.emptyMap());}catch (Exception e) {}
        try{api.deleteUploadPreset("api_test_upload_preset4", Cloudinary.emptyMap());}catch (Exception e) {}
        Map options = Cloudinary.asMap(
                "public_id", "api_test", 
                "tags", new String[]{"api_test_tag", uniqueTag},
                "context", "key=value",
                "eager", Collections.singletonList(new Transformation().width(100).crop("scale"))); 
        cloudinary.uploader().upload("src/test/resources/logo.png", options);
        options.put("public_id", "api_test1");
        cloudinary.uploader().upload("src/test/resources/logo.png", options);
    }

    @Before
    public void setUp() {
        this.cloudinary = new Cloudinary();
        assumeNotNull(cloudinary.getStringConfig("api_secret"));
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
        Map result = api.resourceTypes(Cloudinary.emptyMap());
        assertContains("image", (Collection) result.get("resource_types"));
    }

    @Test
    public void test02Resources() throws Exception {
        // should allow listing resources
        Map result = api.resources(Cloudinary.emptyMap());
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
        Map result = api.resources(Cloudinary.asMap("type", "upload"));
        Map resource = findByAttr((List<Map>) result.get("resources"), "public_id", "api_test");
        assertNotNull(resource);
    }

    @Test
    public void test05ResourcesByPrefix() throws Exception {
        // should allow listing resources by prefix
        Map result = api.resources(Cloudinary.asMap("type", "upload", "prefix", "api_test", "tags", true, "context", true));
        List<Map> resources = (List<Map>) result.get("resources");
        assertNotNull(findByAttr(resources, "public_id", "api_test"));
        assertNotNull(findByAttr(resources, "public_id", "api_test1"));
        assertNotNull(findByAttr((List<Map>) result.get("resources"), "context", Cloudinary.asMap("custom", Cloudinary.asMap("key", "value"))));
        boolean found = false;
        for (Map r: resources) {
        	org.json.simple.JSONArray tags = (org.json.simple.JSONArray) r.get("tags");
        	found = found || tags.contains("api_test_tag");
        }
        assertTrue(found);
    }
    
    @Test
    public void testResourcesListingDirection() throws Exception {
        // should allow listing resources in both directions
        Map result = api.resourcesByTag(uniqueTag, Cloudinary.asMap("type", "upload", "direction", "asc"));
        List<Map> resources = (List<Map>) result.get("resources");
        result = api.resourcesByTag(uniqueTag, Cloudinary.asMap("type", "upload", "direction", -1));
        List<Map> resourcesDesc = (List<Map>) result.get("resources");
        Collections.reverse(resources);
        assertEquals(resources, resourcesDesc);
    }
    
    @Test
    public void testResourcesListingStartAt() throws Exception {
    	// should allow listing resources by start date - make sure your clock is set correctly!!!
        Thread.sleep(2000L);
        java.util.Date startAt = new java.util.Date();
		Thread.sleep(2000L);
        Map response = cloudinary.uploader().upload("src/test/resources/logo.png", Cloudinary.emptyMap());
        List<Map> resources = (List<Map>) api.resources(Cloudinary.asMap("type", "upload", "start_at", startAt, "direction", "asc")).get("resources");
        assertEquals(response.get("public_id"), resources.get(0).get("public_id"));
    }
    
    @Test
    public void testResourcesByPublicIds() throws Exception {
        // should allow listing resources by public ids
        Map result = api.resourcesByIds(Arrays.asList("api_test", "api_test1", "bogus"), Cloudinary.asMap("type", "upload", "tags", true, "context", true));
        List<Map> resources = (List<Map>) result.get("resources");
        assertEquals(2, resources.size());
        assertNotNull(findByAttr(resources, "public_id", "api_test"));
        assertNotNull(findByAttr(resources, "public_id", "api_test1"));
        assertNotNull(findByAttr((List<Map>) result.get("resources"), "context", Cloudinary.asMap("custom", Cloudinary.asMap("key", "value"))));
        boolean found = false;
        for (Map r: resources) {
        	org.json.simple.JSONArray tags = (org.json.simple.JSONArray) r.get("tags");
        	found = found || tags.contains("api_test_tag");
        }
        assertTrue(found);
    }
    
    @Test
    public void test06ResourcesTag() throws Exception {
        // should allow listing resources by tag
        Map result = api.resourcesByTag("api_test_tag", Cloudinary.asMap("tags", true, "context", true));
        Map resource = findByAttr((List<Map>) result.get("resources"), "public_id", "api_test");
        assertNotNull(resource);
        resource = findByAttr((List<Map>) result.get("resources"), "context", Cloudinary.asMap("custom", Cloudinary.asMap("key", "value")));
        assertNotNull(resource);
        List<Map> resources = (List<Map>) result.get("resources");
        boolean found = false;
        for (Map r: resources) {
        	org.json.simple.JSONArray tags = (org.json.simple.JSONArray) r.get("tags");
        	found = found || tags.contains("api_test_tag");
        }
        assertTrue(found);
    }

    @Test
    public void test07ResourceMetadata() throws Exception {
        // should allow get resource metadata
        Map resource = api.resource("api_test", Cloudinary.emptyMap());
        assertNotNull(resource);
        assertEquals(resource.get("public_id"), "api_test");
        assertEquals(resource.get("bytes"), 3381L);
        assertEquals(((List) resource.get("derived")).size(), 1);
    }

    @Test
    public void test08DeleteDerived() throws Exception {
        // should allow deleting derived resource
        cloudinary.uploader().upload("src/test/resources/logo.png", Cloudinary.asMap(
                "public_id", "api_test3",
                "eager", Collections.singletonList(new Transformation().width(101).crop("scale"))
                ));
        Map resource = api.resource("api_test3", Cloudinary.emptyMap());
        assertNotNull(resource);
        List<Map> derived = (List<Map>) resource.get("derived");
        assertEquals(derived.size(), 1);
        String derived_resource_id = (String) derived.get(0).get("id");
        api.deleteDerivedResources(Arrays.asList(derived_resource_id), Cloudinary.emptyMap());
        resource = api.resource("api_test3", Cloudinary.emptyMap());
        assertNotNull(resource);
        derived = (List<Map>) resource.get("derived");
        assertEquals(derived.size(), 0);
    }

    @Test(expected = Api.NotFound.class)
    public void test09DeleteResources() throws Exception {
        // should allow deleting resources
        cloudinary.uploader().upload("src/test/resources/logo.png", Cloudinary.asMap("public_id", "api_test3"));
        Map resource = api.resource("api_test3", Cloudinary.emptyMap());
        assertNotNull(resource);
        api.deleteResources(Arrays.asList("apit_test", "api_test2", "api_test3"), Cloudinary.emptyMap());
        api.resource("api_test3", Cloudinary.emptyMap());
    }

    @Test(expected = Api.NotFound.class)
    public void test09aDeleteResourcesByPrefix() throws Exception {
        // should allow deleting resources
        cloudinary.uploader().upload("src/test/resources/logo.png", Cloudinary.asMap("public_id", "api_test_by_prefix"));
        Map resource = api.resource("api_test_by_prefix", Cloudinary.emptyMap());
        assertNotNull(resource);
        api.deleteResourcesByPrefix("api_test_by", Cloudinary.emptyMap());
        api.resource("api_test_by_prefix", Cloudinary.emptyMap());
    }
    
    @Test(expected = Api.NotFound.class)
    public void test09aDeleteResourcesByTags() throws Exception {
        // should allow deleting resources
        cloudinary.uploader().upload("src/test/resources/logo.png", Cloudinary.asMap("public_id", "api_test4", "tags", Arrays.asList("api_test_tag_for_delete")));
        Map resource = api.resource("api_test4", Cloudinary.emptyMap());
        assertNotNull(resource);
        api.deleteResourcesByTag("api_test_tag_for_delete", Cloudinary.emptyMap());
        api.resource("api_test4", Cloudinary.emptyMap());
    }

    @Test
    public void test10Tags() throws Exception {
        // should allow listing tags
        Map result = api.tags(Cloudinary.emptyMap());
        List<String> tags = (List<String>) result.get("tags");
        assertContains("api_test_tag", tags);
    }

    @Test
    public void test11TagsPrefix() throws Exception {
        // should allow listing tag by prefix
        Map result = api.tags(Cloudinary.asMap("prefix", "api_test"));
        List<String> tags = (List<String>) result.get("tags");
        assertContains("api_test_tag", tags);
        result = api.tags(Cloudinary.asMap("prefix", "api_test_no_such_tag"));
        tags = (List<String>) result.get("tags");
        assertEquals(0, tags.size());
    }

    @Test
    public void test12Transformations() throws Exception {
        // should allow listing transformations
        Map result = api.transformations(Cloudinary.emptyMap());
        Map transformation = findByAttr((List<Map>) result.get("transformations"), "name", "c_scale,w_100");

        assertNotNull(transformation);
        assertTrue((Boolean) transformation.get("used"));
    }

    @Test
    public void test13TransformationMetadata() throws Exception {
        // should allow getting transformation metadata
        Map transformation = api.transformation("c_scale,w_100", Cloudinary.emptyMap());
        assertNotNull(transformation);
        assertEquals(new Transformation((List<Map>) transformation.get("info")).generate(), new Transformation().crop("scale").width(100)
                .generate());
    }

    @Test
    public void test14TransformationUpdate() throws Exception {
        // should allow updating transformation allowed_for_strict
        api.updateTransformation("c_scale,w_100", Cloudinary.asMap("allowed_for_strict", true), Cloudinary.emptyMap());
        Map transformation = api.transformation("c_scale,w_100", Cloudinary.emptyMap());
        assertNotNull(transformation);
        assertEquals(transformation.get("allowed_for_strict"), true);
        api.updateTransformation("c_scale,w_100", Cloudinary.asMap("allowed_for_strict", false), Cloudinary.emptyMap());
        transformation = api.transformation("c_scale,w_100", Cloudinary.emptyMap());
        assertNotNull(transformation);
        assertEquals(transformation.get("allowed_for_strict"), false);
    }

    @Test
    public void test15TransformationCreate() throws Exception {
        // should allow creating named transformation
        api.createTransformation("api_test_transformation", new Transformation().crop("scale").width(102).generate(), Cloudinary.emptyMap());
        Map transformation = api.transformation("api_test_transformation", Cloudinary.emptyMap());
        assertNotNull(transformation);
        assertEquals(transformation.get("allowed_for_strict"), true);
        assertEquals(new Transformation((List<Map>) transformation.get("info")).generate(), new Transformation().crop("scale").width(102)
                .generate());
        assertEquals(transformation.get("used"), false);
    }

    @Test
    public void test15aTransformationUnsafeUpdate() throws Exception {
        // should allow unsafe update of named transformation
        api.createTransformation("api_test_transformation3", new Transformation().crop("scale").width(102).generate(), Cloudinary.emptyMap());
        api.updateTransformation("api_test_transformation3", Cloudinary.asMap("unsafe_update", new Transformation().crop("scale").width(103).generate()), Cloudinary.emptyMap());
        Map transformation = api.transformation("api_test_transformation3", Cloudinary.emptyMap());
        assertNotNull(transformation);
        assertEquals(new Transformation((List<Map>) transformation.get("info")).generate(), new Transformation().crop("scale").width(103)
                .generate());
        assertEquals(transformation.get("used"), false);
    }

    @Test
    public void test16aTransformationDelete() throws Exception {
        // should allow deleting named transformation
        api.createTransformation("api_test_transformation2", new Transformation().crop("scale").width(103).generate(), Cloudinary.emptyMap());
        api.transformation("api_test_transformation2", Cloudinary.emptyMap());
        api.deleteTransformation("api_test_transformation2", Cloudinary.emptyMap());
    }

    @Test(expected = Api.NotFound.class)
    public void test16bTransformationDelete() throws Exception {
        api.transformation("api_test_transformation2", Cloudinary.emptyMap());
    }

    @Test
    public void test17aTransformationDeleteImplicit() throws Exception {
        // should allow deleting implicit transformation
        api.transformation("c_scale,w_100", Cloudinary.emptyMap());
        api.deleteTransformation("c_scale,w_100", Cloudinary.emptyMap());
    }

    /**
     * @throws Exception
     * @expectedException \Cloudinary\Api\NotFound
     */
    @Test(expected = Api.NotFound.class)
    public void test17bTransformationDeleteImplicit() throws Exception {
        api.transformation("c_scale,w_100", Cloudinary.emptyMap());
    }

    @Test
    public void test18Usage() throws Exception {
        // should support usage API call
        Map result = api.usage(Cloudinary.emptyMap());
        assertNotNull(result.get("last_updated"));
    }

    @Test
    public void test19Ping() throws Exception {
        // should support ping API call
        Map result = api.ping(Cloudinary.emptyMap());
        assertEquals(result.get("status"), "ok");
    }

    // This test must be last because it deletes (potentially) all dependent transformations which some tests rely on.
    // Add @Test if you really want to test it - This test deletes derived resources!
    public void testDeleteAllResources() throws Exception {
        // should allow deleting all resources
        cloudinary.uploader().upload("src/test/resources/logo.png", Cloudinary.asMap("public_id", "api_test5", "eager", Collections.singletonList(new Transformation().crop("scale").width(2.0))));
        Map result = api.resource("api_test5", Cloudinary.emptyMap());
        assertEquals(1, ((org.json.simple.JSONArray) result.get("derived")).size());
        api.deleteAllResources(Cloudinary.asMap("keep_original", true));
        result = api.resource("api_test5", Cloudinary.emptyMap());
        //assertEquals(0, ((org.json.simple.JSONArray) result.get("derived")).size());
    }
    
    
    @Test
    public void testManualModeration() throws Exception {
    	// should support setting manual moderation status
        Map uploadResult = cloudinary.uploader().upload("src/test/resources/logo.png", Cloudinary.asMap("moderation","manual"));
        Map apiResult = api.update((String) uploadResult.get("public_id"), Cloudinary.asMap("moderation_status", "approved"));
        assertEquals("approved", ((Map) ((List<Map>) apiResult.get("moderation")).get(0)).get("status"));
    }
    
	@Test
	public void testOcrUpdate() {
		// should support requesting ocr info
		try {
			Map uploadResult = cloudinary.uploader().upload(
					"src/test/resources/logo.png", Cloudinary.emptyMap());
			api.update((String) uploadResult.get("public_id"),
					Cloudinary.asMap("ocr", "illegal"));
		} catch (Exception e) {
			assertTrue(e instanceof com.cloudinary.Api.BadRequest);
			assertTrue(e.getMessage().matches("^Illegal value(.*)"));
		}
	}
	
	@Test
	public void testRawConvertUpdate() {
		// should support requesting raw conversion
		try {
			Map uploadResult = cloudinary.uploader().upload(
					"src/test/resources/logo.png", Cloudinary.emptyMap());
			api.update((String) uploadResult.get("public_id"),
					Cloudinary.asMap("raw_convert", "illegal"));
		} catch (Exception e) {
			assertTrue(e instanceof com.cloudinary.Api.BadRequest);
			assertTrue(e.getMessage().matches("^Illegal value(.*)"));
		}
	}
	
	@Test
	public void testCategorizationUpdate() {
		// should support requesting categorization
		try {
			Map uploadResult = cloudinary.uploader().upload(
					"src/test/resources/logo.png", Cloudinary.emptyMap());
			api.update((String) uploadResult.get("public_id"),
					Cloudinary.asMap("categorization", "illegal"));
		} catch (Exception e) {
			assertTrue(e instanceof com.cloudinary.Api.BadRequest);
			assertTrue(e.getMessage().matches("^Illegal value(.*)"));
		}
	}
	
	@Test
	public void testDetectionUpdate() {
		// should support requesting detection
		try {
			Map uploadResult = cloudinary.uploader().upload(
					"src/test/resources/logo.png", Cloudinary.emptyMap());
			api.update((String) uploadResult.get("public_id"),
					Cloudinary.asMap("detection", "illegal"));
		} catch (Exception e) {
			assertTrue(e instanceof com.cloudinary.Api.BadRequest);
			assertTrue(e.getMessage().matches("^Illegal value(.*)"));
		}
	}
	
	@Test
	public void testSimilaritySearchUpdate() {
		// should support requesting similarity search
		try {
			Map uploadResult = cloudinary.uploader().upload(
					"src/test/resources/logo.png", Cloudinary.emptyMap());
			api.update((String) uploadResult.get("public_id"),
					Cloudinary.asMap("similarity_search", "illegal"));
		} catch (Exception e) {
			assertTrue(e instanceof com.cloudinary.Api.BadRequest);
			assertTrue(e.getMessage().matches("^Illegal value(.*)"));
		}
	}
	
	@Test
	public void testUpdateCustomCoordinates() throws IOException, Exception {
		//should update custom coordinates
    	Coordinates coordinates = new Coordinates("121,31,110,151");
    	Map uploadResult = cloudinary.uploader().upload("src/test/resources/logo.png", Cloudinary.emptyMap());
    	cloudinary.api().update(uploadResult.get("public_id").toString(), Cloudinary.asMap("custom_coordinates", coordinates));
    	Map result = cloudinary.api().resource(uploadResult.get("public_id").toString(), Cloudinary.asMap("coordinates", true));
    	long[] expected = new long[]{121L,31L,110L,151L};
    	Object[] actual = ((org.json.simple.JSONArray)((org.json.simple.JSONArray)((Map)result.get("coordinates")).get("custom")).get(0)).toArray();
    	for (int i = 0; i < expected.length; i++){
    		assertEquals(expected[i], actual[i]);
    	}
	}
	
	@Test
	public void testApiLimits() throws Exception {
		// should support reporting the current API limits found in the response header
		ApiResponse result1 = api.transformations(Cloudinary.emptyMap());
		ApiResponse result2 = api.transformations(Cloudinary.emptyMap());
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
		api.createUploadPreset(Cloudinary.asMap("name",
				"api_test_upload_preset", "folder", "folder"));
		api.createUploadPreset(Cloudinary.asMap("name",
				"api_test_upload_preset2", "folder", "folder2"));
		api.createUploadPreset(Cloudinary.asMap("name",
				"api_test_upload_preset3", "folder", "folder3"));
		org.json.simple.JSONArray presets = (org.json.simple.JSONArray) api
				.uploadPresets(Cloudinary.emptyMap()).get("presets");
		assertEquals(((Map) presets.get(0)).get("name"),
				"api_test_upload_preset3");
		assertEquals(((Map) presets.get(1)).get("name"),
				"api_test_upload_preset2");
		assertEquals(((Map) presets.get(2)).get("name"),
				"api_test_upload_preset");
		api.deleteUploadPreset("api_test_upload_preset", Cloudinary.emptyMap());
		api.deleteUploadPreset("api_test_upload_preset2", Cloudinary.emptyMap());
		api.deleteUploadPreset("api_test_upload_preset3", Cloudinary.emptyMap());
	}

	@Test
	public void testGetUploadPreset() throws Exception {
		// should allow getting a single upload_preset
		String[] tags = { "a", "b", "c" };
		Map context = Cloudinary.asMap("a", "b", "c", "d");
		Transformation transformation = new Transformation();
		transformation.width(100).crop("scale");
		Map result = api.createUploadPreset(Cloudinary.asMap("unsigned", true,
				"folder", "folder", "transformation", transformation, "tags",
				tags, "context", context));
		String name = result.get("name").toString();
		Map preset = api.uploadPreset(name, Cloudinary.emptyMap());
		assertEquals(preset.get("name"), name);
		assertEquals(Boolean.TRUE, preset.get("unsigned"));
		Map settings = (Map) preset.get("settings");
		assertEquals(settings.get("folder"), "folder");
		Map outTransformation = (Map) ((org.json.simple.JSONArray) settings
				.get("transformation")).get(0);
		assertEquals(outTransformation.get("width"), 100L);
		assertEquals(outTransformation.get("crop"), "scale");
		Object[] outTags = ((org.json.simple.JSONArray) settings.get("tags"))
				.toArray();
		assertArrayEquals(tags, outTags);
		Map outContext = (Map) settings.get("context");
		assertEquals(context, outContext);
	}

	@Test
	public void testDeleteUploadPreset() throws Exception {
		// should allow deleting upload_presets", :upload_preset => true do
		api.createUploadPreset(Cloudinary.asMap("name",
				"api_test_upload_preset4", "folder", "folder"));
		api.uploadPreset("api_test_upload_preset4", Cloudinary.emptyMap());
		api.deleteUploadPreset("api_test_upload_preset4", Cloudinary.emptyMap());
		boolean error = false;
		try {
			api.uploadPreset("api_test_upload_preset4", Cloudinary.emptyMap());
		} catch (Exception e) {
			error = true;
		}
		assertTrue(error);
	}

	@Test
	public void testUpdateUploadPreset() throws Exception {
		// should allow updating upload_presets
		String name = api
				.createUploadPreset(Cloudinary.asMap("folder", "folder"))
				.get("name").toString();
		Map preset = api.uploadPreset(name, Cloudinary.emptyMap());
		Map settings = (Map) preset.get("settings");
		settings.putAll(Cloudinary.asMap("colors", true, "unsigned", true,
				"disallow_public_id", true));
		api.updateUploadPreset(name, settings);
		settings.remove("unsigned");
		preset = api.uploadPreset(name, Cloudinary.emptyMap());
		assertEquals(name, preset.get("name"));
		assertEquals(Boolean.TRUE, preset.get("unsigned"));
		assertEquals(settings, preset.get("settings"));
		api.deleteUploadPreset(name, Cloudinary.emptyMap());
	}
	
	@Test
	public void testListByModerationUpdate() throws Exception {
		// "should support listing by moderation kind and value
		Map result1 = cloudinary.uploader().upload(
				"src/test/resources/logo.png",
				Cloudinary.asMap("moderation", "manual"));
		Map result2 = cloudinary.uploader().upload(
				"src/test/resources/logo.png",
				Cloudinary.asMap("moderation", "manual"));
		Map result3 = cloudinary.uploader().upload(
				"src/test/resources/logo.png",
				Cloudinary.asMap("moderation", "manual"));
		api.update((String) result1.get("public_id"),
				Cloudinary.asMap("moderation_status", "approved"));
		api.update((String) result2.get("public_id"),
				Cloudinary.asMap("moderation_status", "rejected"));
		Map approved = api.resourcesByModeration("manual", "approved",
				Cloudinary.asMap("max_results", 1000));
		Map rejected = api.resourcesByModeration("manual", "rejected",
				Cloudinary.asMap("max_results", 1000));
		Map pending = api.resourcesByModeration("manual", "pending",
				Cloudinary.asMap("max_results", 1000));
		assertNotNull(findByAttr((List<Map>) approved.get("resources"),
				"public_id", (String) result1.get("public_id")));
		assertNull(findByAttr((List<Map>) approved.get("resources"),
				"public_id", (String) result2.get("public_id")));
		assertNull(findByAttr((List<Map>) approved.get("resources"),
				"public_id", (String) result2.get("public_id")));
		assertNotNull(findByAttr((List<Map>) rejected.get("resources"),
				"public_id", (String) result2.get("public_id")));
		assertNull(findByAttr((List<Map>) rejected.get("resources"),
				"public_id", (String) result1.get("public_id")));
		assertNull(findByAttr((List<Map>) rejected.get("resources"),
				"public_id", (String) result3.get("public_id")));
		assertNotNull(findByAttr((List<Map>) pending.get("resources"),
				"public_id", (String) result3.get("public_id")));
		assertNull(findByAttr((List<Map>) pending.get("resources"),
				"public_id", (String) result1.get("public_id")));
		assertNull(findByAttr((List<Map>) pending.get("resources"),
				"public_id", (String) result2.get("public_id")));
	}
	
	private void assertContains(Object object, Collection list) {
        assertTrue(list.contains(object));
    }
}
