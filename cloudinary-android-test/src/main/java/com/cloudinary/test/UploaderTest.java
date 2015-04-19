package com.cloudinary.test;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.cloudinary.json.JSONArray;
import org.cloudinary.json.JSONObject;

import android.test.InstrumentationTestCase;
import android.util.Log;

import com.cloudinary.Cloudinary;
import com.cloudinary.Coordinates;
import com.cloudinary.Transformation;
import com.cloudinary.android.Utils;
import com.cloudinary.utils.ObjectUtils;
import com.cloudinary.utils.Rectangle;

public class UploaderTest extends InstrumentationTestCase {


    public static final String TEST_IMAGE = "images/old_logo.png";
    private Cloudinary cloudinary;
	private static boolean first = true;

	public void setUp() throws Exception {
		String url = Utils.cloudinaryUrlFromContext(getInstrumentation().getContext());
		this.cloudinary = new Cloudinary(url);
		if (first) {
			first = false;
			if (cloudinary.config.apiSecret  == null) {
				Log.e("UploaderTest", "Please CLOUDINARY_URL in AndroidManifest for Upload test to run");
			}
		}
	}

	protected InputStream getAssetStream(String filename) throws IOException {
		return getInstrumentation().getContext().getAssets().open(filename);
	}

	public void testUpload() throws Exception {
		if (cloudinary.config.apiSecret == null)
			return;
		JSONObject result = new JSONObject(cloudinary.uploader().upload(getAssetStream(TEST_IMAGE), ObjectUtils.asMap("colors", true)));
		assertEquals(result.getLong("width"), 241L);
		assertEquals(result.getLong("height"), 51L);
		assertNotNull(result.get("colors"));
		assertNotNull(result.get("predominant"));
		Map<String, Object> to_sign = new HashMap<String, Object>();
		to_sign.put("public_id", result.getString("public_id"));
		to_sign.put("version", ObjectUtils.asString(result.get("version")));
		String expected_signature = cloudinary.apiSignRequest(to_sign, cloudinary.config.apiSecret);
		assertEquals(result.get("signature"), expected_signature);
	}

	public void testUnsignedUpload() throws Exception {
		if (cloudinary.config.apiSecret == null)
			return;
		JSONObject result = new JSONObject(cloudinary.uploader().unsignedUpload(getAssetStream(TEST_IMAGE), "sample_preset_dhfjhriu",
				ObjectUtils.emptyMap()));
		assertEquals(result.getLong("width"), 241L);
		assertEquals(result.getLong("height"), 51L);
		Map<String, Object> to_sign = new HashMap<String, Object>();
		to_sign.put("public_id", result.getString("public_id"));
		to_sign.put("version", ObjectUtils.asString(result.get("version")));
		Log.d("TestRunner",cloudinary.config.apiSecret);
		String expected_signature = cloudinary.apiSignRequest(to_sign, cloudinary.config.apiSecret);
		assertEquals(result.get("signature"), expected_signature);
	}

	public void testUploadUrl() throws Exception {
		if (cloudinary.config.apiSecret == null)
			return;
		JSONObject result = new JSONObject(cloudinary.uploader().upload("http://cloudinary.com/images/old_logo.png", ObjectUtils.emptyMap()));
		assertEquals(result.getLong("width"), 241L);
		assertEquals(result.getLong("height"), 51L);
		Map<String, Object> to_sign = new HashMap<String, Object>();
		to_sign.put("public_id", (String) result.get("public_id"));
		to_sign.put("version", ObjectUtils.asString(result.get("version")));
		String expected_signature = cloudinary.apiSignRequest(to_sign, cloudinary.config.apiSecret);
		assertEquals(result.get("signature"), expected_signature);
	}

