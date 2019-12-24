package com.cloudinary.api.signing;

import com.cloudinary.utils.StringUtils;

class SigningUtils {
    static String safeString(String str) {
        return StringUtils.isEmpty(str) ? "" : str;
    }
}
