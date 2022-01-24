package com.cloudinary.utils;

import java.net.MalformedURLException;
import java.net.URL;

public class AnalyticsUtils {
    public static String analyticsPrefix = "?_a=";
    public static String token = null;

    public static Boolean checkIfQueryParamExist(String urlString) {
        try {
            URL url = new URL(urlString);
            if (url.getQuery() == null) {
                return false;
            }
        } catch (MalformedURLException e) {
            return true;
        }
        return true;
    }
}
