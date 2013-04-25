package com.cloudinary.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeNotNull;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;

@SuppressWarnings({"rawtypes", "unchecked"})
public class UploaderTest {

    private Cloudinary cloudinary;
    
	@BeforeClass
	public static void setUpClass() {
    	Cloudinary cloudinary = new Cloudinary();
        if (cloudinary.getStringConfig("api_secret") == null) {
          System.err.println("Please setup environment for Upload test to run");
        }
    }

	@Before
	public void setUp() {
    	this.cloudinary = new Cloudinary();
    	assumeNotNull(cloudinary.getStringConfig("api_secret"));
    }

    @Test
	public void testUpload() throws IOException {
        Map result = cloudinary.uploader().upload("src/test/resources/logo.png", Cloudinary.emptyMap());
        assertEquals(result.get("width"), 241L);
        assertEquals(result.get("height"), 51L);
        Map<String, Object> to_sign = new HashMap<String, Object>();
        to_sign.put("public_id", (String) result.get("public_id"));
        to_sign.put("version", Cloudinary.asString(result.get("version")));
        String expected_signature = cloudinary.apiSignRequest(to_sign, cloudinary.getStringConfig("api_secret"));
        assertEquals(result.get("signature"), expected_signature);
    }

    @Test
	public void testRename() throws Exception {
        Map result = cloudinary.uploader().upload("src/test/resources/logo.png", Cloudinary.emptyMap());

        cloudinary.uploader().rename((String) result.get("public_id"), result.get("public_id")+"2", Cloudinary.emptyMap());
        assertNotNull(cloudinary.api().resource(result.get("public_id")+"2", Cloudinary.emptyMap()));

        Map result2 = cloudinary.uploader().upload("src/test/resources/favicon.ico", Cloudinary.emptyMap());
        boolean error_found=false;
        try {
        	cloudinary.uploader().rename((String) result2.get("public_id"), result.get("public_id")+"2", Cloudinary.emptyMap());
        } catch(Exception e) {
        	error_found=true;
        }
        assertTrue(error_found);
        cloudinary.uploader().rename((String) result2.get("public_id"), result.get("public_id")+"2", Cloudinary.asMap("overwrite", Boolean.TRUE));
        assertEquals(cloudinary.api().resource(result.get("public_id")+"2", Cloudinary.emptyMap()).get("format"), "ico");
    }

    @Test
	public void testExplicit() throws IOException {
        Map result = cloudinary.uploader().explicit("cloudinary", Cloudinary.asMap("eager", Collections.singletonList(new Transformation().crop("scale").width(2.0)), "type", "twitter_name")); 
        String url = cloudinary.url().type("twitter_name").transformation(new Transformation().crop("scale").width(2.0)).format("png").version(result.get("version")).generate("cloudinary");
        assertEquals(((Map) ((List)result.get("eager")).get(0)).get("url"), url);        
    }

    @Test
	public void testEager() throws IOException {
        cloudinary.uploader().upload("src/test/resources/logo.png", Cloudinary.asMap("eager", Collections.singletonList(new Transformation().crop("scale").width(2.0))));
    }

    @Test
	public void testHeaders() throws IOException {
        cloudinary.uploader().upload("src/test/resources/logo.png", Cloudinary.asMap("headers", new String[]{"Link: 1"}));
        cloudinary.uploader().upload("src/test/resources/logo.png", Cloudinary.asMap("headers", Cloudinary.asMap("Link", "1")));
    }

    @Test
	public void testText() throws IOException {
        Map result = cloudinary.uploader().text("hello world", Cloudinary.emptyMap());
        assertTrue(((Long) result.get("width")) > 1);
        assertTrue(((Long) result.get("height")) > 1);
    }
    
    @Test
    public void testImageUploadTag() {
    	String tag = cloudinary.uploader().imageUploadTag("test-field", Cloudinary.emptyMap(), Cloudinary.asMap("htmlattr", "htmlvalue"));
    	assertTrue(tag.contains("type='file'"));
    	assertTrue(tag.contains("data-cloudinary-field='test-field'"));
    	assertTrue(tag.contains("class='cloudinary-fileupload'"));
    	assertTrue(tag.contains("htmlattr='htmlvalue'"));
    	tag = cloudinary.uploader().imageUploadTag("test-field", Cloudinary.emptyMap(), Cloudinary.asMap("class", "myclass"));
    	assertTrue(tag.contains("class='cloudinary-fileupload myclass'"));
    }

