package com.cloudinary;

import com.cloudinary.utils.ObjectUtils;
import com.cloudinary.utils.StringUtils;
import org.cloudinary.json.JSONObject;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import static org.junit.Assert.*;

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
    public void testMergeToSingleUnderscore() {
        assertEquals("a_b_c_d", StringUtils.mergeToSingleUnderscore("a_b_c_d"));
        assertEquals("a_b_c_d", StringUtils.mergeToSingleUnderscore("a_b_c_    d"));
        assertEquals("a_b_c_d", StringUtils.mergeToSingleUnderscore("a_b_c_  _  d"));
        assertEquals("_a_b_c_d_", StringUtils.mergeToSingleUnderscore("___ _ a____   b_c_    d   _  _"));
        assertEquals("a", StringUtils.mergeToSingleUnderscore("a"));
        assertEquals("a_", StringUtils.mergeToSingleUnderscore("a___________"));
        assertEquals("_a", StringUtils.mergeToSingleUnderscore("    a"));
    }

    @Test
    public void testIsVariable(){
        assertTrue(StringUtils.isVariable("$a6"));
        assertTrue(StringUtils.isVariable("$a64534534"));
        assertTrue(StringUtils.isVariable("$ab"));
        assertTrue(StringUtils.isVariable("$asdasda"));
        assertTrue(StringUtils.isVariable("$a34asd12e"));

        assertFalse(StringUtils.isVariable("$a"));
        assertFalse(StringUtils.isVariable("sda"));
        assertFalse(StringUtils.isVariable("   "));
        assertFalse(StringUtils.isVariable("... . /"));
        assertFalse(StringUtils.isVariable("$"));
        assertFalse(StringUtils.isVariable("$4"));
        assertFalse(StringUtils.isVariable("$4dfds"));
        assertFalse(StringUtils.isVariable("$612s"));
        assertFalse(StringUtils.isVariable("$6 12s"));
        assertFalse(StringUtils.isVariable("$6 1.2s"));
    }

    @Test
    public void testReplaceIfFirstChar(){
        assertEquals("abcdef", StringUtils.replaceIfFirstChar("abcdef", 'b', "*"));
        assertEquals("abcdef", StringUtils.replaceIfFirstChar("abcdef", 'f', "*"));
        assertEquals("abcdef", StringUtils.replaceIfFirstChar("abcdef", 'z', "*"));
        assertEquals("abcdef", StringUtils.replaceIfFirstChar("abcdef", '4', "*"));
        assertEquals("abcdef", StringUtils.replaceIfFirstChar("abcdef", '$', "*"));
        assertEquals("abc#def", StringUtils.replaceIfFirstChar("abc#def", 'b', "*"));
        assertEquals("$%^bcdef", StringUtils.replaceIfFirstChar("$%^bcdef", 'b', "*"));

        assertEquals("*bcdef", StringUtils.replaceIfFirstChar("abcdef", 'a', "*"));
        assertEquals("***bcdef", StringUtils.replaceIfFirstChar("abcdef", 'a', "***"));
        assertEquals("aaabcdef", StringUtils.replaceIfFirstChar("abcdef", 'a', "aaa"));
        assertEquals("---%^bcdef", StringUtils.replaceIfFirstChar("$%^bcdef", '$', "---"));

    }

    @Test
    public void testIsHttpUrl(){
        assertTrue(StringUtils.isHttpUrl("http://earsadasdsad"));
        assertTrue(StringUtils.isHttpUrl("https://earsadasdsad"));
        assertTrue(StringUtils.isHttpUrl("http://"));
        assertTrue(StringUtils.isHttpUrl("https://"));

        assertFalse(StringUtils.isHttpUrl("dafadfasd"));
        assertFalse(StringUtils.isHttpUrl("dafadfasd#$@"));
        assertFalse(StringUtils.isHttpUrl("htt://"));
    }

    @Test
    public void testMergeSlashes(){
        assertEquals("a/b/c/d/e", StringUtils.mergeSlashesInUrl("a////b///c//d/e"));
        assertEquals("abcd",StringUtils.mergeSlashesInUrl( "abcd"));
        assertEquals("ab/cd",StringUtils.mergeSlashesInUrl( "ab/cd"));
        assertEquals("/abcd",StringUtils.mergeSlashesInUrl( "/////abcd"));
        assertEquals("/abcd/",StringUtils.mergeSlashesInUrl( "////abcd///"));
        assertEquals("/abcd/",StringUtils.mergeSlashesInUrl( "/abcd/"));
    }

    @Test
    public void testHasVersionString(){
        assertTrue(StringUtils.hasVersionString("wqeasdlv31423423"));
        assertTrue(StringUtils.hasVersionString("v1"));
        assertTrue(StringUtils.hasVersionString("v1fdasfasd"));
        assertTrue(StringUtils.hasVersionString("asdasv1fdasfasd"));
        assertTrue(StringUtils.hasVersionString("12v1fdasfasd"));

        assertFalse(StringUtils.hasVersionString("121fdasfasd"));
        assertFalse(StringUtils.hasVersionString(""));
        assertFalse(StringUtils.hasVersionString("vvv"));
        assertFalse(StringUtils.hasVersionString("v"));
        assertFalse(StringUtils.hasVersionString("asdvvv"));
    }

    @Test
    public void testRemoveStartingChars(){
        assertEquals("abcde", StringUtils.removeStartingChars("abcde", 'b'));
        assertEquals("bcde", StringUtils.removeStartingChars("abcde", 'a'));
        assertEquals("bcde", StringUtils.removeStartingChars("aaaaaabcde", 'a'));
        assertEquals("bcdeaa", StringUtils.removeStartingChars("aaaaaabcdeaa", 'a'));
    }
}