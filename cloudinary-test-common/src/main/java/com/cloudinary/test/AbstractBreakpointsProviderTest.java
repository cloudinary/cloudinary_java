package com.cloudinary.test;

import com.cloudinary.*;
import com.cloudinary.cache.KeyValueResponsiveBreakpointsCacheAdapter;
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
    private KeyValueResponsiveBreakpointsCacheAdapter cacheAdapter;

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
        String cloudinaryUrl = System.getProperty("CLOUDINARY_URL", System.getenv("CLOUDINARY_URL"));
        final Configuration config;
        if (cloudinaryUrl != null) {
            config = Configuration.from(cloudinaryUrl);
        } else {
            config = new Configuration();
        }

        this.cacheAdapter = new KeyValueResponsiveBreakpointsCacheAdapter(new MemoryKeyValueStore());
        config.useResponsiveBreakpointsCache = true;
        config.responsiveBreakpointsCacheAdapter = this.cacheAdapter;
        this.cloudinary = new Cloudinary(config.asMap());
        assumeNotNull(cloudinary.config.apiSecret);
    }

    /**
     * This test verifies that tag creation triggers a breakpoint fetch and caches them correctly
     *
     * @throws Exception
     */
    @Test
    public void testGetBreakpoints() throws Exception {
        // Upload a photo to work with
        Map result = cloudinary.uploader().upload(SRC_TEST_IMAGE, asMap("tags", Arrays.asList(SDK_TEST_TAG, BP_TAGS)));
        String publicId = result.get("public_id").toString();
        String type = result.get("type").toString();
        String resourceType = result.get("resource_type").toString();
        String format = result.get("format").toString();

        Url url = cloudinary.url();
        url.type(type).resourceType(resourceType).format(format).publicId(publicId).transformation().rawTransformation("e_blur");
        Url clone = url.clone();

        // generate tag - this should trigger a fetch the breakpoints and caching of the breakpoints.
        url.imageTag(publicId, new TagOptions().srcset(new Srcset(100, 1000, 30, 20 * 1024)));

        // fetch breakpoints directly (bypassing cache):
        ResponsiveBreakpointPayload payload = cloudinary.get().getBreakpoints(clone, 100, 1000, 20 * 1024, 30);

        // verify that the provider cached the result and that it is equal to the direct fetch
        ResponsiveBreakpointPayload cached = cacheAdapter.get(publicId, type, resourceType, "e_blur", format);
        Assert.assertNotNull(cached);

        Assert.assertTrue(Arrays.equals(payload.getBreakpoints(), cached.getBreakpoints()));
    }
}
