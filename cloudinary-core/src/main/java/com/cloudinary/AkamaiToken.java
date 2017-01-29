package com.cloudinary;

import com.cloudinary.utils.StringUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * Token generator for Akamai authentication
 */
public class AkamaiToken {
    public String tokenName = Cloudinary.AKAMAI_TOKEN_NAME;
    public String key;
    public long startTime;
    public long endTime;
    public String ip;
    public String acl;
    public long window;

    public AkamaiToken(String key) {
        this.key = key;
    }

    public AkamaiToken setTokenName(String tokenName) {
        this.tokenName = tokenName;
        return this;
    }

    /**
     * Set the start time of the token. Defaults to now.
     *
     * @param startTime in seconds since epoch
     * @return
     */
    public AkamaiToken setStartTime(long startTime) {
        this.startTime = startTime;
        return this;
    }

    /**
     * Set the end time (expiration) of the token
     *
     * @param endTime in seconds since epoch
     * @return
     */
    public AkamaiToken setEndTime(long endTime) {
        this.endTime = endTime;
        return this;
    }

    public AkamaiToken setIp(String ip) {
        this.ip = ip;
        return this;
    }

    public AkamaiToken setAcl(String acl) {
        this.acl = acl;
        return this;
    }

    /**
     * The duration of the token in seconds. This value is used to calculate the expiration of the token.
     * It is ignored if endTime is provided.
     *
     * @param window
     * @return
     */
    public AkamaiToken setWindow(long window) {
        this.window = window;
        return this;
    }

    /**
     * Generate the authentication token
     *
     * @return a signed token
     */
    public String generate() {
        long expiration = endTime;
        if (expiration == 0) {
            if (window > 0) {
                final long start = startTime > 0 ? startTime : Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTimeInMillis() / 1000L;
                expiration = start + window;
            } else {
                throw new IllegalArgumentException("Must provide either endTime or window");
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
        tokenParts.add("acl=" + acl);
        String auth = digest(StringUtils.join(tokenParts, "~"));
        tokenParts.add("hmac=" + auth);
        return tokenName + "=" + StringUtils.join(tokenParts, "~");
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


}
