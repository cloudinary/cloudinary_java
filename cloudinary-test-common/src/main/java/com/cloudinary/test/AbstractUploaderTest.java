package com.cloudinary.test;

import com.cloudinary.*;
import com.cloudinary.utils.ObjectUtils;
import com.cloudinary.utils.Rectangle;
import org.cloudinary.json.JSONArray;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.*;
import org.junit.rules.TestName;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.zip.ZipInputStream;

import static com.cloudinary.utils.ObjectUtils.asArray;
import static com.cloudinary.utils.ObjectUtils.asMap;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.junit.Assume.assumeNotNull;

@SuppressWarnings({"rawtypes", "unchecked"})
abstract public class AbstractUploaderTest extends MockableTest {

    private static final String ARCHIVE_TAG = "archive_tag_" + String.valueOf(System.currentTimeMillis());
    public static final int SRC_TEST_IMAGE_W = 241;
    public static final int SRC_TEST_IMAGE_H = 51;

    @BeforeClass
    public static void setUpClass() throws IOException {
        Cloudinary cloudinary = new Cloudinary();
        if (cloudinary.config.apiSecret == null) {
            System.err.println("Please setup environment for Upload test to run");
        }

        cloudinary.uploader().upload(SRC_TEST_IMAGE, asMap("tags", new String [] {SDK_TEST_TAG, ARCHIVE_TAG}));
        cloudinary.uploader().upload(SRC_TEST_IMAGE,
                asMap("tags", new String [] {SDK_TEST_TAG, ARCHIVE_TAG},
                        "transformation", new Transformation().crop("scale").width(10)));
    }

    @AfterClass
    public static void tearDownClass() {
        Api api = MockableTest.cleanUp();
        Cloudinary cloudinary = new Cloudinary();
        try {
            api.deleteResourcesByTag(ARCHIVE_TAG, ObjectUtils.emptyMap());
        } catch (Exception ignored) {
        }
    }

    @Rule
    public TestName currentTest = new TestName();

    @Before
    public void setUp() {
        System.out.println("Running " + this.getClass().getName() + "." + currentTest.getMethodName());
        this.cloudinary = new Cloudinary();
        assumeNotNull(cloudinary.config.apiSecret);
    }

    @Test
    public void testUtf8Upload() throws IOException {
        Map result = cloudinary.uploader().upload(SRC_TEST_IMAGE, asMap("colors", true, "tags", SDK_TEST_TAG, "public_id", "aåßéƒ"));
        assertEquals(result.get("width"), SRC_TEST_IMAGE_W);
        assertEquals(result.get("height"), SRC_TEST_IMAGE_H);
        assertNotNull(result.get("colors"));
        assertNotNull(result.get("predominant"));
        Map<String, Object> to_sign = new HashMap<String, Object>();
        to_sign.put("public_id", result.get("public_id"));
        to_sign.put("version", ObjectUtils.asString(result.get("version")));
        String expected_signature = cloudinary.apiSignRequest(to_sign, cloudinary.config.apiSecret);
        assertEquals(result.get("signature"), expected_signature);
    }

    @Test
    public void testUpload() throws IOException {
        Map result = cloudinary.uploader().upload(SRC_TEST_IMAGE, asMap("colors", true, "tags", SDK_TEST_TAG));
        assertEquals(result.get("width"), SRC_TEST_IMAGE_W);
        assertEquals(result.get("height"), SRC_TEST_IMAGE_H);
        assertNotNull(result.get("colors"));
        assertNotNull(result.get("predominant"));
        Map<String, Object> to_sign = new HashMap<String, Object>();
        to_sign.put("public_id", result.get("public_id"));
        to_sign.put("version", ObjectUtils.asString(result.get("version")));
        String expected_signature = cloudinary.apiSignRequest(to_sign, cloudinary.config.apiSecret);
        assertEquals(result.get("signature"), expected_signature);
    }

