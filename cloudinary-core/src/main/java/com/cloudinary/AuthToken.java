package com.cloudinary;

import com.cloudinary.utils.ObjectUtils;
import com.cloudinary.utils.StringUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Authentication Token generator
 */
public class AuthToken {
    /**
     * A null AuthToken, which can be passed to a method to override global settings.
     */
    public static final AuthToken NULL_AUTH_TOKEN = new AuthToken().setNull();
    private static final String AUTH_TOKEN_NAME = "__cld_token__";

    private String tokenName = AUTH_TOKEN_NAME;
    private String key;
    private long startTime;
    private long expiration;
    private String ip;
    private List<String> acl = new ArrayList<>();
    private long duration;
    private boolean isNullToken = false;
    private static final Pattern UNSAFE_URL_CHARS_PATTERN = Pattern.compile("[ \"#%&'/:;<=>?@\\[\\\\\\]^`{|}~]");

    public AuthToken() {
    }

    public AuthToken(String key) {
        this.key = key;
    }

    /**
     * Create a new AuthToken configuration.
     *
     * @param options The following keys may be used in the options: key, startTime, expiration, ip, acl, duration.
     */
    public AuthToken(Map options) {
        if (options != null) {
            this.tokenName = ObjectUtils.asString(options.get("tokenName"), this.tokenName);
            this.key = (String) options.get("key");
            this.startTime = ObjectUtils.asLong(options.get("startTime"), 0L);
            this.expiration = ObjectUtils.asLong(options.get("expiration"), 0L);
            this.ip = (String) options.get("ip");

            Object acl = options.get("acl");
            if (acl != null) {
                if (acl instanceof String) {
                    this.acl = Collections.singletonList(acl.toString());
                } else if (Collection.class.isAssignableFrom(acl.getClass())) {
                    this.acl = new ArrayList<String>((Collection<String>)acl);
                }
            }

            this.duration = ObjectUtils.asLong(options.get("duration"), 0L);
        }
    }

    public Map<String,Object> asMap(){
        Map<String,Object> result = new HashMap<String, Object>();

        result.put("tokenName", this.tokenName);
        result.put("key", this.key);
        result.put("startTime", this.startTime);
        result.put("expiration", this.expiration);
        result.put("ip", this.ip);
        result.put("acl", this.acl);
        result.put("duration", this.duration);

        return result;
    }

    /**
     * Create a new AuthToken configuration overriding the default token name.
     *
     * @param tokenName the name of the token. must be supported by the server.
     * @return this
     */
    public AuthToken tokenName(String tokenName) {
        this.tokenName = tokenName;
        return this;
    }

    /**
     * Set the start time of the token. Defaults to now.
     *
     * @param startTime in seconds since epoch
     * @return this
     */
    public AuthToken startTime(long startTime) {
        this.startTime = startTime;
        return this;
    }

    /**
     * Set the end time (expiration) of the token
     *
     * @param expiration in seconds since epoch
     * @return this
     */
    public AuthToken expiration(long expiration) {
        this.expiration = expiration;
        return this;
    }

    /**
     * Set the ip of the client
     *
     * @param ip
     * @return this
     */
    public AuthToken ip(String ip) {
        this.ip = ip;
        return this;
    }

    /**
     * Define an ACL for a cookie token
     *
     * @param acl
     * @return this
     */
    public AuthToken acl(String... acl) {
        this.acl = Arrays.asList(acl);
        return this;
    }

    /**
     * The duration of the token in seconds. This value is used to calculate the expiration of the token.
     * It is ignored if expiration is provided.
     *
     * @param duration in seconds
     * @return this
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

    /**
     * Generate a URL token for the given URL.
     *
     * @param url the URL to be authorized
     * @return a URL token
     */
    public String generate(String url) {

        if (url == null && (acl == null || acl.size() == 0)) {
            throw new IllegalArgumentException("Must provide acl or url");
        }

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
        if (acl != null && acl.size() > 0) {
            tokenParts.add("acl=" + escapeToLower(String.join("!", acl)));
        }
        ArrayList<String> toSign = new ArrayList<String>(tokenParts);
        if (url != null && (acl == null || acl.size() == 0)) {
            toSign.add("url=" + escapeToLower(url));
        }
        String auth = digest(StringUtils.join(toSign, "~"));
        tokenParts.add("hmac=" + auth);
        return tokenName + "=" + StringUtils.join(tokenParts, "~");

    }

    /**
     * Escape url using lowercase hex code
     *
     * @param url a url string
     * @return escaped url
     */
    private String escapeToLower(String url) {
        String encodedUrl = StringUtils.urlEncode(url, UNSAFE_URL_CHARS_PATTERN, Charset.forName("UTF-8"));
        return encodedUrl;
    }

    /**
     * Create a copy of this AuthToken
     *
     * @return a new AuthToken object
     */
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

    /**
     * Merge this token with another, creating a new token. Other's members who are not <code>null</code> or <code>0</code> will override this object's members.
     *
     * @param other the token to merge from
     * @return a new token
     */
    public AuthToken merge(AuthToken other) {
        if (other.equals(NULL_AUTH_TOKEN)) {
            // NULL_AUTH_TOKEN can't merge
            return other;
        }
        AuthToken merged = new AuthToken();
        merged.key = other.key != null ? other.key : this.key;
        merged.tokenName = other.tokenName != null ? other.tokenName : this.tokenName;
        merged.startTime = other.startTime != 0 ? other.startTime : this.startTime;
        merged.expiration = other.expiration != 0 ? other.expiration : this.expiration;
        merged.ip = other.ip != null ? other.ip : this.ip;
        merged.acl = other.acl != null ? other.acl : this.acl;
        merged.duration = other.duration != 0 ? other.duration : this.duration;
        return merged;
    }

    private String digest(String message) {
        byte[] binKey = StringUtils.hexStringToByteArray(key);
        try {
            Mac hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret = new SecretKeySpec(binKey, "HmacSHA256");
            hmac.init(secret);
            final byte[] bytes = message.getBytes();
            return StringUtils.encodeHexString(hmac.doFinal(bytes)).toLowerCase();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Cannot create authorization token.", e);
        } catch (InvalidKeyException e) {
            throw new RuntimeException("Cannot create authorization token.", e);
        }
    }

    private AuthToken setNull() {
        isNullToken = true;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof AuthToken) {
            AuthToken other = (AuthToken) o;
            return (isNullToken && other.isNullToken) ||
                    (key == null ? other.key == null : key.equals(other.key)) &&
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
        if (isNullToken) {
            return 0;
        } else {
            return Arrays.asList(tokenName, startTime, expiration, duration, ip, acl).hashCode();
        }
    }
}