	public void testUploadDataUri() throws Exception {
		if (cloudinary.config.apiSecret == null)
			return;
		JSONObject result = new JSONObject(
				cloudinary
						.uploader()
						.upload("data:image/png;base64,iVBORw0KGgoAA\nAANSUhEUgAAABAAAAAQAQMAAAAlPW0iAAAABlBMVEUAAAD///+l2Z/dAAAAM0l\nEQVR4nGP4/5/h/1+G/58ZDrAz3D/McH8yw83NDDeNGe4Ug9C9zwz3gVLMDA/A6\nP9/AFGGFyjOXZtQAAAAAElFTkSuQmCC",
								ObjectUtils.emptyMap()));
		assertEquals(result.getLong("width"), 16L);
		assertEquals(result.getLong("height"), 16L);
		Map<String, Object> to_sign = new HashMap<String, Object>();
		to_sign.put("public_id", (String) result.get("public_id"));
		to_sign.put("version", ObjectUtils.asString(result.get("version")));
		String expected_signature = cloudinary.apiSignRequest(to_sign, cloudinary.config.apiSecret);
		assertEquals(result.get("signature"), expected_signature);
	}

	public void testUploadExternalSignature() throws Exception {
		String apiSecret = cloudinary.config.apiSecret;
		if (apiSecret == null)
			return;
		Map<String, String> config = new HashMap<String, String>();
		config.put("api_key", cloudinary.config.apiKey);
		config.put("cloud_name", cloudinary.config.cloudName);

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("timestamp", Long.valueOf(System.currentTimeMillis() / 1000L).toString());
		params.put("signature", this.cloudinary.apiSignRequest(params, apiSecret));
		Cloudinary emptyCloudinary = new Cloudinary(config);
		JSONObject result = new JSONObject(emptyCloudinary.uploader().upload(getAssetStream(TEST_IMAGE), params));
		assertEquals(result.getLong("width"), 241L);
		assertEquals(result.getLong("height"), 51L);
		Map<String, Object> to_sign = new HashMap<String, Object>();
		to_sign.put("public_id", result.getString("public_id"));
		to_sign.put("version", ObjectUtils.asString(result.get("version")));
		String expected_signature = cloudinary.apiSignRequest(to_sign, apiSecret);
		assertEquals(result.get("signature"), expected_signature);
	}

	public void testRename() throws Exception {
		if (cloudinary.config.apiSecret == null)
			return;
		JSONObject result = new JSONObject(cloudinary.uploader().upload(getAssetStream(TEST_IMAGE), ObjectUtils.emptyMap()));

		cloudinary.uploader().rename(result.getString("public_id"), result.get("public_id") + "2", ObjectUtils.emptyMap());

		JSONObject result2 = new JSONObject(cloudinary.uploader().upload(getAssetStream("images/favicon.ico"), ObjectUtils.emptyMap()));
		boolean error_found = false;
		try {
			cloudinary.uploader().rename((String) result2.get("public_id"), result.get("public_id") + "2", ObjectUtils.emptyMap());
		} catch (Exception e) {
			error_found = true;
		}
		assertTrue(error_found);
		cloudinary.uploader().rename((String) result2.get("public_id"), result.get("public_id") + "2", ObjectUtils.asMap("overwrite", Boolean.TRUE));
	}

	public void testExplicit() throws Exception {
		if (cloudinary.config.apiSecret == null)
			return;
		JSONObject result = new JSONObject(cloudinary.uploader().explicit("cloudinary",
				ObjectUtils.asMap("eager", Collections.singletonList(new Transformation().crop("scale").width(2.0)), "type", "twitter_name")));
		String url = cloudinary.url().type("twitter_name").transformation(new Transformation().crop("scale").width(2.0)).format("png")
				.version(result.get("version")).generate("cloudinary");
		assertEquals(result.getJSONArray("eager").getJSONObject(0).get("url"), url);
	}

	public void testEager() throws Exception {
		if (cloudinary.config.apiSecret == null)
			return;
		cloudinary.uploader().upload(getAssetStream(TEST_IMAGE),
				ObjectUtils.asMap("eager", Collections.singletonList(new Transformation().crop("scale").width(2.0))));
	}

	public void testHeaders() throws Exception {
		if (cloudinary.config.apiSecret == null)
			return;
		cloudinary.uploader().upload(getAssetStream(TEST_IMAGE), ObjectUtils.asMap("headers", new String[] { "Link: 1" }));
		cloudinary.uploader().upload(getAssetStream(TEST_IMAGE), ObjectUtils.asMap("headers", ObjectUtils.asMap("Link", "1")));
	}

	public void testText() throws Exception {
		if (cloudinary.config.apiSecret == null)
			return;
		JSONObject result = new JSONObject(cloudinary.uploader().text("hello world", ObjectUtils.emptyMap()));
		assertTrue(result.getInt("width") > 1);
		assertTrue(result.getInt("height") > 1);
	}

