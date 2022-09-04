package com.cloudinary;

import com.cloudinary.utils.Analytics;
import com.cloudinary.utils.ObjectUtils;

import org.hamcrest.CoreMatchers;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.Collections;
import java.util.Map;
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
    public void generateWithStartAndDuration() throws Exception {
        AuthToken t = new AuthToken(KEY);
        t.startTime(1111111111).acl("/image/*").duration(300);
        assertEquals("should generate an authorization token with startTime and duration", "__cld_token__=st=1111111111~exp=1111111411~acl=%2fimage%2f*~hmac=1751370bcc6cfe9e03f30dd1a9722ba0f2cdca283fa3e6df3342a00a7528cc51", t.generate());
    }

    @Test
    public void generateWithDuration() throws Exception {
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
    public void testMustProvideExpirationOrDuration(){
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
        assertEquals(message,"http://test123-res.cloudinary.com/image/authenticated/s--v2fTPYTu--/v1486020273/sample.jpg", url);

        message = "explicit authToken should override global setting";
        url = cloudinary.url().signed(true).authToken(new AuthToken(ALT_KEY).startTime(222222222).duration(100)).resourceType("image").type("authenticated").transformation(new Transformation().crop("scale").width(300)).generate("sample.jpg");
        assertEquals(message,"http://test123-res.cloudinary.com/image/authenticated/c_scale,w_300/sample.jpg?__cld_token__=st=222222222~exp=222222322~hmac=55cfe516530461213fe3b3606014533b1eca8ff60aeab79d1bb84c9322eebc1f", url);

        message = "should compute expiration as start time + duration";
        url = cloudinary.url().signed(true).authToken(new AuthToken().startTime(11111111).duration(300))
                .type("authenticated").version("1486020273").generate("sample.jpg");
        assertEquals(message,"http://test123-res.cloudinary.com/image/authenticated/v1486020273/sample.jpg?__cld_token__=st=11111111~exp=11111411~hmac=8db0d753ee7bbb9e2eaf8698ca3797436ba4c20e31f44527e43b6a6e995cfdb3", url);

    }

    @Test
    public void testConfiguration() {
        cloudinary = new Cloudinary("cloudinary://a:b@test123?load_strategies=false&auth_token[key]=aabbcc112233&auth_token[duration]=200");
        assertEquals(cloudinary.config.authToken, new AuthToken("aabbcc112233").duration(200));
    }

    @Test
    public void testTokenGeneration(){
        AuthToken token = new AuthToken(KEY);
        token.duration(300);
        String user = "foobar"; // username taken from elsewhere
        token.acl("/*/t_" + user);
        token.startTime(222222222); // we can't rely on the default "now" value in tests
        String cookieToken = token.generate();
        assertEquals("__cld_token__=st=222222222~exp=222222522~acl=%2f*%2ft_foobar~hmac=8e39600cc18cec339b21fe2b05fcb64b98de373355f8ce732c35710d8b10259f", cookieToken);
    }

    @Test
    public void testUrlInTag() {
//        String message = "should add token to an image tag url";
//        String url = cloudinary.url().signed(true).resourceType("image").type("authenticated").version("1486020273").imageTag("sample.jpg");
//        assertThat(url, Matchers.matchesPattern("<img.*src='http://res.cloudinary.com/test123/image/authenticated/v1486020273/sample.jpg\\?__cld_token__=st=11111111~exp=11111411~hmac=9bd6f41e2a5893da8343dc8eb648de8bf73771993a6d1457d49851250caf3b80.*>"));

        String videoTag = cloudinary.url().transformation(new Transformation())
                .type("upload")
                .authToken(new AuthToken("123456").duration(300))
                .signed(true)
                .secure(true)
                .videoTag("sample", Cloudinary.asMap(
                        "controls", true,
                        "loop", true)
                );
        System.out.println(videoTag);

    }

    @Test
    public void testIgnoreUrlIfAclIsProvided() {
        String user = "foobar"; // username taken from elsewhere
        AuthToken token = new AuthToken(KEY).duration(300).acl("/*/t_" + user).startTime(222222222);
        String cookieToken = token.generate();
        AuthToken aclToken = new AuthToken(KEY).duration(300).acl("/*/t_" + user).startTime(222222222);
        String cookieAclToken = aclToken.generate("http://res.cloudinary.com/test123/image/upload/v1486020273/sample.jpg");
        assertEquals(cookieToken, cookieAclToken);
    }

    @Test
    public void testMultiplePatternsInAcl() {
        AuthToken token = new AuthToken(KEY).duration(3600).acl("/image/authenticated/*", "/image2/authenticated/*", "/image3/authenticated/*").startTime(22222222);
        String cookieToken = token.generate();
        Assert.assertThat(cookieToken, CoreMatchers.containsString("~acl=%2fimage%2fauthenticated%2f*!%2fimage2%2fauthenticated%2f*!%2fimage3%2fauthenticated%2f*~"));
    }

    @Test
    public void testPublicAclInitializationFromMap() {
        Map options = ObjectUtils.asMap(
                "acl", Collections.singleton("foo"),
                "expiration", 100,
                "key", KEY,
                "tokenName", "token");
        String token = new AuthToken(options).generate();
        assertEquals("token=exp=100~acl=foo~hmac=88be250f3a912add862959076ee74f392fa0959a953fddd9128787d5c849efd9", token);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMissingAclAndUrlShouldThrow() {
        String token = new AuthToken(KEY).duration(300).generate();
    }

    @Test
    public void testMissingUrlNotMissingAclShouldNotThrow() {
        String token = new AuthToken(KEY).duration(300).generate("http://res.cloudinary.com/test123");
    }


}
