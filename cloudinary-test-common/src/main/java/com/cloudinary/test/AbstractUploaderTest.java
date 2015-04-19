package com.cloudinary.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeNotNull;
import static org.junit.Assert.assertArrayEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import com.cloudinary.Cloudinary;
import com.cloudinary.Coordinates;
import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;
import com.cloudinary.utils.Rectangle;

@SuppressWarnings({"rawtypes", "unchecked"})
abstract public class AbstractUploaderTest {

    public static final String SRC_TEST_IMAGE = "../cloudinary-test-common/src/main/resources/old_logo.png";
    public static final String REMOTE_TEST_IMAGE = "http://cloudinary.com/images/old_logo.png";
    private Cloudinary cloudinary;

	@BeforeClass
	public static void setUpClass() {
    	Cloudinary cloudinary = new Cloudinary();
        if (cloudinary.config.apiSecret == null) {
          System.err.println("Please setup environment for Upload test to run");
        }
    }

    @Rule public TestName currentTest = new TestName();

	@Before
	public void setUp() {
		System.out.println("Running " +this.getClass().getName()+"."+ currentTest.getMethodName());
    	this.cloudinary = new Cloudinary();
    	assumeNotNull(cloudinary.config.apiSecret);
    }

    @Test
	public void testUpload() throws IOException {
        Map result = cloudinary.uploader().upload(SRC_TEST_IMAGE, ObjectUtils.asMap("colors", true));
        assertEquals(result.get("width"), 241);
        assertEquals(result.get("height"), 51);
        assertNotNull(result.get("colors"));
        assertNotNull(result.get("predominant"));
        Map<String, Object> to_sign = new HashMap<String, Object>();
        to_sign.put("public_id", (String) result.get("public_id"));
        to_sign.put("version", ObjectUtils.asString(result.get("version")));
        String expected_signature = cloudinary.apiSignRequest(to_sign, cloudinary.config.apiSecret);
        assertEquals(result.get("signature"), expected_signature);
    }

    @Test
	public void testUploadUrl() throws IOException {
        Map result = cloudinary.uploader().upload(REMOTE_TEST_IMAGE, ObjectUtils.emptyMap());
        assertEquals(result.get("width"), 241);
        assertEquals(result.get("height"), 51);
        Map<String, Object> to_sign = new HashMap<String, Object>();
        to_sign.put("public_id", (String) result.get("public_id"));
        to_sign.put("version", ObjectUtils.asString(result.get("version")));
        String expected_signature = cloudinary.apiSignRequest(to_sign, cloudinary.config.apiSecret);
        assertEquals(result.get("signature"), expected_signature);
    }

    @Test
	public void testUploadDataUri() throws IOException {
        Map result = cloudinary.uploader().upload("data:image/png;base64,iVBORw0KGgoAA\nAANSUhEUgAAABAAAAAQAQMAAAAlPW0iAAAABlBMVEUAAAD///+l2Z/dAAAAM0l\nEQVR4nGP4/5/h/1+G/58ZDrAz3D/McH8yw83NDDeNGe4Ug9C9zwz3gVLMDA/A6\nP9/AFGGFyjOXZtQAAAAAElFTkSuQmCC", ObjectUtils.emptyMap());
        assertEquals(result.get("width"), 16);
        assertEquals(result.get("height"), 16);
        Map<String, Object> to_sign = new HashMap<String, Object>();
        to_sign.put("public_id", (String) result.get("public_id"));
        to_sign.put("version", ObjectUtils.asString(result.get("version")));
        String expected_signature = cloudinary.apiSignRequest(to_sign, cloudinary.config.apiSecret);
        assertEquals(result.get("signature"), expected_signature);
    }
    
    @Test
    public void testUploadUTF8() throws IOException {
        Map result = cloudinary.uploader().upload("../cloudinary-test-common/src/main/resources/old_logo.png", ObjectUtils.asMap("public_id", "Plattenkreiss_ñg-é"));
        assertEquals(result.get("public_id"), "Plattenkreiss_ñg-é");
    }

