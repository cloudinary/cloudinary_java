package com.cloudinary.test;

import static org.junit.Assert.assertEquals;
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
        Map result = cloudinary.uploader().upload("tests/logo.png", Cloudinary.emptyMap());
        assertEquals(result.get("width"), 241L);
        assertEquals(result.get("height"), 51L);
        Map<String, String> to_sign = new HashMap<String, String>();
        to_sign.put("public_id", (String) result.get("public_id"));
        to_sign.put("version", Cloudinary.asString(result.get("version")));
        String expected_signature = cloudinary.apiSignRequest(to_sign, cloudinary.getStringConfig("api_secret"));
        assertEquals(result.get("signature"), expected_signature);
    }

    @Test
	public void testExplicit() throws IOException {
        Map result = cloudinary.uploader().explicit("cloudinary", Cloudinary.asMap("eager", Collections.singletonList(new Transformation().crop("scale").width(2.0)), "type", "twitter_name")); 
        String url = cloudinary.url().type("twitter_name").transformation(new Transformation().crop("scale").width(2.0)).format("png").version(result.get("version")).generate("cloudinary");
        assertEquals(((Map) ((List)result.get("eager")).get(0)).get("url"), url);        
    }

    @Test
	public void testEager() throws IOException {
        cloudinary.uploader().upload("tests/logo.png", Cloudinary.asMap("eager", Collections.singletonList(new Transformation().crop("scale").width(2.0))));
    }

    @Test
	public void testHeaders() throws IOException {
        cloudinary.uploader().upload("tests/logo.png", Cloudinary.asMap("headers", new String[]{"Link: 1"}));
        cloudinary.uploader().upload("tests/logo.png", Cloudinary.asMap("headers", Cloudinary.asMap("Link", "1")));
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
}
