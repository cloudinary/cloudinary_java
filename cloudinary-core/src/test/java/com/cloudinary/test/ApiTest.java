package com.cloudinary.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
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
import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;

@SuppressWarnings({"rawtypes", "unchecked"})
public class ApiTest {

    private Cloudinary cloudinary;
    private Api api;

    @BeforeClass
    public static void setUpClass() throws IOException {
        Cloudinary cloudinary = new Cloudinary();
        if (cloudinary.getStringConfig("api_secret") == null) {
            System.err.println("Please setup environment for Upload test to run");
            return;
        }
        Api api = cloudinary.api();
        try {
            api.deleteResources(Arrays.asList("api_test", "api_test1", "api_test2", "api_test3"), Cloudinary.emptyMap());
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
        Map options = Cloudinary.asMap(
                "public_id", "api_test", 
                "tags", "api_test_tag",
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

    private void assertContains(Object object, Collection list) {
        assertTrue(list.contains(object));
    }
    
    //this test must be last because it deletes (potentially) all dependent transformations which some tests rely on.
    @Test
    public void testDeleteAllResources() throws Exception {
        // should allow deleting all resources
        cloudinary.uploader().upload("src/test/resources/logo.png", Cloudinary.asMap("public_id", "api_test5", "eager", Collections.singletonList(new Transformation().crop("scale").width(2.0))));
        Map result = cloudinary.api().resource("api_test5", Cloudinary.emptyMap());
        assertEquals(1, ((org.json.simple.JSONArray) result.get("derived")).size());
        api.deleteAllResources(Cloudinary.asMap("keep_original", true));
        result = cloudinary.api().resource("api_test5", Cloudinary.emptyMap());
        //assertEquals(0, ((org.json.simple.JSONArray) result.get("derived")).size());
    }
}