    @Test
	public void testRename() throws Exception {
        Map result = cloudinary.uploader().upload(SRC_TEST_IMAGE, ObjectUtils.emptyMap());

        cloudinary.uploader().rename((String) result.get("public_id"), result.get("public_id")+"2", ObjectUtils.emptyMap());
        assertNotNull(cloudinary.api().resource(result.get("public_id")+"2", ObjectUtils.emptyMap()));

        Map result2 = cloudinary.uploader().upload("../cloudinary-test-common/src/main/resources/favicon.ico", ObjectUtils.emptyMap());
        boolean error_found=false;
        try {
        	cloudinary.uploader().rename((String) result2.get("public_id"), result.get("public_id")+"2", ObjectUtils.emptyMap());
        } catch(Exception e) {
        	error_found=true;
        }
        assertTrue(error_found);
        cloudinary.uploader().rename((String) result2.get("public_id"), result.get("public_id")+"2", ObjectUtils.asMap("overwrite", Boolean.TRUE));
        assertEquals(cloudinary.api().resource(result.get("public_id")+"2", ObjectUtils.emptyMap()).get("format"), "ico");
    }

    @Test
	public void testUniqueFilename() throws Exception {
        Map result = cloudinary.uploader().upload(SRC_TEST_IMAGE, ObjectUtils.asMap("use_filename", true));
	assertTrue(((String) result.get("public_id")).matches("old_logo_[a-z0-9]{6}"));
        result = cloudinary.uploader().upload(SRC_TEST_IMAGE, ObjectUtils.asMap("use_filename", true, "unique_filename", false));
	assertEquals((String) result.get("public_id"), "old_logo");
	}
    @Test
	public void testExplicit() throws IOException {
        Map result = cloudinary.uploader().explicit("cloudinary", ObjectUtils.asMap("eager", Collections.singletonList(new Transformation().crop("scale").width(2.0)), "type", "twitter_name"));
        String url = cloudinary.url().type("twitter_name").transformation(new Transformation().crop("scale").width(2.0)).format("png").version(result.get("version")).generate("cloudinary");
        String eagerUrl = (String) ((Map) ((List)result.get("eager")).get(0)).get("url");
        String cloudName = cloudinary.config.cloudName;
        assertEquals(eagerUrl.substring(eagerUrl.indexOf(cloudName)), url.substring(url.indexOf(cloudName)));
    }

    @Test
	public void testEager() throws IOException {
        cloudinary.uploader().upload(SRC_TEST_IMAGE, ObjectUtils.asMap("eager", Collections.singletonList(new Transformation().crop("scale").width(2.0))));
    }

    @Test
	public void testHeaders() throws IOException {
        cloudinary.uploader().upload(SRC_TEST_IMAGE, ObjectUtils.asMap("headers", new String[]{"Link: 1"}));
        cloudinary.uploader().upload(SRC_TEST_IMAGE, ObjectUtils.asMap("headers", ObjectUtils.asMap("Link", "1")));
    }

    @Test
	public void testText() throws IOException {
        Map result = cloudinary.uploader().text("hello world", ObjectUtils.emptyMap());
        assertTrue(((Integer) result.get("width")) > 1);
        assertTrue(((Integer) result.get("height")) > 1);
    }

    @Test
    public void testImageUploadTag() {
    	String tag = cloudinary.uploader().imageUploadTag("test-field", ObjectUtils.asMap("callback", "http://localhost/cloudinary_cors.html"), ObjectUtils.asMap("htmlattr", "htmlvalue"));
    	assertTrue(tag.contains("type='file'"));
    	assertTrue(tag.contains("data-cloudinary-field='test-field'"));
    	assertTrue(tag.contains("class='cloudinary-fileupload'"));
    	assertTrue(tag.contains("htmlattr='htmlvalue'"));
    	tag = cloudinary.uploader().imageUploadTag("test-field", ObjectUtils.asMap("callback", "http://localhost/cloudinary_cors.html"), ObjectUtils.asMap("class", "myclass"));
    	assertTrue(tag.contains("class='cloudinary-fileupload myclass'"));
    }

    @Test
    public void testSprite() throws IOException {
        cloudinary.uploader().upload(SRC_TEST_IMAGE, ObjectUtils.asMap("tags", "sprite_test_tag", "public_id", "sprite_test_tag_1"));
        cloudinary.uploader().upload(SRC_TEST_IMAGE, ObjectUtils.asMap("tags", "sprite_test_tag", "public_id", "sprite_test_tag_2"));
        Map result = cloudinary.uploader().generate_sprite("sprite_test_tag", ObjectUtils.emptyMap());
        assertEquals(2, ((Map) result.get("image_infos")).size());
        result = cloudinary.uploader().generate_sprite("sprite_test_tag", ObjectUtils.asMap("transformation", "w_100"));
        assertTrue(((String) result.get("css_url")).contains("w_100"));
        result = cloudinary.uploader().generate_sprite("sprite_test_tag", ObjectUtils.asMap("transformation", new Transformation().width(100), "format", "jpg"));
        assertTrue(((String) result.get("css_url")).contains("f_jpg,w_100"));
    }

