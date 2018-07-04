package com.cloudinary.test;

import com.cloudinary.Api;
import com.cloudinary.Cloudinary;
import com.cloudinary.Url;
import com.cloudinary.cache.ResponsiveBreakpointPayload;
import com.cloudinary.utils.ObjectUtils;
import org.junit.*;
import org.junit.rules.TestName;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import static com.cloudinary.utils.ObjectUtils.asMap;
import static org.junit.Assume.assumeNotNull;

public abstract class AbstractGetTest extends MockableTest {
    private static final String GET_TAGS = SDK_TEST_TAG + "_getter";

    @Rule
    public TestName currentTest = new TestName();

    @BeforeClass
    public static void setUpClass() throws IOException {
        Cloudinary cloudinary = new Cloudinary();
        if (cloudinary.config.apiSecret == null) {
            System.err.println("Please setup environment for Upload test to run");
            return;
        }

        cloudinary.uploader().upload(SRC_TEST_IMAGE, asMap("tags", new String[]{SDK_TEST_TAG, GET_TAGS}));
    }

    @AfterClass
    public static void tearDownClass() {
        Api api = new Cloudinary().api();
        try {
            api.deleteResourcesByTag(GET_TAGS, ObjectUtils.emptyMap());
        } catch (Exception ignored) {
        }
        try {
            api.deleteResourcesByTag(GET_TAGS, ObjectUtils.asMap("resource_type", "video"));
        } catch (Exception ignored) {
        }
        try {
            api.deleteResourcesByTag(GET_TAGS, ObjectUtils.asMap("resource_type", "raw"));
        } catch (Exception ignored) {
        }
    }

    @Before
    public void setUp() {
        System.out.println("Running " + this.getClass().getName() + "." + currentTest.getMethodName());
        this.cloudinary = new Cloudinary();
        assumeNotNull(cloudinary.config.apiSecret);
    }

    @Test
    public void testGetBreakpoints() throws Exception {

        Map result = cloudinary.uploader().upload(SRC_TEST_IMAGE, asMap("tags", Arrays.asList(SDK_TEST_TAG, GET_TAGS)));
        String publicId = result.get("public_id").toString();
        String type = result.get("type").toString();
        String resourceType = result.get("resource_type").toString();
        String format = result.get("format").toString();

        Url url = cloudinary.url();
        url.type(type).resourceType(resourceType).format(format).publicId(publicId).transformation().rawTransformation("e_blur");
        ResponsiveBreakpointPayload payload = cloudinary.get().getBreakpoints(url, 100, 1000, 100, 12);
        Assert.assertNotNull(payload);
    }
}
