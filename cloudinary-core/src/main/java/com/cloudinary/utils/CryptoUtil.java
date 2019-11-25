package com.cloudinary.utils;

import javax.xml.bind.DatatypeConverter;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class CryptoUtil {
    public static String sha1(String input) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest msdDigest = MessageDigest.getInstance("SHA-1");
        msdDigest.update(input.getBytes("UTF-8"), 0, input.length());
        return DatatypeConverter.printHexBinary(msdDigest.digest());
    }
}
