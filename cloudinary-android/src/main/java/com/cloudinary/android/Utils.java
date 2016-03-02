package com.cloudinary.android;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

public class Utils {
    public static String cloudinaryUrlFromContext(Context context) {
        String url = "";
        try {
            PackageManager packageManager = context.getPackageManager();
            ApplicationInfo info = packageManager.getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            if (info != null && info.metaData != null) {
                url = (String) info.metaData.get("CLOUDINARY_URL");
            }
        } catch (NameNotFoundException e) {
            // No metadata found
        }
        return url;
    }
}
