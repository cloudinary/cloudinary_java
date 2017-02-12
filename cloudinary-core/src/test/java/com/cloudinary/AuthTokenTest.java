package com.cloudinary;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.util.Calendar;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

public class AuthTokenTest {
    public static final String KEY = "00112233FF99";
    public static final String ALT_KEY = "CCBB2233FF00";
    private Cloudinary cloudinary;

    @Rule
    public TestName currentTest = new TestName();

    @Before
    public void setUp() {
        System.out.println("Running " + this.getClass().getName() + "." + currentTest.getMethodName());
        this.cloudinary = new Cloudinary("cloudinary://a:b@test123?load_strategies=false");
        final AuthToken authToken = new AuthToken(KEY).duration(300);
        authToken.startTime(11111111); // start time is set for test purposes
        cloudinary.config.authToken = authToken;
        cloudinary.config.cloudName = "test123";

    }

    @Test
    public void generateWithStartAndWindow() throws Exception {
        AuthToken t = new AuthToken(KEY);
        t.startTime(1111111111).acl("/image/*").duration(300);
        assertEquals("should generate an Akamai token with startTime and duration", "__cld_token__=st=1111111111~exp=1111111411~acl=/image/*~hmac=0854e8b6b6a46471a80b2dc28c69bd352d977a67d031755cc6f3486c121b43af", t.generate());
    }

    @Test
    public void generateWithWindow() throws Exception {
        long firstExp = Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTimeInMillis() / 1000L + 300;
        Thread.sleep(1200);
        String token = new AuthToken(KEY).acl("*").duration(300).generate();
        Thread.sleep(1200);
        long secondExp = Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTimeInMillis() / 1000L + 300;
        Matcher m = Pattern.compile("exp=(\\d+)").matcher(token);
        assertTrue(m.find());
        final String expString = m.group(1);
        final long actual = Long.parseLong(expString);
        assertThat(actual, Matchers.greaterThanOrEqualTo(firstExp));
        assertThat(actual, Matchers.lessThanOrEqualTo(secondExp));
        assertEquals(token, new AuthToken(KEY).acl("*").expiration(actual).generate());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMustProvideEndTimeOrWindow(){
        new AuthToken(KEY).acl("*").generate();
    }

    @Test
    public void testAuthenticatedUrl() {
        cloudinary.config.privateCdn = true;

        String message = "should add token if authToken is globally set and signed = true";
        String url = cloudinary.url().signed(true).resourceType("image").type("authenticated").version("1486020273").generate("sample.jpg");
        assertEquals(message,"http://test123-res.cloudinary.com/image/authenticated/v1486020273/sample.jpg?__cld_token__=st=11111111~exp=11111411~hmac=8db0d753ee7bbb9e2eaf8698ca3797436ba4c20e31f44527e43b6a6e995cfdb3", url);

        message = "should add token for 'public' resource";
        url = cloudinary.url().signed(true).resourceType("image").type("public").version("1486020273").generate("sample.jpg");
        assertEquals(message,"http://test123-res.cloudinary.com/image/public/v1486020273/sample.jpg?__cld_token__=st=11111111~exp=11111411~hmac=c2b77d9f81be6d89b5d0ebc67b671557e88a40bcf03dd4a6997ff4b994ceb80e", url);

        message = "should not add token if signed is false";
        url = cloudinary.url().resourceType("image").type("authenticated").version("1486020273").generate("sample.jpg");
        assertEquals(message,"http://test123-res.cloudinary.com/image/authenticated/v1486020273/sample.jpg", url);

        message = "should not add token if authToken is globally set but null auth token is explicitly set and signed = true";
        url = cloudinary.url().authToken(AuthToken.NULL_AUTH_TOKEN).signed(true).resourceType("image").type("authenticated").version("1486020273").generate("sample.jpg");
        assertEquals(message,"http://test123-res.cloudinary.com/image/authenticated/v1486020273/sample.jpg", url);

        message = "explicit authToken should override global setting";
        url = cloudinary.url().signed(true).authToken(new AuthToken(ALT_KEY).startTime(222222222).duration(100)).resourceType("image").type("authenticated").transformation(new Transformation().crop("scale").width(300)).generate("sample.jpg");
        assertEquals(message,"http://test123-res.cloudinary.com/image/authenticated/c_scale,w_300/sample.jpg?__cld_token__=st=222222222~exp=222222322~hmac=7d276841d70c4ecbd0708275cd6a82e1f08e47838fbb0bceb2538e06ddfa3029", url);

        message = "should compute expiration as start time + duration";
        AuthToken token = new AuthToken(KEY).startTime(11111111).duration(300);
        url = cloudinary.url().signed(true).authToken(token).resourceType("image").type("authenticated").version("1486020273").generate("sample.jpg");
        assertEquals(message,"http://test123-res.cloudinary.com/image/authenticated/v1486020273/sample.jpg?__cld_token__=st=11111111~exp=11111411~hmac=8db0d753ee7bbb9e2eaf8698ca3797436ba4c20e31f44527e43b6a6e995cfdb3", url);

    }

    @Test
    public void testConfiguration() {
        cloudinary = new Cloudinary("cloudinary://a:b@test123?load_strategies=false&auth_token[key]=aabbcc112233&auth_token[duration]=200");

        assertEquals(cloudinary.config.authToken.key, "aabbcc112233");
        assertEquals(cloudinary.config.authToken.duration, 200);

    }

    @Test
    public void testTokenGeneration(){
        AuthToken token = new AuthToken(KEY);
        token.duration = 300;
        String user = "foobar"; // username taken from elsewhere
        token.acl = "/*/t_" + user;
        token.startTime(222222222); // we can't rely on the default "now" value in tests
        String cookieToken = token.generate();
        assertEquals("__cld_token__=st=222222222~exp=222222522~acl=/*/t_foobar~hmac=eb5e2266c8ec9573f696025f075b92998080347e1c12ac39a26c94d7d712704a", cookieToken);
    }

    @Test
    public void testUrlInTag() {
        String message = "should add token to an image tag url";
        String url = cloudinary.url().signed(true).resourceType("image").type("authenticated").version("1486020273").imageTag("sample.jpg");
        assertThat(url, Matchers.matchesPattern("<img.*src='http://res.cloudinary.com/test123/image/authenticated/v1486020273/sample.jpg\\?__cld_token__=st=11111111~exp=11111411~hmac=8db0d753ee7bbb9e2eaf8698ca3797436ba4c20e31f44527e43b6a6e995cfdb3.*>"));

    }
}