	public void testSprite() throws Exception {
		if (cloudinary.config.apiSecret == null)
			return;
		cloudinary.uploader().upload(getAssetStream(TEST_IMAGE), ObjectUtils.asMap("tags", "sprite_test_tag", "public_id", "sprite_test_tag_1"));
		cloudinary.uploader().upload(getAssetStream(TEST_IMAGE), ObjectUtils.asMap("tags", "sprite_test_tag", "public_id", "sprite_test_tag_2"));
		JSONObject result = new JSONObject(cloudinary.uploader().generate_sprite("sprite_test_tag", ObjectUtils.emptyMap()));
		assertEquals(2, result.getJSONObject("image_infos").length());
		result = new JSONObject(cloudinary.uploader().generate_sprite("sprite_test_tag", ObjectUtils.asMap("transformation", "w_100")));
		assertTrue((result.getString("css_url")).contains("w_100"));
		result = new JSONObject(cloudinary.uploader().generate_sprite("sprite_test_tag",
				ObjectUtils.asMap("transformation", new Transformation().width(100), "format", "jpg")));
		assertTrue((result.getString("css_url")).contains("f_jpg,w_100"));
	}

	public void testMulti() throws Exception {
		if (cloudinary.config.apiSecret == null)
			return;
		cloudinary.uploader().upload(getAssetStream(TEST_IMAGE), ObjectUtils.asMap("tags", "multi_test_tag", "public_id", "multi_test_tag_1"));
		cloudinary.uploader().upload(getAssetStream(TEST_IMAGE), ObjectUtils.asMap("tags", "multi_test_tag", "public_id", "multi_test_tag_2"));
		JSONObject result = new JSONObject(cloudinary.uploader().multi("multi_test_tag", ObjectUtils.emptyMap()));
		assertTrue((result.getString("url")).endsWith(".gif"));
		result = new JSONObject(cloudinary.uploader().multi("multi_test_tag", ObjectUtils.asMap("transformation", "w_100")));
		assertTrue((result.getString("url")).contains("w_100"));
		result = new JSONObject(cloudinary.uploader().multi("multi_test_tag", ObjectUtils.asMap("transformation", new Transformation().width(111), "format", "pdf")));
		assertTrue((result.getString("url")).contains("w_111"));
		assertTrue((result.getString("url")).endsWith(".pdf"));
	}

	public void testUniqueFilename() throws Exception {
		if (cloudinary.config.apiSecret == null)
			return;

		File f = new File(getInstrumentation().getContext().getCacheDir() + "/old_logo.png");

		InputStream is = getAssetStream(TEST_IMAGE);
		int size = is.available();
		byte[] buffer = new byte[size];
		is.read(buffer);
		is.close();

		FileOutputStream fos = new FileOutputStream(f);
		fos.write(buffer);
		fos.close();

		JSONObject result = new JSONObject(cloudinary.uploader().upload(f, ObjectUtils.asMap("use_filename", true)));
		assertTrue(result.getString("public_id").matches("old_logo_[a-z0-9]{6}"));
		result = new JSONObject(cloudinary.uploader().upload(f, ObjectUtils.asMap("use_filename", true, "unique_filename", false)));
		assertEquals(result.getString("public_id"), "old_logo");
	}

	public void testFaceCoordinates() throws Exception {
		// should allow sending face coordinates
		if (cloudinary.config.apiSecret == null)
			return;
		Coordinates coordinates = new Coordinates();
		Rectangle rect1 = new Rectangle(121, 31, 231, 182);
		Rectangle rect2 = new Rectangle(120, 30, 229, 270);
		coordinates.addRect(rect1);
		coordinates.addRect(rect2);
		JSONObject result = new JSONObject(cloudinary.uploader().upload(getAssetStream(TEST_IMAGE), ObjectUtils.asMap("face_coordinates", coordinates, "faces", true)));
		JSONArray resultFaces = result.getJSONArray("faces");
		assertEquals(2, resultFaces.length());

		JSONArray resultCoordinates = resultFaces.getJSONArray(0);

		assertEquals(rect1.x, resultCoordinates.getInt(0));
		assertEquals(rect1.y, resultCoordinates.getInt(1));
		assertEquals(rect1.width, resultCoordinates.getInt(2));
		assertEquals(rect1.height, resultCoordinates.getInt(3));

		resultCoordinates = resultFaces.getJSONArray(1);

		assertEquals(rect2.x, resultCoordinates.getInt(0));
		assertEquals(rect2.y, resultCoordinates.getInt(1));
		assertEquals(rect2.width, resultCoordinates.getInt(2));
		assertEquals(rect2.height, resultCoordinates.getInt(3));

	}