    @Test
    public void testMulti() throws IOException {
        cloudinary.uploader().upload(SRC_TEST_IMAGE, ObjectUtils.asMap("tags", "multi_test_tag", "public_id", "multi_test_tag_1"));
        cloudinary.uploader().upload(SRC_TEST_IMAGE, ObjectUtils.asMap("tags", "multi_test_tag", "public_id", "multi_test_tag_2"));
        Map result = cloudinary.uploader().multi("multi_test_tag", ObjectUtils.emptyMap());
        assertTrue(((String) result.get("url")).endsWith(".gif"));
        result = cloudinary.uploader().multi("multi_test_tag", ObjectUtils.asMap("transformation", "w_100"));
        assertTrue(((String) result.get("url")).contains("w_100"));
        result = cloudinary.uploader().multi("multi_test_tag", ObjectUtils.asMap("transformation", new Transformation().width(111), "format", "pdf"));
        assertTrue(((String) result.get("url")).contains("w_111"));
        assertTrue(((String) result.get("url")).endsWith(".pdf"));
    }

    @Test
    public void testTags() throws Exception {
        Map result = cloudinary.uploader().upload(SRC_TEST_IMAGE, ObjectUtils.emptyMap());
        String public_id = (String)result.get("public_id");
        Map result2 = cloudinary.uploader().upload(SRC_TEST_IMAGE, ObjectUtils.emptyMap());
        String public_id2 = (String)result2.get("public_id");
        cloudinary.uploader().addTag("tag1", new String[]{public_id, public_id2}, ObjectUtils.emptyMap());
        cloudinary.uploader().addTag("tag2", new String[]{public_id}, ObjectUtils.emptyMap());
        List<String> tags = (List<String>) cloudinary.api().resource(public_id, ObjectUtils.emptyMap()).get("tags");
        assertEquals(tags, ObjectUtils.asArray(new String[]{"tag1", "tag2"}));
        tags = (List<String>) cloudinary.api().resource(public_id2, ObjectUtils.emptyMap()).get("tags");
        assertEquals(tags, ObjectUtils.asArray(new String[]{"tag1"}));
        cloudinary.uploader().removeTag("tag1", new String[]{public_id}, ObjectUtils.emptyMap());
        tags = (List<String>) cloudinary.api().resource(public_id, ObjectUtils.emptyMap()).get("tags");
        assertEquals(tags, ObjectUtils.asArray(new String[]{"tag2"}));
        cloudinary.uploader().replaceTag("tag3", new String[]{public_id}, ObjectUtils.emptyMap());
        tags = (List<String>) cloudinary.api().resource(public_id, ObjectUtils.emptyMap()).get("tags");
        assertEquals(tags, ObjectUtils.asArray(new String[]{"tag3"}));
    }

    @Test
    public void testAllowedFormats() throws Exception {
    	//should allow whitelisted formats if allowed_formats
    	String[] formats = {"png"};
    	Map result = cloudinary.uploader().upload(SRC_TEST_IMAGE, ObjectUtils.asMap("allowed_formats", formats));
    	assertEquals(result.get("format"), "png");
    }

    @Test
    public void testAllowedFormatsWithIllegalFormat() throws Exception {
    	//should prevent non whitelisted formats from being uploaded if allowed_formats is specified
    	boolean errorFound = false;
    	String[] formats = {"jpg"};
    	try{
    		cloudinary.uploader().upload(SRC_TEST_IMAGE, ObjectUtils.asMap("allowed_formats", formats));
    	} catch(Exception e) {
        	errorFound=true;
        }
        assertTrue(errorFound);
    }