    @Test
    public void testUploadUrl() throws IOException {
        Map result = cloudinary.uploader().upload(REMOTE_TEST_IMAGE, asMap("tags", SDK_TEST_TAG));
        assertEquals(result.get("width"), SRC_TEST_IMAGE_W);
        assertEquals(result.get("height"), SRC_TEST_IMAGE_H);
        Map<String, Object> to_sign = new HashMap<String, Object>();
        to_sign.put("public_id", result.get("public_id"));
        to_sign.put("version", ObjectUtils.asString(result.get("version")));
        String expected_signature = cloudinary.apiSignRequest(to_sign, cloudinary.config.apiSecret);
        assertEquals(result.get("signature"), expected_signature);
    }

    @Test
    public void testUploadDataUri() throws IOException {
        Map result = cloudinary.uploader().upload("data:image/png;base64,iVBORw0KGgoAA\nAANSUhEUgAAABAAAAAQAQMAAAAlPW0iAAAABlBMVEUAAAD///+l2Z/dAAAAM0l\nEQVR4nGP4/5/h/1+G/58ZDrAz3D/McH8yw83NDDeNGe4Ug9C9zwz3gVLMDA/A6\nP9/AFGGFyjOXZtQAAAAAElFTkSuQmCC", asMap("tags", SDK_TEST_TAG));
        assertEquals(result.get("width"), 16);
        assertEquals(result.get("height"), 16);
        Map<String, Object> to_sign = new HashMap<String, Object>();
        to_sign.put("public_id", result.get("public_id"));
        to_sign.put("version", ObjectUtils.asString(result.get("version")));
        String expected_signature = cloudinary.apiSignRequest(to_sign, cloudinary.config.apiSecret);
        assertEquals(result.get("signature"), expected_signature);
    }

    @Test
    public void testUploadUTF8() throws IOException {
        Map result = cloudinary.uploader().upload("../cloudinary-test-common/src/main/resources/old_logo.png", asMap("public_id", "Plattenkreiss_ñg-é", "tags", SDK_TEST_TAG));
        assertEquals(result.get("public_id"), "Plattenkreiss_ñg-é");
        cloudinary.uploader().upload(result.get("url"), asMap("tags", SDK_TEST_TAG));
    }

    @Test
    public void testRename() throws Exception {
        Map result = cloudinary.uploader().upload(SRC_TEST_IMAGE, asMap("tags", SDK_TEST_TAG));

        Object publicId = result.get("public_id");
        String publicId2 = "folder/" + publicId + "2";
        cloudinary.uploader().rename((String) publicId, publicId2, ObjectUtils.emptyMap());
        assertNotNull(cloudinary.api().resource(publicId2, ObjectUtils.emptyMap()));

        Map result2 = cloudinary.uploader().upload("../cloudinary-test-common/src/main/resources/favicon.ico", asMap("tags", SDK_TEST_TAG));
        boolean error_found=false;
        try {
        	cloudinary.uploader().rename((String) result2.get("public_id"), publicId2, asMap("tags", SDK_TEST_TAG));
        } catch(Exception e) {
        	error_found=true;
        }
        assertTrue(error_found);
        cloudinary.uploader().rename((String) result2.get("public_id"), publicId2, asMap("overwrite", true, "tags", SDK_TEST_TAG));
        assertEquals(cloudinary.api().resource(publicId2, ObjectUtils.emptyMap()).get("format"), "ico");
    }

    @Test
    public void testUniqueFilename() throws Exception {
        Map result = cloudinary.uploader().upload(SRC_TEST_IMAGE, asMap("use_filename", true, "tags", SDK_TEST_TAG));
        assertTrue(((String) result.get("public_id")).matches("old_logo_[a-z0-9]{6}"));
        result = cloudinary.uploader().upload(SRC_TEST_IMAGE, asMap("use_filename", true, "unique_filename", false, "tags", SDK_TEST_TAG));
        assertEquals(result.get("public_id"), "old_logo");
    }

