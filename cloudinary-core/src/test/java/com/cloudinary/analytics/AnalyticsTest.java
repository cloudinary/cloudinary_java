package com.cloudinary.analytics;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.Analytics;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

public class AnalyticsTest {

    private Cloudinary cloudinary;

    @Rule
    public TestName currentTest = new TestName();

    @Before
    public void setUp() {
        System.out.println("Running " + this.getClass().getName() + "." + currentTest.getMethodName());
        this.cloudinary = new Cloudinary("cloudinary://a:b@test123?load_strategies=false");
    }

    @Test
    public void testToQueryParam() {
        Analytics analytics = new Analytics("F", "2.0.0", "1.8.0");
        String result = analytics.toQueryParam();
        Assert.assertEquals(result, "_a=AFAACMh0");
    }

    @Test
    public void testUrlWithAnalytics() {
        cloudinary.config.analytics = true;
        cloudinary.setAnalytics(new Analytics("F", "2.0.0", "1.8.0"));
        String url = cloudinary.url().generate("test");
        Assert.assertEquals(url, "http://res.cloudinary.com/test123/image/upload/test?_a=AFAACMh0");
        cloudinary.config.analytics = false;
        cloudinary.analytics = null;
    }

    @Test
    public void testUrlWithNoAnalytics() {
        String url = cloudinary.url().generate("test");
        Assert.assertEquals(url, "http://res.cloudinary.com/test123/image/upload/test");
        cloudinary.config.analytics = false;
        cloudinary.analytics = null;
    }

    @Test
    public void testUrlWithNoAnalyticsDefined() {
        cloudinary.config.analytics = false;
        String url = cloudinary.url().generate("test");
        Assert.assertEquals(url, "http://res.cloudinary.com/test123/image/upload/test");
        cloudinary.config.analytics = false;
        cloudinary.analytics = null;
    }

    @Test
    public void testUrlWithNoAnalyticsNull() {
        cloudinary.analytics = null;
        String url = cloudinary.url().generate("test");
        Assert.assertEquals(url, "http://res.cloudinary.com/test123/image/upload/test");
        cloudinary.config.analytics = false;
        cloudinary.analytics = null;
    }

    @Test
    public void testUrlWithNoAnalyticsNullAndTrue() {
        cloudinary.config.analytics = true;
        cloudinary.analytics = null;
        String url = cloudinary.url().generate("test");
        Assert.assertEquals(url, "http://res.cloudinary.com/test123/image/upload/test");
        cloudinary.config.analytics = false;
        cloudinary.analytics = null;
    }

    @Test
    public void testMiscAnalyticsObject() {
        cloudinary.config.analytics = true;
        Analytics analytics = new Analytics("Z", "1.24.0", "12.0.0");
        String result = analytics.toQueryParam();
        Assert.assertEquals(result, "_a=AZAlhAM0");
    }

    @Test
    public void testErrorAnalytics() {
        cloudinary.config.analytics = true;
        Analytics analytics = new Analytics("Z", "1.24.0", "0");
        String result = analytics.toQueryParam();
        Assert.assertEquals(result, "_a=E");
    }

}