package com.cloudinary.test;

import com.cloudinary.Api;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.IOException;
import java.util.Map;
import java.util.Random;

public class MockableTest {

    public static final String SRC_TEST_IMAGE = "../cloudinary-test-common/src/main/resources/old_logo.png";
    public static final String REMOTE_TEST_IMAGE = "http://cloudinary.com/images/old_logo.png";
    protected static final int SUFFIX = new Random().nextInt(99999);
    protected static final String SDK_TEST_TAG = "cloudinary_java_test_" + SUFFIX;
    protected static final String uniqueTag = SDK_TEST_TAG + (new java.util.Date().getTime());
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

    protected Map preloadResource() throws IOException {
        return preloadResource(ObjectUtils.emptyMap());
    }

    protected Map preloadResource(Map options) throws IOException {
        Map combinedOptions = ObjectUtils.asMap(
                "tags", new String[]{SDK_TEST_TAG, uniqueTag},
                "transformation", "c_scale,w_100");
        combinedOptions.putAll(options);
        return cloudinary.uploader().upload("http://res.cloudinary.com/demo/image/upload/sample", combinedOptions);
    }

    public static Api cleanUp() {
        Cloudinary cloudinary = new Cloudinary();
        Api api = cloudinary.api();
        try {
            api.deleteResourcesByTag(uniqueTag, ObjectUtils.emptyMap());
        } catch (Exception ignored) {
        }
        return api;
    }
}