    @Test
    public void testExplicit() throws IOException {
        Map result = cloudinary.uploader().explicit("sample", asMap("eager", Collections.singletonList(new Transformation().crop("scale").width(2.0)), "type", "upload"));
        String url = cloudinary.url().transformation(new Transformation().crop("scale").width(2.0)).format("jpg").version(result.get("version")).generate("sample");
        String eagerUrl = (String) ((Map) ((List) result.get("eager")).get(0)).get("url");
        String cloudName = cloudinary.config.cloudName;
        assertEquals(eagerUrl.substring(eagerUrl.indexOf(cloudName)), url.substring(url.indexOf(cloudName)));
    }

    @Test
    public void testEager() throws IOException {
        cloudinary.uploader().upload(SRC_TEST_IMAGE, asMap("eager", Collections.singletonList(new Transformation().crop("scale").width(2.0)), "tags", SDK_TEST_TAG));
    }

    @Test
    public void testHeaders() throws IOException {
        cloudinary.uploader().upload(SRC_TEST_IMAGE, asMap("headers", new String[]{"Link: 1"}, "tags", SDK_TEST_TAG));
        cloudinary.uploader().upload(SRC_TEST_IMAGE, asMap("headers", asMap("Link", "1"), "tags", SDK_TEST_TAG));
    }

    @Test
    public void testText() throws IOException {
        Map result = cloudinary.uploader().text("hello world", asMap("tags", SDK_TEST_TAG));
        assertTrue(((Integer) result.get("width")) > 1);
        assertTrue(((Integer) result.get("height")) > 1);
    }

    @Test
    public void testImageUploadTag() {
        String tag = cloudinary.uploader().imageUploadTag("test-field", asMap("callback", "http://localhost/cloudinary_cors.html"), asMap("htmlattr", "htmlvalue"));
        assertTrue(tag.contains("type='file'"));
        assertTrue(tag.contains("data-cloudinary-field='test-field'"));
        assertTrue(tag.contains("class='cloudinary-fileupload'"));
        assertTrue(tag.contains("htmlattr='htmlvalue'"));
        tag = cloudinary.uploader().imageUploadTag("test-field", asMap("callback", "http://localhost/cloudinary_cors.html"), asMap("class", "myclass"));
        assertTrue(tag.contains("class='cloudinary-fileupload myclass'"));
    }

    @Test
    public void testSprite() throws IOException {
        final String sprite_test_tag = String.format("sprite_test_tag_%d", new java.util.Date().getTime()) ;
        cloudinary.uploader().upload(SRC_TEST_IMAGE, asMap("tags", new String [] {sprite_test_tag, SDK_TEST_TAG}, "public_id", "sprite_test_tag_1"));
        cloudinary.uploader().upload(SRC_TEST_IMAGE, asMap("tags", new String [] {sprite_test_tag, SDK_TEST_TAG}, "public_id", "sprite_test_tag_2"));
        Map result = cloudinary.uploader().generateSprite(sprite_test_tag, asMap("tags", SDK_TEST_TAG));
        assertEquals(2, ((Map) result.get("image_infos")).size());
        result = cloudinary.uploader().generateSprite(sprite_test_tag, asMap("transformation", "w_100"));
        assertTrue(((String) result.get("css_url")).contains("w_100"));
        result = cloudinary.uploader().generateSprite(sprite_test_tag, asMap("transformation", new Transformation().width(100), "format", "jpg"));
        assertTrue(((String) result.get("css_url")).contains("f_jpg,w_100"));
    }

