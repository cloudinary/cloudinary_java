package com.cloudinary.test;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.cloudinary.utils.StringUtils;

import static org.junit.Assume.assumeTrue;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class MockableTest {

    public static final String HEBREW_PDF = "../cloudinary-test-common/src/main/resources/אבג.docx";
    public static final String ENGLISH_PDF = "../cloudinary-test-common/src/main/resources/abc.docx";
    public static final String SRC_TEST_IMAGE = "../cloudinary-test-common/src/main/resources/old_logo.png";
    public static final String SRC_TEST_VIDEO = "http://res.cloudinary.com/demo/video/upload/dog.mp4";
    public static final String SRC_TEST_RAW = "../cloudinary-test-common/src/main/resources/docx.docx";
    public static final String REMOTE_TEST_IMAGE = "http://cloudinary.com/images/old_logo.png";
    protected static String SUFFIX = StringUtils.isNotBlank(System.getenv("TRAVIS_JOB_ID")) ? System.getenv("TRAVIS_JOB_ID") : String.valueOf(new Random().nextInt(99999));
    protected static final String SDK_TEST_TAG = "cloudinary_java_test_" + SUFFIX;
    protected Cloudinary cloudinary;

    protected Object getParam(String name){
        throw new UnsupportedOperationException();
    }
    protected String getURL(){
        throw new UnsupportedOperationException();
    }
    protected String getHttpMethod(){
        throw new UnsupportedOperationException();
    }

    protected Map preloadResource(Map options) throws IOException {
        if (!options.containsKey("tags")){
            throw new IllegalArgumentException("Must provide unique per-class tags");
        }
        Map combinedOptions = ObjectUtils.asMap("transformation", "c_scale,w_100");
        combinedOptions.putAll(options);
        return cloudinary.uploader().upload("http://res.cloudinary.com/demo/image/upload/sample", combinedOptions);
    }

    private static final List<String> enabledAddons = getEnabledAddons();

    protected void assumeAddonEnabled(String addon) throws Exception {
        boolean enabled = enabledAddons.contains(addon.toLowerCase()) 
            || (enabledAddons.size() == 1 && enabledAddons.get(0).equalsIgnoreCase("all"));

        assumeTrue(String.format("Use CLD_TEST_ADDONS environment variable to enable tests for %s.", addon), enabled);
    }

    private static List<String> getEnabledAddons() {
        String envAddons = System.getenv()
            .getOrDefault("CLD_TEST_ADDONS", "")
            .toLowerCase()
            .replaceAll("\\s", "");

        return Arrays.asList(envAddons.split(","));
    }
}
