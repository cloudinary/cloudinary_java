package com.cloudinary.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

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
		}
		Api api = cloudinary.api();
		try {
			api.delete_resources(Arrays.asList("api_test", "api_test1", "api_test2", "api_test3"), new HashMap());
		} catch (Exception e) {
		}
		try {
			api.delete_transformation("api_test_transformation", new HashMap());
		} catch (Exception e) {
		}
		try {
			api.delete_transformation("api_test_transformation2", new HashMap());
		} catch (Exception e) {
		}
		Map options = new HashMap();
		options.put("public_id", "api_test");
		options.put("tags", "api_test_tag");
		options.put("eager", Collections.singletonList(new Transformation().width(100).crop("scale")));
		cloudinary.uploader().upload("tests/logo.png", options);
		options.put("public_id", "api_test1");
		cloudinary.uploader().upload("tests/logo.png", options);
	}

	@Before
	public void setUp() {
		this.cloudinary = new Cloudinary();
		this.api = cloudinary.api();
	}

	public Map find_by_attr(List<Map> elements, String attr, Object value) {
		for (Map element : elements) {
			if (value.equals(element.get(attr))) {
				return element;
			}
		}
		return null;
	}

	@Test
	public void test01_resource_types() throws Exception {
		// should allow listing resource_types
		Map result = api.resource_types(new HashMap());
		assertContains("image", (Collection) result.get("resource_types"));
	}

	@Test
	public void test02_resources() throws Exception {
		// should allow listing resources
		Map result = api.resources(new HashMap());
		Map resource = find_by_attr((List<Map>) result.get("resources"), "public_id", "api_test");
		assertNotNull(resource);
		assertEquals(resource.get("type"), "upload");
	}

	@Test
	public void test03_resources_cursor() throws Exception {
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
	public void test04_resources_by_type() throws Exception {
		// should allow listing resources by type
		Map options = new HashMap();
		options.put("type", "upload");
		Map result = api.resources(options);
		Map resource = find_by_attr((List<Map>) result.get("resources"), "public_id", "api_test");
		assertNotNull(resource);
	}

	@Test
	public void test05_resources_by_prefix() throws Exception {
		// should allow listing resources by prefix
		Map options = new HashMap();
		options.put("type", "upload");
		options.put("prefix", "api_test");
		Map result = api.resources(options);
		List<Map> resources = (List<Map>) result.get("resources");
		assertNotNull(find_by_attr(resources, "public_id", "api_test"));
		assertNotNull(find_by_attr(resources, "public_id", "api_test1"));
	}

	@Test
	public void test06_resources_tag() throws Exception {
		// should allow listing resources by tag
		Map result = api.resources_by_tag("api_test_tag", new HashMap());
		Map resource = find_by_attr((List<Map>) result.get("resources"), "public_id", "api_test");
		assertNotNull(resource);
	}

	@Test
	public void test07_resource_metadata() throws Exception {
		// should allow get resource metadata
		Map resource = api.resource("api_test", new HashMap());
		assertNotNull(resource);
		assertEquals(resource.get("public_id"), "api_test");
		assertEquals(resource.get("bytes"), 3381L);
		assertEquals(((List) resource.get("derived")).size(), 1);
	}

	@Test
	public void test08_delete_derived() throws Exception {
		// should allow deleting derived resource
		Map options = new HashMap();
		options.put("public_id", "api_test3");
		options.put("eager", Collections.singletonList(new Transformation().width(101).crop("scale")));
		cloudinary.uploader().upload("tests/logo.png", options);
		Map resource = api.resource("api_test3", new HashMap());
		assertNotNull(resource);
		List<Map> derived = (List<Map>) resource.get("derived");
		assertEquals(derived.size(), 1);
		String derived_resource_id = (String) derived.get(0).get("id");
		api.delete_derived_resources(Arrays.asList(derived_resource_id), new HashMap());
		resource = api.resource("api_test3", new HashMap());
		assertNotNull(resource);
		derived = (List<Map>) resource.get("derived");
		assertEquals(derived.size(), 0);
	}

	@Test(expected = Api.NotFound.class)
	public void test09_delete_resources() throws Exception {
		// should allow deleting resources
		Map options = new HashMap();
		options.put("public_id", "api_test3");
		cloudinary.uploader().upload("tests/logo.png", options);
		Map resource = api.resource("api_test3", new HashMap());
		assertNotNull(resource);
		api.delete_resources(Arrays.asList("apit_test", "api_test2", "api_test3"), new HashMap());
		api.resource("api_test3", new HashMap());
	}

	@Test
	public void test10_tags() throws Exception {
		// should allow listing tags
		Map result = api.tags(new HashMap());
		List<String> tags = (List<String>) result.get("tags");
		assertContains("api_test_tag", tags);
	}

	@Test
	public void test11_tags_prefix() throws Exception {
		// should allow listing tag by prefix
		Map options = new HashMap();
		options.put("prefix", "api_test");
		Map result = api.tags(options);
		List<String> tags = (List<String>) result.get("tags");
		assertContains("api_test_tag", tags);
		options.put("prefix", "api_test_no_such_tag");
		result = api.tags(options);
		tags = (List<String>) result.get("tags");
		assertEquals(0, tags.size());
	}

	@Test
	public void test12_transformations() throws Exception {
		// should allow listing transformations
		Map result = api.transformations(new HashMap());
		Map transformation = find_by_attr((List<Map>) result.get("transformations"), "name", "c_scale,w_100");

		assertNotNull(transformation);
		assertTrue((Boolean) transformation.get("used"));
	}

	@Test
	public void test13_transformation_metadata() throws Exception {
		// should allow getting transformation metadata
		Map transformation = api.transformation("c_scale,w_100", new HashMap());
		assertNotNull(transformation);
		assertEquals(new Transformation((List<Map>) transformation.get("info")).generate(), new Transformation().crop("scale").width(100)
				.generate());
	}

	@Test
	public void test14_transformation_update() throws Exception {
		// should allow updating transformation allowed_for_strict
		Map updates = new HashMap();
		updates.put("allowed_for_strict", true);
		api.update_transformation("c_scale,w_100", updates, new HashMap());
		Map transformation = api.transformation("c_scale,w_100", new HashMap());
		assertNotNull(transformation);
		assertEquals(transformation.get("allowed_for_strict"), true);
		updates.put("allowed_for_strict", false);
		api.update_transformation("c_scale,w_100", updates, new HashMap());
		transformation = api.transformation("c_scale,w_100", new HashMap());
		assertNotNull(transformation);
		assertEquals(transformation.get("allowed_for_strict"), false);
	}

	@Test
	public void test15_transformation_create() throws Exception {
		// should allow creating named transformation
		api.create_transformation("api_test_transformation", new Transformation().crop("scale").width(102).generate(), new HashMap());
		Map transformation = api.transformation("api_test_transformation", new HashMap());
		assertNotNull(transformation);
		assertEquals(transformation.get("allowed_for_strict"), true);
		assertEquals(new Transformation((List<Map>) transformation.get("info")).generate(), new Transformation().crop("scale").width(102)
				.generate());
		assertEquals(transformation.get("used"), false);
	}

	@Test
	public void test16a_transformation_delete() throws Exception {
		// should allow deleting named transformation
		api.create_transformation("api_test_transformation2", new Transformation().crop("scale").width(103).generate(), new HashMap());
		api.transformation("api_test_transformation2", new HashMap());
		api.delete_transformation("api_test_transformation2", new HashMap());
	}

	@Test(expected = Api.NotFound.class)
	public void test16b_transformation_delete() throws Exception {
		api.transformation("api_test_transformation2", new HashMap());
	}

	@Test
	public void test17a_transformation_delete_implicit() throws Exception {
		// should allow deleting implicit transformation
		api.transformation("c_scale,w_100", new HashMap());
		api.delete_transformation("c_scale,w_100", new HashMap());
	}

	/**
	 * @throws Exception
	 * @expectedException \Cloudinary\Api\NotFound
	 */
	@Test(expected = Api.NotFound.class)
	public void test17b_transformation_delete_implicit() throws Exception {
		api.transformation("c_scale,w_100", new HashMap());
	}

	private void assertContains(Object object, Collection list) {
		assertTrue(list.contains(object));
	}
}
