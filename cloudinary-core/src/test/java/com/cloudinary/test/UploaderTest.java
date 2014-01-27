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

import java.awt.Rectangle;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.cloudinary.Cloudinary;
import com.cloudinary.Coordinates;
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
        Map result = cloudinary.uploader().upload("src/test/resources/logo.png", Cloudinary.asMap("colors", true));
        assertEquals(result.get("width"), 241L);
        assertEquals(result.get("height"), 51L);
        assertNotNull(result.get("colors"));
        assertNotNull(result.get("predominant"));
        Map<String, Object> to_sign = new HashMap<String, Object>();
        to_sign.put("public_id", (String) result.get("public_id"));
        to_sign.put("version", Cloudinary.asString(result.get("version")));
        String expected_signature = cloudinary.apiSignRequest(to_sign, cloudinary.getStringConfig("api_secret"));
        assertEquals(result.get("signature"), expected_signature);
    }
    
    @Test
	public void testUploadUrl() throws IOException {
        Map result = cloudinary.uploader().upload("http://cloudinary.com/images/logo.png", Cloudinary.emptyMap());
        assertEquals(result.get("width"), 241L);
        assertEquals(result.get("height"), 51L);
        Map<String, Object> to_sign = new HashMap<String, Object>();
        to_sign.put("public_id", (String) result.get("public_id"));
        to_sign.put("version", Cloudinary.asString(result.get("version")));
        String expected_signature = cloudinary.apiSignRequest(to_sign, cloudinary.getStringConfig("api_secret"));
        assertEquals(result.get("signature"), expected_signature);
    }

    @Test
	public void testUploadDataUri() throws IOException {
        Map result = cloudinary.uploader().upload("data:image/png;base64,iVBORw0KGgoAA\nAANSUhEUgAAABAAAAAQAQMAAAAlPW0iAAAABlBMVEUAAAD///+l2Z/dAAAAM0l\nEQVR4nGP4/5/h/1+G/58ZDrAz3D/McH8yw83NDDeNGe4Ug9C9zwz3gVLMDA/A6\nP9/AFGGFyjOXZtQAAAAAElFTkSuQmCC", Cloudinary.emptyMap());
        assertEquals(result.get("width"), 16L);
        assertEquals(result.get("height"), 16L);
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
	public void testUniqueFilename() throws Exception {
        Map result = cloudinary.uploader().upload("src/test/resources/logo.png", Cloudinary.asMap("use_filename", true));
	assertTrue(((String) result.get("public_id")).matches("logo_[a-z0-9]{6}")); 
        result = cloudinary.uploader().upload("src/test/resources/logo.png", Cloudinary.asMap("use_filename", true, "unique_filename", false));
	assertEquals((String) result.get("public_id"), "logo"); 
	}
    @Test
	public void testExplicit() throws IOException {
        Map result = cloudinary.uploader().explicit("cloudinary", Cloudinary.asMap("eager", Collections.singletonList(new Transformation().crop("scale").width(2.0)), "type", "twitter_name")); 
        String url = cloudinary.url().type("twitter_name").transformation(new Transformation().crop("scale").width(2.0)).format("png").version(result.get("version")).generate("cloudinary");
        String eagerUrl = (String) ((Map) ((List)result.get("eager")).get(0)).get("url");
        String cloudName = cloudinary.getStringConfig("cloud_name");
        assertEquals(eagerUrl.substring(eagerUrl.indexOf(cloudName)), url.substring(url.indexOf(cloudName)));        
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
    	String tag = cloudinary.uploader().imageUploadTag("test-field", Cloudinary.asMap("callback", "http://localhost/cloudinary_cors.html"), Cloudinary.asMap("htmlattr", "htmlvalue"));
    	assertTrue(tag.contains("type='file'"));
    	assertTrue(tag.contains("data-cloudinary-field='test-field'"));
    	assertTrue(tag.contains("class='cloudinary-fileupload'"));
    	assertTrue(tag.contains("htmlattr='htmlvalue'"));
    	tag = cloudinary.uploader().imageUploadTag("test-field", Cloudinary.asMap("callback", "http://localhost/cloudinary_cors.html"), Cloudinary.asMap("class", "myclass"));
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
    
    @Test
    public void testAllowedFormats() throws Exception {
    	//should allow whitelisted formats if allowed_formats
    	String[] formats = {"png"}; 
    	Map result = cloudinary.uploader().upload("src/test/resources/logo.png", Cloudinary.asMap("allowed_formats", formats));
    	assertEquals(result.get("format"), "png");
    }
    
    @Test
    public void testAllowedFormatsWithIllegalFormat() throws Exception {
    	//should prevent non whitelisted formats from being uploaded if allowed_formats is specified
    	boolean errorFound = false;
    	String[] formats = {"jpg"}; 
    	try{
    		cloudinary.uploader().upload("src/test/resources/logo.png", Cloudinary.asMap("allowed_formats", formats));
    	} catch(Exception e) {
        	errorFound=true;
        }
        assertTrue(errorFound);
    }
    
    @Test
    public void testAllowedFormatsWithFormat() throws Exception {
    	//should allow non whitelisted formats if type is specified and convert to that type
    	String[] formats = {"jpg"}; 
    	Map result = cloudinary.uploader().upload("src/test/resources/logo.png", Cloudinary.asMap("allowed_formats", formats, "format", "jpg"));
    	assertEquals("jpg", result.get("format"));
    }
    
    @Test
    public void testFaceCoordinates() throws Exception {
    	//should allow sending face coordinates
    	Coordinates coordinates = new Coordinates();
    	Rectangle rect1 = new Rectangle(121,31,110,151);
    	Rectangle rect2 = new Rectangle(120,30,109,150);
    	coordinates.addRect(rect1);
    	coordinates.addRect(rect2);
    	Map result = cloudinary.uploader().upload("src/test/resources/logo.png", Cloudinary.asMap("face_coordinates", coordinates, "faces", true));
    	org.json.simple.JSONArray resultFaces = (org.json.simple.JSONArray) result.get("faces");
    	assertEquals(2, resultFaces.size());
    	
    	Object[] resultCoordinates = ((org.json.simple.JSONArray) resultFaces.get(0)).toArray();
    	
    	assertEquals((long)rect1.x, resultCoordinates[0]);
    	assertEquals((long)rect1.y, resultCoordinates[1]);
    	assertEquals((long)rect1.width, resultCoordinates[2]);
    	assertEquals((long)rect1.height, resultCoordinates[3]);
    	
    	resultCoordinates = ((org.json.simple.JSONArray) resultFaces.get(1)).toArray();
    	
    	assertEquals((long)rect2.x, resultCoordinates[0]);
    	assertEquals((long)rect2.y, resultCoordinates[1]);
    	assertEquals((long)rect2.width, resultCoordinates[2]);
    	assertEquals((long)rect2.height, resultCoordinates[3]);
    	
    	Coordinates differentCoordinates = new Coordinates();
    	Rectangle rect3 = new Rectangle(122,32,111,152);
    	differentCoordinates.addRect(rect3);
    	cloudinary.uploader().explicit((String) result.get("public_id"), Cloudinary.asMap("face_coordinates", differentCoordinates, "faces", true, "type", "upload"));
    	Map info = cloudinary.api().resource((String) result.get("public_id"), Cloudinary.asMap("faces", true));
    	
    	resultFaces = (org.json.simple.JSONArray) info.get("faces");
    	assertEquals(1, resultFaces.size());
    	resultCoordinates = ((org.json.simple.JSONArray) resultFaces.get(0)).toArray();

    	assertEquals((long)rect3.x, resultCoordinates[0]);
    	assertEquals((long)rect3.y, resultCoordinates[1]);
    	assertEquals((long)rect3.width, resultCoordinates[2]);
    	assertEquals((long)rect3.height, resultCoordinates[3]);
    	
    }
    
    @Test
    public void testContext() throws Exception {
    	//should allow sending context
    	Map context = Cloudinary.asMap("caption", "some caption", "alt", "alternative");
    	Map result = cloudinary.uploader().upload("src/test/resources/logo.png", Cloudinary.asMap("context", context));
    	Map info = cloudinary.api().resource((String) result.get("public_id"), Cloudinary.asMap("context", true));
    	assertEquals(Cloudinary.asMap("custom", context), info.get("context"));
    	Map differentContext = Cloudinary.asMap("caption", "different caption", "alt2", "alternative alternative");
    	cloudinary.uploader().explicit((String) result.get("public_id"), Cloudinary.asMap("type", "upload", "context", differentContext));
    	info = cloudinary.api().resource((String) result.get("public_id"), Cloudinary.asMap("context", true));
    	assertEquals(Cloudinary.asMap("custom", differentContext), info.get("context"));
    }
}
