package com.cloudinary;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class SmartUrlEncoder {
    public static String encode(String input) {
        try {
            return URLEncoder.encode(input, "UTF-8").replace("%2F", "/").replace("%3A", ":").replace("+", "%20");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}
