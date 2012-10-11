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
	public void setUpClass() {
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
	public void test_upload() throws IOException {
        Map result = cloudinary.uploader().upload("tests/logo.png", new HashMap());
        assertEquals(result.get("width"), 241L);
        assertEquals(result.get("height"), 51L);
        Map<String, String> to_sign = new HashMap<String, String>();
        to_sign.put("public_id", (String) result.get("public_id"));
        to_sign.put("version", Cloudinary.as_string(result.get("version")));
        String expected_signature = cloudinary.api_sign_request(to_sign, cloudinary.getStringConfig("api_secret"));
        assertEquals(result.get("signature"), expected_signature);
    }

    @Test
	public void test_explicit() throws IOException {
    	Map options = new HashMap();
    	options.put("type", "twitter_name");
    	options.put("eager", Collections.singletonList(new Transformation().crop("scale").width(2.0)));
        Map result = cloudinary.uploader().explicit("cloudinary", options); 
        String url = cloudinary.url().type("twitter_name").transformation(new Transformation().crop("scale").width(2.0)).format("png").version(result.get("version")).generate("cloudinary");
        assertEquals(((Map) ((List)result.get("eager")).get(0)).get("url"), url);        
    }

    @Test
	public void test_eager() throws IOException {
    	Map options = new HashMap();
    	options.put("eager", Collections.singletonList(new Transformation().crop("scale").width(2.0)));
        cloudinary.uploader().upload("tests/logo.png", options);
    }

    @Test
	public void test_headers() throws IOException {
    	Map options = new HashMap();
    	options.put("headers", new String[]{"Link: 1"});
        cloudinary.uploader().upload("tests/logo.png", options);
    	options = new HashMap();
    	Map<String,String> headers = new HashMap<String, String>();
    	headers.put("Link", "1");
    	options.put("headers", headers);
        cloudinary.uploader().upload("tests/logo.png", options);
    }

    @Test
	public void test_text() throws IOException {
        Map result = cloudinary.uploader().text("hello world", new HashMap());
        assertTrue(((Long) result.get("width")) > 1);
        assertTrue(((Long) result.get("height")) > 1);
    }
}