    @Test
    public void testSprite() throws IOException {
        cloudinary.uploader().upload("src/test/resources/logo.png", Cloudinary.asMap("tags", "sprite_test_tag", "public_id", "sprite_test_tag_1"));
        cloudinary.uploader().upload("src/test/resources/logo.png", Cloudinary.asMap("tags", "sprite_test_tag", "public_id", "sprite_test_tag_2"));
        Map result = cloudinary.uploader().generate_sprite("sprite_test_tag", Cloudinary.emptyMap());
        assertEquals(2, ((Map) result.get("image_infos")).size()); 
        result = cloudinary.uploader().generate_sprite("sprite_test_tag", Cloudinary.asMap("transformation", "w_100"));
        assertTrue(((String) result.get("css_url")).contains("w_100"));
        result = cloudinary.uploader().generate_sprite("sprite_test_tag", Cloudinary.asMap("transformation", new Transformation().width(100), "format", "jpg"));
        assertTrue(((String) result.get("css_url")).contains("f_jpg,w_100"));
    }

    @Test
    public void testMulti() throws IOException {
        cloudinary.uploader().upload("src/test/resources/logo.png", Cloudinary.asMap("tags", "multi_test_tag", "public_id", "multi_test_tag_1"));
        cloudinary.uploader().upload("src/test/resources/logo.png", Cloudinary.asMap("tags", "multi_test_tag", "public_id", "multi_test_tag_2"));
        Map result = cloudinary.uploader().multi("multi_test_tag", Cloudinary.emptyMap());
        assertTrue(((String) result.get("url")).endsWith(".gif"));
        result = cloudinary.uploader().multi("multi_test_tag", Cloudinary.asMap("transformation", "w_100"));
        assertTrue(((String) result.get("url")).contains("w_100"));
        result = cloudinary.uploader().multi("multi_test_tag", Cloudinary.asMap("transformation", new Transformation().width(111), "format", "pdf"));
        assertTrue(((String) result.get("url")).contains("w_111"));
        assertTrue(((String) result.get("url")).endsWith(".pdf"));
    }

    @Test
    public void testTags() throws Exception {
        Map result = cloudinary.uploader().upload("src/test/resources/logo.png", Cloudinary.emptyMap());
        String public_id = (String)result.get("public_id");
        Map result2 = cloudinary.uploader().upload("src/test/resources/logo.png", Cloudinary.emptyMap());
        String public_id2 = (String)result2.get("public_id");
        cloudinary.uploader().addTag("tag1", new String[]{public_id, public_id2}, Cloudinary.emptyMap());
        cloudinary.uploader().addTag("tag2", new String[]{public_id}, Cloudinary.emptyMap());
        List<String> tags = (List<String>) cloudinary.api().resource(public_id, Cloudinary.emptyMap()).get("tags"); 
        assertEquals(tags, Cloudinary.asArray(new String[]{"tag1", "tag2"}));
        tags = (List<String>) cloudinary.api().resource(public_id2, Cloudinary.emptyMap()).get("tags"); 
        assertEquals(tags, Cloudinary.asArray(new String[]{"tag1"}));
        cloudinary.uploader().removeTag("tag1", new String[]{public_id}, Cloudinary.emptyMap());
        tags = (List<String>) cloudinary.api().resource(public_id, Cloudinary.emptyMap()).get("tags"); 
        assertEquals(tags, Cloudinary.asArray(new String[]{"tag2"}));
        cloudinary.uploader().replaceTag("tag3", new String[]{public_id}, Cloudinary.emptyMap());
        tags = (List<String>) cloudinary.api().resource(public_id, Cloudinary.emptyMap()).get("tags"); 
        assertEquals(tags, Cloudinary.asArray(new String[]{"tag3"}));
    }
}
