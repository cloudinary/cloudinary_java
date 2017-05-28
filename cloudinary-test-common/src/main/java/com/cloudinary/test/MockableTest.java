package com.cloudinary.test;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.cloudinary.utils.StringUtils;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.IOException;
import java.util.Map;
import java.util.Random;

public class MockableTest {

    public static final String SRC_TEST_IMAGE = "../cloudinary-test-common/src/main/resources/old_logo.png";
    public static final String REMOTE_TEST_IMAGE = "http://cloudinary.com/images/old_logo.png";
    protected static String SUFFIX = StringUtils.isNotBlank(System.getenv("TRAVIS_JOB_ID")) ? System.getenv("TRAVIS_JOB_ID") : String.valueOf(new Random().nextInt(99999));
    protected static final String SDK_TEST_TAG = "cloudinary_java_test_" + SUFFIX;
    protected Cloudinary cloudinary;

    protected Object getParam(String name){
        throw new NotImplementedException();
    }
    protected String getURL(){
        throw new NotImplementedException();
    }
    protected String getHttpMethod(){
        throw new NotImplementedException();
    }

    protected Map preloadResource(Map options) throws IOException {
        if (!options.containsKey("tags")){
            throw new IllegalArgumentException("Must provide unique per-class tags");
        }
        Map combinedOptions = ObjectUtils.asMap("transformation", "c_scale,w_100");
        combinedOptions.putAll(options);
        return cloudinary.uploader().upload("http://res.cloudinary.com/demo/image/upload/sample", combinedOptions);
    }
}
