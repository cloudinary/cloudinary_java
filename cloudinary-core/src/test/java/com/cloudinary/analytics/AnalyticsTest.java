package com.cloudinary.analytics;

import com.cloudinary.AuthToken;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.Analytics;
import org.junit.*;
import org.junit.rules.TestName;

import static org.junit.Assert.assertEquals;

public class AnalyticsTest {

    public static final String KEY = "00112233FF99";

    private Cloudinary cloudinary;

    @Rule
    public TestName currentTest = new TestName();

    @Before
    public void setUp() {
        System.out.println("Running " + this.getClass().getName() + "." + currentTest.getMethodName());
        this.cloudinary = new Cloudinary("cloudinary://a:b@test123?load_strategies=false");
    }

    @Test
    public void testEncodeVersion() {
        Analytics analytics = new Analytics();
        analytics.setSDKSemver("1.24.0");
        analytics.setTechVersion("12.0.0");
        String result = analytics.toQueryParam();
        Assert.assertEquals(result, "_a=CAGAlhAMZGd0");

        analytics.setSDKSemver("12.0");
        result = analytics.toQueryParam();
        Assert.assertEquals(result, "_a=CAGAMAMZGd0");

        analytics.setSDKSemver("43.21.26");
        result = analytics.toQueryParam();
        Assert.assertEquals(result, "_a=CAG///AMZGd0");

        analytics.setSDKSemver("0.0.0");
        result = analytics.toQueryParam();
        Assert.assertEquals(result, "_a=CAGAAAAMZGd0");

        analytics.setSDKSemver("43.21.27");
        result = analytics.toQueryParam();
        Assert.assertEquals(result, "_a=E");

    }

    @Test
    public void testToQueryParam() {
        Analytics analytics = new Analytics("F", "2.0.0", "1.8.0", "Z", "1.34.0");
        String result = analytics.toQueryParam();
        Assert.assertEquals(result, "_a=CAFAACMhZGd0");
    }

    @Test
    public void testUrlWithAnalytics() {
        cloudinary.config.analytics = true;
        cloudinary.setAnalytics(new Analytics("F", "2.0.0", "1.8.0", "Z", "1.34.0"));
        String url = cloudinary.url().generate("test");
        Assert.assertEquals(url, "http://res.cloudinary.com/test123/image/upload/test?_a=CAFAACMhZGd0");
    }

    @Test
    public void testUrlWithNoAnalytics() {
        String url = cloudinary.url().generate("test");
        Assert.assertEquals(url, "http://res.cloudinary.com/test123/image/upload/test");
    }

    @Test
    public void testUrlWithNoAnalyticsDefined() {
        cloudinary.config.analytics = false;
        String url = cloudinary.url().generate("test");
        Assert.assertEquals(url, "http://res.cloudinary.com/test123/image/upload/test");
    }

    @Test
    public void testUrlWithNoAnalyticsNull() {
        String url = cloudinary.url().generate("test");
        Assert.assertEquals(url, "http://res.cloudinary.com/test123/image/upload/test");
    }

    @Test
    public void testUrlWithNoAnalyticsNullAndTrue() {
        cloudinary.config.analytics = true;
        cloudinary.analytics.setSDKSemver("1.30.0");
        cloudinary.analytics.setTechVersion("12.0.0");
        String url = cloudinary.url().generate("test");
        Assert.assertEquals(url, "http://res.cloudinary.com/test123/image/upload/test?_a=CAGAu5AMZGd0");
    }

    @Test
    public void testMiscAnalyticsObject() {
        cloudinary.config.analytics = true;
        Analytics analytics = new Analytics("Z", "1.24.0", "12.0.0", "Z", "1.34.0");
        String result = analytics.toQueryParam();
        Assert.assertEquals(result, "_a=CAZAlhAMZGd0");
    }

    @Test
    public void testErrorAnalytics() {
        cloudinary.config.analytics = true;
        Analytics analytics = new Analytics("Z", "1.24.0", "0", "Z", "1.34.0");
        String result = analytics.toQueryParam();
        Assert.assertEquals(result, "_a=E");
    }

    @Test
    public void testUrlNoAnalyticsWithQueryParams() {
        final AuthToken authToken = new AuthToken(KEY).duration(300);
        authToken.startTime(11111111); // start time is set for test purposes
        cloudinary.config.authToken = authToken;
        cloudinary.config.cloudName = "test123";

        cloudinary.config.analytics = true;
        cloudinary.setAnalytics(new Analytics("F", "2.0.0", System.getProperty("java.version"), "Z", System.getProperty("os.version")));
        cloudinary.config.privateCdn = true;
        String url = cloudinary.url().signed(true).type("authenticated").generate("test");
        assertEquals(url,"http://test123-res.cloudinary.com/image/authenticated/test?__cld_token__=st=11111111~exp=11111411~hmac=735a49389a72ac0b90d1a84ac5d43facd1a9047f153b39e914747ef6ed195e53");
        cloudinary.config.privateCdn = false;
    }

    @After
    public void tearDown() {
        cloudinary.config.analytics = false;
        cloudinary.analytics = null;
    }

}
