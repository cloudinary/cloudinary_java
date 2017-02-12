package com.cloudinary;

import com.cloudinary.utils.StringUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Authentication Token generator
 */
public class AuthToken {
    public static final AuthToken NULL_AUTH_TOKEN = new AuthToken().setNull();
    public static final String AUTH_TOKEN_NAME = "__cld_token__";

    public String tokenName = AUTH_TOKEN_NAME;
    public String key;
    public long startTime;
    public long expiration;
    public String ip;
    public String acl;
    public long duration;
    private boolean isNullToken = false;

    public AuthToken() {
    }

    public AuthToken(String key) {
        this.key = key;
    }

    public AuthToken tokenName(String tokenName) {
        this.tokenName = tokenName;
        return this;
    }

    /**
     * Set the start time of the token. Defaults to now.
     *
     * @param startTime in seconds since epoch
     * @return
     */
    public AuthToken startTime(long startTime) {
        this.startTime = startTime;
        return this;
    }

    /**
     * Set the end time (expiration) of the token
     *
     * @param expiration in seconds since epoch
     * @return
     */
    public AuthToken expiration(long expiration) {
        this.expiration = expiration;
        return this;
    }

    public AuthToken ip(String ip) {
        this.ip = ip;
        return this;
    }

    public AuthToken acl(String acl) {
        this.acl = acl;
        return this;
    }

    /**
     * The duration of the token in seconds. This value is used to calculate the expiration of the token.
     * It is ignored if expiration is provided.
     *
     * @param duration in seconds
     * @return
     */
    public AuthToken duration(long duration) {
        this.duration = duration;
        return this;
    }

    /**
     * Generate the authentication token
     *
     * @return a signed token
     */
    public String generate() {
        return generate(null);
    }

    public String generate(String url) {
        long expiration = this.expiration;
        if (expiration == 0) {
            if (duration > 0) {
                final long start = startTime > 0 ? startTime : Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTimeInMillis() / 1000L;
                expiration = start + duration;
            } else {
                throw new IllegalArgumentException("Must provide either expiration or duration");
            }
        }
        ArrayList<String> tokenParts = new ArrayList<String>();
        if (ip != null) {
            tokenParts.add("ip=" + ip);
        }
        if (startTime > 0) {
            tokenParts.add("st=" + startTime);
        }
        tokenParts.add("exp=" + expiration);
        if (acl != null) {
            tokenParts.add("acl=" + acl);
        }
        ArrayList<String> toSign = new ArrayList<String>(tokenParts);
        if (url != null) {

            try {
                toSign.add("url=" + escapeUrl(url));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        String auth = digest(StringUtils.join(toSign, "~"));
        tokenParts.add("hmac=" + auth);
        return tokenName + "=" + StringUtils.join(tokenParts, "~");

    }

    /**
     * Escape url using lowercase hex code
     * @param url a url string
     * @return escaped url
     * @throws UnsupportedEncodingException see {@link URLEncoder#encode}
     */
    private String escapeUrl(String url) throws UnsupportedEncodingException {
        String escaped;
        StringBuilder sb= new StringBuilder(URLEncoder.encode(url, "UTF-8"));
        String regex= "%..";
        Pattern p = Pattern.compile(regex); // Create the pattern.
        Matcher matcher = p.matcher(sb); // Create the matcher.
        while (matcher.find()) {
            String buf= sb.substring(matcher.start(), matcher.end()).toLowerCase();
            sb.replace(matcher.start(), matcher.end(), buf);
        }
        escaped = sb.toString();
        return escaped;
    }


    public AuthToken copy() {
        final AuthToken authToken = new AuthToken(key);
        authToken.tokenName = tokenName;
        authToken.startTime = startTime;
        authToken.expiration = expiration;
        authToken.ip = ip;
        authToken.acl = acl;
        authToken.duration = duration;
        return authToken;
    }

    private String digest(String message) {
        byte[] binKey = DatatypeConverter.parseHexBinary(key);
        try {
            Mac hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret = new SecretKeySpec(binKey, "HmacSHA256");
            hmac.init(secret);
            final byte[] bytes = message.getBytes();
            return DatatypeConverter.printHexBinary(hmac.doFinal(bytes)).toLowerCase();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
        return null;
    }

    private AuthToken setNull() {
        isNullToken = true;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if(o instanceof AuthToken) {
            AuthToken other = (AuthToken) o;
            return  (isNullToken && other.isNullToken)  ||
                    key == null ? other.key == null : key.equals(other.key) &&
                    tokenName.equals(other.tokenName) &&
                    startTime == other.startTime &&
                    expiration == other.expiration &&
                    duration == other.duration &&
                    (ip == null ? other.ip == null : ip.equals(other.ip)) &&
                    (acl == null ? other.acl == null : acl.equals(other.acl));
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        if(isNullToken) {
            return 0;
        } else {
            return Arrays.asList(tokenName, startTime, expiration, duration, ip, acl).hashCode();
        }
    }
}