    @Test
    public void testMulti() throws IOException {
        cloudinary.uploader().upload(SRC_TEST_IMAGE, asMap("tags", new String[] {"multi_test_tag", SDK_TEST_TAG}, "public_id", "multi_test_tag_1"));
        cloudinary.uploader().upload(SRC_TEST_IMAGE, asMap("tags", new String[] {"multi_test_tag", SDK_TEST_TAG}, "public_id", "multi_test_tag_2"));
        Map result = cloudinary.uploader().multi("multi_test_tag", asMap("tags", SDK_TEST_TAG));
        assertTrue(((String) result.get("url")).endsWith(".gif"));
        result = cloudinary.uploader().multi("multi_test_tag", asMap("transformation", "w_100"));
        assertTrue(((String) result.get("url")).contains("w_100"));
        result = cloudinary.uploader().multi("multi_test_tag", asMap("transformation", new Transformation().width(111), "format", "pdf"));
        assertTrue(((String) result.get("url")).contains("w_111"));
        assertTrue(((String) result.get("url")).endsWith(".pdf"));
    }

    @Test
    public void testTags() throws Exception {
        Map result = cloudinary.uploader().upload(SRC_TEST_IMAGE, ObjectUtils.emptyMap());
        String public_id = (String) result.get("public_id");
        Map result2 = cloudinary.uploader().upload(SRC_TEST_IMAGE, ObjectUtils.emptyMap());
        String public_id2 = (String) result2.get("public_id");
        cloudinary.uploader().addTag("tag1", new String[]{public_id, public_id2}, ObjectUtils.emptyMap());
        cloudinary.uploader().addTag("tag2", new String[]{public_id}, ObjectUtils.emptyMap());
        List<String> tags = (List<String>) cloudinary.api().resource(public_id, ObjectUtils.emptyMap()).get("tags");
        assertEquals(tags, asArray(new String[]{"tag1", "tag2"}));
        tags = (List<String>) cloudinary.api().resource(public_id2, ObjectUtils.emptyMap()).get("tags");
        assertEquals(tags, asArray(new String[]{"tag1"}));
        cloudinary.uploader().removeTag("tag1", new String[]{public_id}, ObjectUtils.emptyMap());
        tags = (List<String>) cloudinary.api().resource(public_id, ObjectUtils.emptyMap()).get("tags");
        assertEquals(tags, asArray(new String[]{"tag2"}));
        cloudinary.uploader().replaceTag("tag3", new String[]{public_id}, ObjectUtils.emptyMap());
        tags = (List<String>) cloudinary.api().resource(public_id, ObjectUtils.emptyMap()).get("tags");
        assertEquals(tags, asArray(new String[]{"tag3"}));
        result = cloudinary.uploader().removeAllTags(new String[]{public_id, public_id2, "noSuchId"}, ObjectUtils.emptyMap());
        List<String> publicIds = (List<String>) result.get("public_ids");
        assertThat(publicIds, containsInAnyOrder(public_id, public_id2)); // = and not containing "noSuchId"
        result = cloudinary.api().resource(public_id, ObjectUtils.emptyMap());
        assertThat((Map<? extends String, ?>) result, not(hasKey("tags")));

    }

    @Test
    public void testAllowedFormats() throws Exception {
        //should allow whitelisted formats if allowed_formats
        String[] formats = {"png"};
        Map result = cloudinary.uploader().upload(SRC_TEST_IMAGE, asMap("allowed_formats", formats, "tags", SDK_TEST_TAG));
        assertEquals(result.get("format"), "png");
    }

    @Test
    public void testAllowedFormatsWithIllegalFormat() throws Exception {
        //should prevent non whitelisted formats from being uploaded if allowed_formats is specified
        boolean errorFound = false;
        String[] formats = {"jpg"};
        try {
            cloudinary.uploader().upload(SRC_TEST_IMAGE, asMap("allowed_formats", formats, "tags", SDK_TEST_TAG));
        } catch (Exception e) {
            errorFound = true;
        }
        assertTrue(errorFound);
    }

    @Test
    public void testAllowedFormatsWithFormat() throws Exception {
        //should allow non whitelisted formats if type is specified and convert to that type
        String[] formats = {"jpg"};
        Map result = cloudinary.uploader().upload(SRC_TEST_IMAGE, asMap("allowed_formats", formats, "format", "jpg", "tags", SDK_TEST_TAG));
        assertEquals("jpg", result.get("format"));
    }