    @Test
    public void testAllowedFormatsWithFormat() throws Exception {
    	//should allow non whitelisted formats if type is specified and convert to that type
    	String[] formats = {"jpg"};
    	Map result = cloudinary.uploader().upload(SRC_TEST_IMAGE, ObjectUtils.asMap("allowed_formats", formats, "format", "jpg"));
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
    	Map result = cloudinary.uploader().upload(SRC_TEST_IMAGE, ObjectUtils.asMap("face_coordinates", coordinates, "faces", true));
    	ArrayList resultFaces = ((ArrayList) result.get("faces"));
    	assertEquals(2, resultFaces.size());

    	Object[] resultCoordinates = ((ArrayList) resultFaces.get(0)).toArray();

    	assertEquals(rect1.x, resultCoordinates[0]);
    	assertEquals(rect1.y, resultCoordinates[1]);
    	assertEquals(rect1.width, resultCoordinates[2]);
    	assertEquals(rect1.height, resultCoordinates[3]);

    	resultCoordinates =((ArrayList)resultFaces.get(1)).toArray();

    	assertEquals(rect2.x, resultCoordinates[0]);
    	assertEquals(rect2.y, resultCoordinates[1]);
    	assertEquals(rect2.width, resultCoordinates[2]);
    	assertEquals(rect2.height, resultCoordinates[3]);

    	Coordinates differentCoordinates = new Coordinates();
    	Rectangle rect3 = new Rectangle(122,32,111,152);
    	differentCoordinates.addRect(rect3);
    	cloudinary.uploader().explicit((String) result.get("public_id"), ObjectUtils.asMap("face_coordinates", differentCoordinates, "faces", true, "type", "upload"));
    	Map info = cloudinary.api().resource((String) result.get("public_id"), ObjectUtils.asMap("faces", true));

    	resultFaces = (ArrayList) info.get("faces");
    	assertEquals(1, resultFaces.size());
    	resultCoordinates = ((ArrayList) resultFaces.get(0)).toArray();

    	assertEquals(rect3.x, resultCoordinates[0]);
    	assertEquals(rect3.y, resultCoordinates[1]);
    	assertEquals(rect3.width, resultCoordinates[2]);
    	assertEquals(rect3.height, resultCoordinates[3]);

    }

    @Test
    public void testCustomCoordinates() throws Exception {
    	//should allow sending face coordinates
    	Coordinates coordinates = new Coordinates("121,31,110,151");
    	Map uploadResult = cloudinary.uploader().upload(SRC_TEST_IMAGE, ObjectUtils.asMap("custom_coordinates", coordinates));
    	Map result = cloudinary.api().resource(uploadResult.get("public_id").toString(), ObjectUtils.asMap("coordinates", true));
    	int[] expected = new int[]{121,31,110,151};
    	Object[] actual = ((ArrayList)((ArrayList)((Map)result.get("coordinates")).get("custom")).get(0)).toArray();
    	for (int i = 0; i < expected.length; i++){
    		assertEquals(expected[i], actual[i]);
    	}

    	coordinates = new Coordinates(new int[]{122,32,110,152});
    	cloudinary.uploader().explicit((String) uploadResult.get("public_id"), ObjectUtils.asMap("custom_coordinates", coordinates, "coordinates", true, "type", "upload"));
    	result = cloudinary.api().resource(uploadResult.get("public_id").toString(), ObjectUtils.asMap("coordinates", true));
    	expected = new int[]{122,32,110,152};
    	actual = ((ArrayList)((ArrayList)((Map)result.get("coordinates")).get("custom")).get(0)).toArray();
    	for (int i = 0; i < expected.length; i++){
    		assertEquals(expected[i], actual[i]);
    	}
    }

    @Test
    public void testContext() throws Exception {
    	//should allow sending context
    	Map context = ObjectUtils.asMap("caption", "some caption", "alt", "alternative");
    	Map result = cloudinary.uploader().upload(SRC_TEST_IMAGE, ObjectUtils.asMap("context", context));
    	Map info = cloudinary.api().resource((String) result.get("public_id"), ObjectUtils.asMap("context", true));
    	assertEquals(ObjectUtils.asMap("custom", context), info.get("context"));
    	Map differentContext = ObjectUtils.asMap("caption", "different caption", "alt2", "alternative alternative");
    	cloudinary.uploader().explicit((String) result.get("public_id"), ObjectUtils.asMap("type", "upload", "context", differentContext));
    	info = cloudinary.api().resource((String) result.get("public_id"), ObjectUtils.asMap("context", true));
    	assertEquals(ObjectUtils.asMap("custom", differentContext), info.get("context"));
    }

    @Test
    public void testModerationRequest() throws Exception {
    	//should support requesting manual moderation
    	Map result = cloudinary.uploader().upload(SRC_TEST_IMAGE,  ObjectUtils.asMap("moderation", "manual"));
    	assertEquals("manual", ((Map) ((List<Map>) result.get("moderation")).get(0)).get("kind"));
    	assertEquals("pending", ((Map) ((List<Map>) result.get("moderation")).get(0)).get("status"));
    }


    @Test
    public void testRawConvertRequest() {
    	//should support requesting raw conversion
    	try {
    		cloudinary.uploader().upload(SRC_TEST_IMAGE,  ObjectUtils.asMap("raw_convert", "illegal"));
    	} catch(Exception e) {
    		assertTrue(e.getMessage().matches("(.*)(Illegal value|not a valid)(.*)"));
        }
    }

