package com.cloudinary.test;

import com.cloudinary.Api;
import com.cloudinary.Cloudinary;
import com.cloudinary.ResponsiveBreakpointsProvider;
import com.cloudinary.Url;
import com.cloudinary.cache.CacheAdapter;
import com.cloudinary.cache.ResponsiveBreakpointPayload;
import com.cloudinary.utils.ObjectUtils;
import org.junit.*;
import org.junit.rules.TestName;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import static com.cloudinary.utils.ObjectUtils.asMap;
import static org.junit.Assume.assumeNotNull;

public abstract class AbstractBreakpointsProviderTest extends MockableTest {
    private static final String BP_TAGS = SDK_TEST_TAG + "_bp_provider";

    @Rule
    public TestName currentTest = new TestName();

    @BeforeClass
    public static void setUpClass() throws IOException {
        Cloudinary cloudinary = new Cloudinary();
        if (cloudinary.config.apiSecret == null) {
            System.err.println("Please setup environment for breakpoints test to run");
            return;
        }

        cloudinary.uploader().upload(SRC_TEST_IMAGE, asMap("tags", new String[]{SDK_TEST_TAG, BP_TAGS}));
    }

    @AfterClass
    public static void tearDownClass() {
        Api api = new Cloudinary().api();
        try {
            api.deleteResourcesByTag(BP_TAGS, ObjectUtils.emptyMap());
        } catch (Exception ignored) {
        }
        try {
            api.deleteResourcesByTag(BP_TAGS, ObjectUtils.asMap("resource_type", "video"));
        } catch (Exception ignored) {
        }
        try {
            api.deleteResourcesByTag(BP_TAGS, ObjectUtils.asMap("resource_type", "raw"));
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
        CacheAdapter<ResponsiveBreakpointPayload> adapter = new MockMemCache();

        ResponsiveBreakpointsProvider provider = new ResponsiveBreakpointsProvider(adapter, cloudinary);

        Map result = cloudinary.uploader().upload(SRC_TEST_IMAGE, asMap("tags", Arrays.asList(SDK_TEST_TAG, BP_TAGS)));
        String publicId = result.get("public_id").toString();
        String type = result.get("type").toString();
        String resourceType = result.get("resource_type").toString();
        String format = result.get("format").toString();

        ResponsiveBreakpointsProvider.CacheKey cacheKey = new ResponsiveBreakpointsProvider.CacheKey();
        cacheKey.transformation = "e_blur";
        cacheKey.type = type;
        cacheKey.resourceType = resourceType;
        cacheKey.format = format;
        cacheKey.publicId = publicId;

        Url url = cloudinary.url();
        url.type(type).resourceType(resourceType).format(format).publicId(publicId).transformation().rawTransformation(cacheKey.transformation);
        ResponsiveBreakpointPayload payload = provider.get(url, cacheKey, 100, 1000, 100, 12);

        ResponsiveBreakpointPayload cached = adapter.get(cacheKey.publicId, cacheKey.type, cacheKey.resourceType, cacheKey.transformation, cacheKey.format);
        Assert.assertNotNull(payload);
        Assert.assertTrue(Arrays.equals(payload.getBreakpoints(), cached.getBreakpoints()));
    }
}