    @Test
    public void testFaceCoordinates() throws Exception {
        //should allow sending face coordinates
        Coordinates coordinates = new Coordinates();
        Rectangle rect1 = new Rectangle(121, 31, 110, 151);
        Rectangle rect2 = new Rectangle(120, 30, 109, 150);
        coordinates.addRect(rect1);
        coordinates.addRect(rect2);
        Map result = cloudinary.uploader().upload(SRC_TEST_IMAGE, asMap("face_coordinates", coordinates, "faces", true, "tags", SDK_TEST_TAG));
        ArrayList resultFaces = ((ArrayList) result.get("faces"));
        assertEquals(2, resultFaces.size());

        Object[] resultCoordinates = ((ArrayList) resultFaces.get(0)).toArray();

        assertEquals(rect1.x, resultCoordinates[0]);
        assertEquals(rect1.y, resultCoordinates[1]);
        assertEquals(rect1.width, resultCoordinates[2]);
        assertEquals(rect1.height, resultCoordinates[3]);

        resultCoordinates = ((ArrayList) resultFaces.get(1)).toArray();

        assertEquals(rect2.x, resultCoordinates[0]);
        assertEquals(rect2.y, resultCoordinates[1]);
        assertEquals(rect2.width, resultCoordinates[2]);
        assertEquals(rect2.height, resultCoordinates[3]);

        Coordinates differentCoordinates = new Coordinates();
        Rectangle rect3 = new Rectangle(122, 32, 111, 152);
        differentCoordinates.addRect(rect3);
        cloudinary.uploader().explicit((String) result.get("public_id"), asMap("face_coordinates", differentCoordinates, "faces", true, "type", "upload"));
        Map info = cloudinary.api().resource((String) result.get("public_id"), asMap("faces", true));

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
        Coordinates coordinates = new Coordinates("121,31,300,151");
        Map uploadResult = cloudinary.uploader().upload(SRC_TEST_IMAGE, asMap("custom_coordinates", coordinates, "tags", SDK_TEST_TAG));
        Map result = cloudinary.api().resource(uploadResult.get("public_id").toString(), asMap("coordinates", true));
        int[] expected = new int[]{121, 31, SRC_TEST_IMAGE_W, SRC_TEST_IMAGE_H};
        Object[] actual = ((ArrayList) ((ArrayList) ((Map) result.get("coordinates")).get("custom")).get(0)).toArray();
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], actual[i]);
        }

        coordinates = new Coordinates(new int[]{122, 32, SRC_TEST_IMAGE_W + 100, SRC_TEST_IMAGE_H + 100});
        cloudinary.uploader().explicit((String) uploadResult.get("public_id"), asMap("custom_coordinates", coordinates, "coordinates", true, "type", "upload"));
        result = cloudinary.api().resource(uploadResult.get("public_id").toString(), asMap("coordinates", true));
        expected = new int[]{122, 32, SRC_TEST_IMAGE_W + 100, SRC_TEST_IMAGE_H + 100};
        actual = ((ArrayList) ((ArrayList) ((Map) result.get("coordinates")).get("custom")).get(0)).toArray();
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], actual[i]);
        }
    }

    @Test
    public void testModerationRequest() throws Exception {
        //should support requesting manual moderation
        Map result = cloudinary.uploader().upload(SRC_TEST_IMAGE, asMap("moderation", "manual", "tags", SDK_TEST_TAG));
        assertEquals("manual", ((List<Map>) result.get("moderation")).get(0).get("kind"));
        assertEquals("pending", ((List<Map>) result.get("moderation")).get(0).get("status"));
    }


    @Test
    public void testRawConvertRequest() {
        //should support requesting raw conversion
        try {
            cloudinary.uploader().upload(SRC_TEST_IMAGE, asMap("raw_convert", "illegal", "tags", SDK_TEST_TAG));
        } catch (Exception e) {
            assertTrue(e.getMessage().matches("(.*)(Illegal value|not a valid)(.*)"));
        }
    }

    @Test
    public void testCategorizationRequest() {
        //should support requesting categorization
        try {
            cloudinary.uploader().upload(SRC_TEST_IMAGE, asMap("categorization", "illegal", "tags", SDK_TEST_TAG));
        } catch (Exception e) {
            assertTrue(e.getMessage().matches("(.*)(Illegal value|not a valid|invalid)(.*)"));
        }
    }

    @Test
    public void testDetectionRequest() {
        //should support requesting detection
        try {
            cloudinary.uploader().upload(SRC_TEST_IMAGE, asMap("detection", "illegal", "tags", SDK_TEST_TAG));
        } catch (Exception e) {
            assertTrue(e.getMessage().matches("(.*)(Illegal value|not a valid)(.*)"));
        }
    }

    @Test
    public void testAutoTaggingRequest() {
        //should support requesting auto tagging
        try {
            cloudinary.uploader().upload(SRC_TEST_IMAGE, asMap("auto_tagging", 0.5f, "tags", SDK_TEST_TAG));
        } catch (Exception e) {
            assertTrue(e.getMessage().matches("^Must use(.*)"));
        }
    }

    @Test
    public void testUploadLarge() throws Exception {
        // support uploading large files

        File temp = File.createTempFile("cldupload.test.", "");
        FileOutputStream out = new FileOutputStream(temp);
        int[] header = new int[]{0x42, 0x4D, 0x4A, 0xB9, 0x59, 0x00, 0x00, 0x00, 0x00, 0x00, 0x8A, 0x00, 0x00, 0x00, 0x7C, 0x00, 0x00, 0x00, 0x78, 0x05, 0x00, 0x00, 0x78, 0x05, 0x00, 0x00, 0x01, 0x00, 0x18, 0x00, 0x00, 0x00, 0x00, 0x00, 0xC0, 0xB8, 0x59, 0x00, 0x61, 0x0F, 0x00, 0x00, 0x61, 0x0F, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0xFF, 0x00, 0x00, 0xFF, 0x00, 0x00, 0xFF, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0xFF, 0x42, 0x47, 0x52, 0x73, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x54, 0xB8, 0x1E, 0xFC, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x66, 0x66, 0x66, 0xFC, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0xC4, 0xF5, 0x28, 0xFF, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x04, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
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

        String [] tags = new String [] {"upload_large_tag", SDK_TEST_TAG};

        Map resource = cloudinary.uploader().uploadLarge(temp, asMap("resource_type", "raw", "chunk_size", 5243000, "tags", tags));
        assertArrayEquals(tags, ((java.util.ArrayList) resource.get("tags")).toArray());

        assertEquals("raw", resource.get("resource_type"));

        resource = cloudinary.uploader().uploadLarge(new FileInputStream(temp), asMap("chunk_size", 5243000, "tags", tags));
        assertArrayEquals(tags, ((java.util.ArrayList) resource.get("tags")).toArray());
        assertEquals("image", resource.get("resource_type"));
        assertEquals(1400, resource.get("width"));
        assertEquals(1400, resource.get("height"));

        resource = cloudinary.uploader().uploadLarge(temp, asMap("chunk_size", 5880138, "tags", tags));
        assertArrayEquals(tags, ((java.util.ArrayList)  resource.get("tags")).toArray());
        assertEquals("image", resource.get("resource_type"));
        assertEquals(1400, resource.get("width"));
        assertEquals(1400, resource.get("height"));

        resource = cloudinary.uploader().uploadLarge(new FileInputStream(temp), asMap("chunk_size", 5880138, "tags", tags));
        assertArrayEquals(tags, ((java.util.ArrayList) resource.get("tags")).toArray());
        assertEquals("image", resource.get("resource_type"));
        assertEquals(1400, resource.get("width"));
        assertEquals(1400, resource.get("height"));
    }

    @Test
    public void testUnsignedUpload() throws Exception {
        // should support unsigned uploading using presets
        Map preset = cloudinary.api().createUploadPreset(asMap("folder", "upload_folder", "unsigned", true));
        Map result = cloudinary.uploader().unsignedUpload(SRC_TEST_IMAGE, preset.get("name").toString(), asMap("tags", SDK_TEST_TAG));
        assertTrue(result.get("public_id").toString().matches("^upload_folder\\/[a-z0-9]+$"));
        cloudinary.api().deleteUploadPreset(preset.get("name").toString(), ObjectUtils.emptyMap());
    }

    @Test
    public void testFilenameOption() throws Exception {
        Map result = cloudinary.uploader().upload(SRC_TEST_IMAGE, asMap("filename", "emanelif", "tags", SDK_TEST_TAG));
        assertEquals("emanelif", result.get("original_filename"));
    }

    @Test
    public void testResponsiveBreakpoints() throws Exception {
        ResponsiveBreakpoint breakpoint = new ResponsiveBreakpoint().maxImages(2).createDerived(false);

        // A single breakpoint
        Map result = cloudinary.uploader().upload(SRC_TEST_IMAGE, asMap("responsive_breakpoints",
                breakpoint, "tags", SDK_TEST_TAG
        ));
        java.util.ArrayList breakpointsResponse = (java.util.ArrayList) result.get("responsive_breakpoints");
        java.util.ArrayList breakpoints = (java.util.ArrayList) ((Map) breakpointsResponse.get(0)).get("breakpoints");
        assertEquals(2, breakpoints.size());

        // an array of breakpoints
        result = cloudinary.uploader().upload(SRC_TEST_IMAGE, asMap("responsive_breakpoints",
                new ResponsiveBreakpoint[]{breakpoint}, "tags", SDK_TEST_TAG
        ));
        breakpointsResponse = (java.util.ArrayList) result.get("responsive_breakpoints");
        breakpoints = (java.util.ArrayList) ((Map) breakpointsResponse.get(0)).get("breakpoints");
        assertEquals(2, breakpoints.size());

        // a JSONArray of breakpoints
        JSONArray array = new JSONArray();
        array.put(breakpoint);
        result = cloudinary.uploader().upload(SRC_TEST_IMAGE, asMap("responsive_breakpoints", array, "tags", SDK_TEST_TAG
        ));
        breakpointsResponse = (java.util.ArrayList) result.get("responsive_breakpoints");
        breakpoints = (java.util.ArrayList) ((Map) breakpointsResponse.get(0)).get("breakpoints");
        assertEquals(2, breakpoints.size());
    }

    @Test
    public void testCreateArchive() throws Exception {
        Map result = cloudinary.uploader().createArchive(new ArchiveParams().tags(new String[]{ARCHIVE_TAG}));
        assertEquals(2, result.get("file_count"));
        result = cloudinary.uploader().createArchive(
                new ArchiveParams().tags(new String[]{ARCHIVE_TAG}).transformations(
                        new Transformation[]{new Transformation().width(0.5), new Transformation().width(2.0)}));
        assertEquals(4, result.get("file_count"));
    }

    @Test
    public void testDownloadArchive() throws Exception {
        String result = cloudinary.downloadArchive(new ArchiveParams().tags(new String[]{ARCHIVE_TAG}));
        URL url = new java.net.URL(result);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        ZipInputStream in = new ZipInputStream(new BufferedInputStream(urlConnection.getInputStream()));
        int files = 0;
        try {
            while ((in.getNextEntry()) != null) {
                files += 1;
            }
        } finally {
            in.close();
        }
        assertEquals(2, files);
    }
    
    public void testUploadInvalidUrl() {
        try {
            cloudinary.uploader().upload(REMOTE_TEST_IMAGE + "\n", asMap("return_error", true));
            fail("Expected exception was not thrown");
        } catch(IOException e) {
            assertEquals(e.getMessage(), "File not found or unreadable: " + REMOTE_TEST_IMAGE + "\n");
        }
    }

}
