package com.cloudinary.test;

import com.cloudinary.Api;
import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.api.ApiResponse;
import com.cloudinary.api.exceptions.AlreadyExists;
import com.cloudinary.utils.ObjectUtils;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.*;
import org.junit.rules.TestName;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.junit.Assume.assumeNotNull;

abstract public class AbstractStreamingProfilesApiTest extends MockableTest {
    private static final String PROFILE_NAME = "api_test_streaming_profile" + SUFFIX;
    protected Api api;
    private static final List<String> PREDEFINED_PROFILES = Arrays.asList("4k", "full_hd", "hd", "sd", "full_hd_wifi", "full_hd_lean", "hd_lean");
    public static final String UPDATE_PROFILE_NAME = PROFILE_NAME + "_update";
    public static final String DELETE_PROFILE_NAME = PROFILE_NAME + "_delete";
    public static final String CREATE_PROFILE_NAME = PROFILE_NAME + "_create";

    @BeforeClass
    public static void setUpClass() throws IOException {
        Cloudinary cloudinary = new Cloudinary();
        if (cloudinary.config.apiSecret == null) {
            System.err.println("Please setup environment for Upload test to run");
        }
    }

    @Rule
    public TestName currentTest = new TestName();

    @Before
    public void setUp() {
        System.out.println("Running " + this.getClass().getName() + "." + currentTest.getMethodName());
        this.cloudinary = new Cloudinary();
        assumeNotNull(cloudinary.config.apiSecret);
        this.api = cloudinary.api();
    }

    @Test
    public void testCreate() throws Exception {
        ApiResponse result = api.createStreamingProfile(CREATE_PROFILE_NAME, null, Collections.singletonList(ObjectUtils.asMap(
                "transformation", new Transformation().crop("limit").width(1200).height(1200).bitRate("5m")
        )), ObjectUtils.emptyMap());

        assertTrue(result.containsKey("data"));
        Map profile = (Map) result.get("data");
        assertThat(profile, (Matcher) hasEntry("name", (Object) CREATE_PROFILE_NAME));
    }

    @Test
    public void testGet() throws Exception {
        ApiResponse result = api.getStreamingProfile(PREDEFINED_PROFILES.get(0));
        assertTrue(result.containsKey("data"));
        Map profile = (Map) result.get("data");
        assertThat(profile, (Matcher) hasEntry("name", (Object) (PREDEFINED_PROFILES.get(0))));

    }

    @Test
    public void testList() throws Exception {
        ApiResponse result = api.listStreamingProfiles();
        assertTrue(result.containsKey("data"));
        List profiles = (List) result.get("data");
        // check that the list contains all predefined profiles
        for (String p :
                PREDEFINED_PROFILES) {
            assertThat(profiles, (Matcher) hasItem(hasEntry("name", p)));
        }
    }

    @Test
    public void testDelete() throws Exception {
        ApiResponse result;
        try {
            api.createStreamingProfile(DELETE_PROFILE_NAME, null, Collections.singletonList(ObjectUtils.asMap(
                    "transformation", new Transformation().crop("limit").width(1200).height(1200).bitRate("5m")
            )), ObjectUtils.emptyMap());
        } catch (AlreadyExists ignored) {
        }

        result = api.deleteStreamingProfile(DELETE_PROFILE_NAME);
        assertEquals("deleted", result.get("message"));
    }

    @Test
    public void testUpdate() throws Exception {
        try {
            api.createStreamingProfile(UPDATE_PROFILE_NAME, null, Collections.singletonList(ObjectUtils.asMap(
                    "transformation", new Transformation().crop("limit").width(1200).height(1200).bitRate("5m")
            )), ObjectUtils.emptyMap());
        } catch (AlreadyExists ignored) {
        }
        Map result = api.updateStreamingProfile(UPDATE_PROFILE_NAME, null, Collections.singletonList(
                ObjectUtils.asMap("transformation",
                        new Transformation().crop("limit").width(800).height(800).bitRate("5m")
                )), ObjectUtils.emptyMap());

        assertTrue(result.containsKey("data"));
        assertThat(result, (Matcher) hasEntry("message", (Object) "updated"));
        Map profile = (Map) result.get("data");
        assertThat(profile, (Matcher) hasEntry("name", (Object) UPDATE_PROFILE_NAME));
        assertThat(profile, Matchers.hasEntry(equalTo("representations"), (Matcher) hasItem(hasKey("transformation"))));
        final Map representation = (Map) ((List) profile.get("representations")).get(0);
        Map transformation = (Map) ((List)representation.get("transformation")).get(0);
        assertThat(transformation, allOf(
                (Matcher) hasEntry("width", 800),
                (Matcher) hasEntry("height", 800),
                (Matcher) hasEntry("crop", "limit"),
                (Matcher) hasEntry("bit_rate", "5m")
        ));
    }

    @AfterClass
    public static void tearDownClass() {
        Api api = new Cloudinary().api();
        try {
            api.deleteStreamingProfile(CREATE_PROFILE_NAME);
            api.deleteStreamingProfile(DELETE_PROFILE_NAME);
            api.deleteStreamingProfile(UPDATE_PROFILE_NAME);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