    @Test
    public void testCategorizationRequest() {
    	//should support requesting categorization
    	try {
    		cloudinary.uploader().upload(SRC_TEST_IMAGE,  ObjectUtils.asMap("categorization", "illegal"));
    	} catch(Exception e) {
    		assertTrue(e.getMessage().matches("(.*)(Illegal value|not a valid)(.*)"));
        }
    }

    @Test
    public void testDetectionRequest() {
    	//should support requesting detection
    	try {
    		cloudinary.uploader().upload(SRC_TEST_IMAGE,  ObjectUtils.asMap("detection", "illegal"));
    	} catch(Exception e) {
    		assertTrue(e.getMessage().matches("(.*)(Illegal value|not a valid)(.*)"));
        }
    }

    @Test
    public void testAutoTaggingRequest() {
    	//should support requesting auto tagging
    	try {
    		cloudinary.uploader().upload(SRC_TEST_IMAGE,  ObjectUtils.asMap("auto_tagging", 0.5f));
    	} catch(Exception e) {
    		assertTrue(e.getMessage().matches("^Must use(.*)"));
        }
    }

    @Test
    public void testUploadLarge()  throws Exception {
    	// support uploading large files
        
        File temp = File.createTempFile("cldupload.test.", "");
        FileOutputStream out = new FileOutputStream(temp);
        int[] header = new int[]{0x42,0x4D,0x4A,0xB9,0x59,0x00,0x00,0x00,0x00,0x00,0x8A,0x00,0x00,0x00,0x7C,0x00,0x00,0x00,0x78,0x05,0x00,0x00,0x78,0x05,0x00,0x00,0x01,0x00,0x18,0x00,0x00,0x00,0x00,0x00,0xC0,0xB8,0x59,0x00,0x61,0x0F,0x00,0x00,0x61,0x0F,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0xFF,0x00,0x00,0xFF,0x00,0x00,0xFF,0x00,0x00,0x00,0x00,0x00,0x00,0xFF,0x42,0x47,0x52,0x73,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x54,0xB8,0x1E,0xFC,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x66,0x66,0x66,0xFC,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0xC4,0xF5,0x28,0xFF,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x04,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00};
        byte[] byteHeader = new byte[138];
        for (int i = 0; i <= 137; i++) byteHeader[i] = (byte) header[i];
        byte[] piece = new byte[10];
        Arrays.fill(piece, (byte) 0xff);
        out.write(byteHeader);
        for (int i = 1; i <= 588000; i++) {
          out.write(piece);
        }
        out.close();
        assertEquals(5880138, temp.length());
        ArrayList<String> tags = new java.util.ArrayList<String>();
        tags.add("upload_large_tag");
        
        Map resource = cloudinary.uploader().uploadLarge(temp, ObjectUtils.asMap("resource_type", "raw", "chunk_size", 5243000, "tags", tags));
        assertEquals(tags, resource.get("tags"));
        assertEquals("raw", resource.get("resource_type"));
        
        resource = cloudinary.uploader().uploadLarge(new FileInputStream(temp), ObjectUtils.asMap("chunk_size", 5243000, "tags", tags));
        assertEquals(tags, resource.get("tags"));
        assertEquals("image", resource.get("resource_type"));
        assertEquals(1400, resource.get("width"));
        assertEquals(1400, resource.get("height"));
        
        resource = cloudinary.uploader().uploadLarge(temp, ObjectUtils.asMap("chunk_size", 5880138, "tags", tags));
        assertEquals(tags, resource.get("tags"));
        assertEquals("image", resource.get("resource_type"));
        assertEquals(1400, resource.get("width"));
        assertEquals(1400, resource.get("height"));
        
        resource = cloudinary.uploader().uploadLarge(new FileInputStream(temp), ObjectUtils.asMap("chunk_size", 5880138, "tags", tags));
        assertEquals(tags, resource.get("tags"));
        assertEquals("image", resource.get("resource_type"));
        assertEquals(1400, resource.get("width"));
        assertEquals(1400, resource.get("height"));
    }

    @Test
    public void testUnsignedUpload()  throws Exception {
    	// should support unsigned uploading using presets
        Map preset = cloudinary.api().createUploadPreset(ObjectUtils.asMap("folder", "upload_folder", "unsigned", true));
        Map result = cloudinary.uploader().unsignedUpload(SRC_TEST_IMAGE, preset.get("name").toString(), ObjectUtils.emptyMap());
        assertTrue(result.get("public_id").toString().matches("^upload_folder\\/[a-z0-9]+$"));
        cloudinary.api().deleteUploadPreset(preset.get("name").toString(), ObjectUtils.emptyMap());
    }

}
