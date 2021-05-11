package com.cloudinary.transformation;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by amir on 03/11/2015.
 */
public class LayerTest {
    private static final String DEFAULT_ROOT_PATH = "http://res.cloudinary.com/test123/";
    private static final String DEFAULT_UPLOAD_PATH = DEFAULT_ROOT_PATH + "image/upload/";
    private static final String VIDEO_UPLOAD_PATH = DEFAULT_ROOT_PATH + "video/upload/";
    private Cloudinary cloudinary;

    @Before
    public void setUp() {
        this.cloudinary = new Cloudinary("cloudinary://a:b@test123?load_strategies=false");
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testOverlay() {
        // should support overlay
        Transformation transformation = new Transformation().overlay("text:hello");
        String result = cloudinary.url().transformation(transformation).generate("test");
        assertEquals(DEFAULT_UPLOAD_PATH + "l_text:hello/test", result);
        // should not pass width/height to html if overlay
        transformation = new Transformation().overlay("text:hello").width(100).height(100);
        result = cloudinary.url().transformation(transformation).generate("test");
        assertNull(transformation.getHtmlHeight());
        assertNull(transformation.getHtmlWidth());
        assertEquals(DEFAULT_UPLOAD_PATH + "h_100,l_text:hello,w_100/test", result);

        transformation = new Transformation().overlay(new TextLayer().text("goodbye"));
        result = cloudinary.url().transformation(transformation).generate("test");
        assertEquals(DEFAULT_UPLOAD_PATH + "l_text:goodbye/test", result);
    }

    @Test
    public void testUnderlay() {
        Transformation transformation = new Transformation().underlay("text:hello");
        String result = cloudinary.url().transformation(transformation).generate("test");
        assertEquals(DEFAULT_UPLOAD_PATH + "u_text:hello/test", result);
        // should not pass width/height to html if underlay
        transformation = new Transformation().underlay("text:hello").width(100).height(100);
        result = cloudinary.url().transformation(transformation).generate("test");
        assertNull(transformation.getHtmlHeight());
        assertNull(transformation.getHtmlWidth());
        assertEquals(DEFAULT_UPLOAD_PATH + "h_100,u_text:hello,w_100/test", result);
    }


    @Test
    public void testLayerOptions() {
        Object tests[] = {
                new Layer().publicId("logo"),
                "logo",
                new Layer().publicId("logo__111"), //testing SNI-4729
                "logo__111",
                new Layer().publicId("folder/logo"),
                "folder:logo",
                new Layer().publicId("logo").type("private"),
                "private:logo",
                new Layer().publicId("logo").format("png"),
                "logo.png",
                new Layer().resourceType("video").publicId("cat"),
                "video:cat",
                new TextLayer().text("Hello/World").fontFamily("Arial").fontSize(18),
                "text:Arial_18:Hello%252FWorld",
                new TextLayer().text("Hello World, Nice to meet you?").fontFamily("Arial").fontSize(18),
                "text:Arial_18:Hello%20World%252C%20Nice%20to%20meet%20you%3F",
                new TextLayer().text("Hello World, Nice to meet you?").fontFamily("Arial").fontSize(18)
                        .fontWeight("bold").fontStyle("italic").letterSpacing("4"),
                "text:Arial_18_bold_italic_letter_spacing_4:Hello%20World%252C%20Nice%20to%20meet%20you%3F",
                new SubtitlesLayer().publicId("sample_sub_en.srt"), "subtitles:sample_sub_en.srt",
                new SubtitlesLayer().publicId("sample_sub_he.srt").fontFamily("Arial").fontSize(40),
                "subtitles:Arial_40:sample_sub_he.srt"};

        for (int i = 0; i < tests.length; i += 2) {
            Object layer = tests[i];
            String expected = (String) tests[i + 1];
            assertEquals(expected, layer.toString());
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOverlayError1() {
        // Must supply font_family for text in overlay
        cloudinary.url().transformation(new Transformation().overlay(new TextLayer().fontStyle("italic"))).generate("test");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOverlayError2() {
        // Must supply public_id for for non-text underlay
        cloudinary.url().transformation(new Transformation().underlay(new Layer().resourceType("video"))).generate("test");
    }

    @Test
    public void testResourceType() throws Exception {

    }

    @Test
    public void testType() throws Exception {

    }

    @Test
    public void testPublicId() throws Exception {

    }

    @Test
    public void testFormat() throws Exception {

    }

    @Test
    public void testToString() throws Exception {

    }

    @Test
    public void testFormattedPublicId() throws Exception {

    }
}