	public void testContext() throws Exception {
		// should allow sending context
		if (cloudinary.config.apiSecret == null)
			return;
		@SuppressWarnings("rawtypes")
		Map context = ObjectUtils.asMap("caption", "some caption", "alt", "alternative");
		cloudinary.uploader().upload(getAssetStream(TEST_IMAGE), ObjectUtils.asMap("context", context));
	}

	public void testModerationRequest() throws Exception {
		// should support requesting manual moderation
		if (cloudinary.config.apiSecret == null)
			return;
		JSONObject result = new JSONObject(cloudinary.uploader().upload(getAssetStream(TEST_IMAGE), ObjectUtils.asMap("moderation", "manual")));
		assertEquals("manual", result.getJSONArray("moderation").getJSONObject(0).getString("kind"));
		assertEquals("pending", result.getJSONArray("moderation").getJSONObject(0).getString("status"));
	}

	public void testRawConvertRequest() {
		// should support requesting raw conversion
		if (cloudinary.config.apiSecret == null)
			return;
		try {
			cloudinary.uploader().upload(getAssetStream("docx.docx"), ObjectUtils.asMap("raw_convert", "illegal", "resource_type", "raw"));
		} catch (Exception e) {
			assertTrue(e.getMessage().matches(".*illegal is not a valid.*"));
		}
	}

	public void testCategorizationRequest() {
		// should support requesting categorization
		if (cloudinary.config.apiSecret == null)
			return;
		try {
			cloudinary.uploader().upload(getAssetStream(TEST_IMAGE), ObjectUtils.asMap("categorization", "illegal"));
		} catch (Exception e) {
			assertTrue(e.getMessage().matches(".*illegal is not a valid.*"));
		}
	}

	public void testDetectionRequest() {
		// should support requesting detection
		if (cloudinary.config.apiSecret == null)
			return;
		try {
			cloudinary.uploader().upload(getAssetStream(TEST_IMAGE), ObjectUtils.asMap("detection", "illegal"));
		} catch (Exception e) {
			assertTrue(e.getMessage().matches(".*illegal is not a valid.*"));
		}
	}

	public void testAutoTaggingRequest() {
		// should support requesting auto tagging
		if (cloudinary.config.apiSecret == null)
			return;
		
		try {
			cloudinary.uploader().upload(getAssetStream(TEST_IMAGE), ObjectUtils.asMap("auto_tagging", 0.5f));
		} catch (Exception e) {
			for (int i = 0; i < e.getStackTrace().length; i++) {
				StackTraceElement x = e.getStackTrace()[i];
			}
			assertTrue(e.getMessage().matches("^Must use(.*)"));
		}
	}
	
	@SuppressWarnings("unchecked")
	public void testUploadLarge() throws Exception {
    	// support uploading large files
		if (cloudinary.config.apiSecret == null)
			return;
        
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
        
        JSONObject resource = new JSONObject(cloudinary.uploader().uploadLarge(temp, ObjectUtils.asMap("resource_type", "raw", "chunk_size", 5243000)));
        assertEquals("raw", resource.getString("resource_type"));
        
        resource = new JSONObject(cloudinary.uploader().uploadLarge(temp, ObjectUtils.asMap("chunk_size", 5243000)));
        assertEquals("image", resource.getString("resource_type"));
        assertEquals(1400L, resource.getLong("width"));
        assertEquals(1400L, resource.getLong("height"));
        
        resource = new JSONObject(cloudinary.uploader().uploadLarge(temp, ObjectUtils.asMap("chunk_size", 5880138)));
        assertEquals("image", resource.getString("resource_type"));
        assertEquals(1400L, resource.getLong("width"));
        assertEquals(1400L, resource.getLong("height"));
    }
}
