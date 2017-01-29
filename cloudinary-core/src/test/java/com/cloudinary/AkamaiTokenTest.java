package com.cloudinary;

import org.hamcrest.Matchers;
import org.junit.Test;

import java.util.Calendar;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

public class AkamaiTokenTest {
    public static final String KEY = "00112233FF99";

    @Test
    public void generateWithStartAndWindow() throws Exception {
        AkamaiToken t = new AkamaiToken(KEY);
        t.setStartTime(1111111111).setAcl("/image/*").setWindow(300);
        assertEquals("should generate an Akamai token with startTime and window", "__cld_token__=st=1111111111~exp=1111111411~acl=/image/*~hmac=0854e8b6b6a46471a80b2dc28c69bd352d977a67d031755cc6f3486c121b43af", t.generate());
    }

    @Test
    public void generateWithWindow() throws Exception {
        long firstExp = Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTimeInMillis() / 1000L + 300;
        Thread.sleep(1200);
        String token = new AkamaiToken(KEY).setAcl("*").setWindow(300).generate();
        Thread.sleep(1200);
        long secondExp = Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTimeInMillis() / 1000L + 300;
        Matcher m = Pattern.compile("exp=(\\d+)").matcher(token);
        assertTrue(m.find());
        final String expString = m.group(1);
        final long actual = Long.parseLong(expString);
        assertThat(actual, Matchers.greaterThanOrEqualTo(firstExp));
        assertThat(actual, Matchers.lessThanOrEqualTo(secondExp));
        assertEquals(token, new AkamaiToken(KEY).setAcl("*").setEndTime(actual).generate());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMustProvideEndTimeOrWindow(){
        new AkamaiToken(KEY).setAcl("*").generate();
    }


}