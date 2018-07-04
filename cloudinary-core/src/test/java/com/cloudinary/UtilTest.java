package com.cloudinary;

import com.cloudinary.utils.ObjectUtils;
import com.cloudinary.utils.StringUtils;
import org.cloudinary.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by amir on 17/11/2016.
 */
public class UtilTest {

    public static final String START = "2019-02-22 16:20:57 +0200";
    public static final String END = "2019-03-22 00:00:00 +0200";
    public static final String START_REFORMATTED = "2019-02-22T14:20:57Z";
    public static final String END_REFORMATTED = "2019-03-21T22:00:00Z";

    @Test
    public void encodeContext() throws Exception {
        Map context = ObjectUtils.asMap("caption", "different = caption", "alt2", "alt|alternative");
        String result = Util.encodeContext(context);
        assertTrue("caption=different \\= caption|alt2=alt\\|alternative".equals(result) ||
                "alt2=alt\\|alternative|caption=different \\= caption".equals(result));
    }

    @Test
    public void testAccessControlRule() throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
        final Date start = simpleDateFormat.parse(START);
        final Date end = simpleDateFormat.parse(END);
        AccessControlRule acl = AccessControlRule.anonymous(start, end);

        JSONObject deserializedAcl = new JSONObject(acl.toString());
        assertEquals(deserializedAcl.get("access_type"), "anonymous");
        assertEquals(deserializedAcl.get("start"), START_REFORMATTED);
        assertEquals(deserializedAcl.get("end"), END_REFORMATTED);

        acl = AccessControlRule.anonymousFrom(start);
        assertEquals(2, acl.length());
        assertEquals(deserializedAcl.get("access_type"), "anonymous");
        assertEquals(deserializedAcl.get("start"), START_REFORMATTED);

        acl = AccessControlRule.anonymousUntil(end);
        assertEquals(2, acl.length());
        assertEquals(deserializedAcl.get("access_type"), "anonymous");
        assertEquals(deserializedAcl.get("end"), END_REFORMATTED);

        AccessControlRule token = AccessControlRule.token();
        assertEquals(1, token.length());
        assertEquals("{\"access_type\":\"token\"}", token.toString());

    }

    @Test
    public void testSha1() {
        String s = "dawe4k;l34!@#$12dsm;da;k5435";
        String sha1 = StringUtils.sha1(s);
        Assert.assertEquals("ef77c3fb252e01995bcb999550565146ce47f65f", sha1);
    }
}