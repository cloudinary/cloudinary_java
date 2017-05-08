package com.cloudinary.test;

import com.cloudinary.Api;
import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.Uploader;
import com.cloudinary.utils.ObjectUtils;
import org.junit.*;
import org.junit.rules.TestName;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.cloudinary.utils.ObjectUtils.asMap;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assume.assumeNotNull;

@SuppressWarnings({"rawtypes", "unchecked"})
abstract public class AbstractContextTest extends MockableTest {

    private static final String CONTEXT_TAG = "context_tag_" + String.valueOf(System.currentTimeMillis()) + SUFFIX;
    private static Map resource;
    public static final Map CONTEXT = asMap("caption", "some cäption", "alt", "alternativè");
    private Uploader uploader;

    @BeforeClass
    public static void setUpClass() throws Exception {
        Cloudinary cloudinary = new Cloudinary();
        if (cloudinary.config.apiSecret == null) {
            System.err.println("Please setup environment for Upload test to run");
        }

        resource = cloudinary.uploader().upload(SRC_TEST_IMAGE,
                asMap(  "tags", new String[]{SDK_TEST_TAG, CONTEXT_TAG},
                        "context", CONTEXT,
                        "transformation", new Transformation().crop("scale").width(10)));
        final String publicId = (String) resource.get("public_id");
        resource = cloudinary.api().resource(publicId, asMap("context", true));
        assertEquals(asMap("custom", CONTEXT), resource.get("context"));

    }

    @AfterClass
    public static void tearDownClass() {
        Api api = MockableTest.cleanUp();
        Cloudinary cloudinary = new Cloudinary();
        try {
            cloudinary.api().deleteResourcesByTag(CONTEXT_TAG, ObjectUtils.emptyMap());
        } catch (Exception ignored) {
        }
    }

    @Rule
    public TestName currentTest = new TestName();

    @Before
    public void setUp() throws Exception {
        System.out.println("Running " + this.getClass().getName() + "." + currentTest.getMethodName());
        cloudinary = new Cloudinary();
        uploader = cloudinary.uploader();
        assumeNotNull(cloudinary.config.apiSecret);

    }

    @Test
    public void testExplicit() throws Exception {
        //should allow sending context

        Map differentContext = asMap("caption", "different = caption", "alt2", "alt|alternative alternative");
        Map result = uploader.explicit(publicId(), asMap("type", "upload", "context", differentContext));
        assertEquals("explicit API should return the new context", asMap("custom", differentContext), result.get("context"));
        resource = cloudinary.api().resource(publicId(), asMap("context", true));
        assertEquals("explicit API should replace the context", asMap("custom", differentContext), resource.get("context"));
    }

    @Test
    public void testAddContext() throws Exception {
        Map context = new HashMap((Map)((Map)resource.get("context")).get("custom"));
        context.put("caption", "new caption");
        Map result = uploader.addContext(asMap("caption", "new caption"), new String[]{publicId(), "no-such-id"}, null);
        assertThat("addContext should return a list of modified public IDs", (List<String>) result.get("public_ids"), contains(publicId()));

        resource = cloudinary.api().resource(publicId(), asMap("context", true));
        assertEquals(asMap("custom", context), resource.get("context"));
    }

    @Test
    public void testRemoveAllContext() throws Exception {
        Map result = uploader.removeAllContext(new String[]{publicId(), "no-such-id"}, null);
        assertThat((List<String>) result.get("public_ids"), contains(publicId()));

        resource = cloudinary.api().resource(publicId(), asMap("context", true));
        assertThat((Map<? extends String, ?>)resource, not(hasKey("context")));
    }

    private String publicId(){
        return (String) resource.get("public_id");
    }
}
