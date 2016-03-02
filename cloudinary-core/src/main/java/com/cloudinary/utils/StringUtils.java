package com.cloudinary.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;

public class StringUtils {
    public static final String EMPTY = "";

    public static String join(List<String> list, String separator) {
        if (list == null) {
            return null;
        }

        return join(list.toArray(), separator, 0, list.size());
    }

    public static String join(Object[] array, String separator) {
        if (array == null) {
            return null;
        }
        return join(array, separator, 0, array.length);
    }

    public static String join(Collection<String> collection, String separator) {
        if (collection == null) {
            return null;
        }

        return join(collection.toArray(new String[collection.size()]), separator, 0, collection.size());
    }

    public static String join(final Object[] array, String separator, final int startIndex, final int endIndex) {
        if (array == null) {
            return null;
        }
        if (separator == null) {
            separator = EMPTY;
        }

        final int noOfItems = endIndex - startIndex;
        if (noOfItems <= 0) {
            return EMPTY;
        }

        final StringBuilder buf = new StringBuilder(noOfItems * 16);

        for (int i = startIndex; i < endIndex; i++) {
            if (i > startIndex) {
                buf.append(separator);
            }
            if (array[i] != null) {
                buf.append(array[i]);
            }
        }
        return buf.toString();
    }

    final protected static char[] hexArray = "0123456789abcdef".toCharArray();

    public static String encodeHexString(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static String escapeHtml(String input) {
        return HtmlEscape.escapeTextArea(input);
    }

    public static boolean isNotBlank(Object input) {
        if (input == null) return false;
        return !isBlank(input.toString());
    }

    public static boolean isNotBlank(String input) {
        return !isBlank(input);
    }

    public static boolean isEmpty(String input) {
        if (input == null || input.length() == 0) {
            return true;
        }
        return false;
    }

    public static boolean isBlank(String input) {
        int strLen;
        if (input == null || (strLen = input.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if (Character.isWhitespace(input.charAt(i)) == false) {
                return false;
            }
        }
        return true;
    }

    public static String read(InputStream in) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length = 0;
        while ((length = in.read(buffer)) != -1) {
            baos.write(buffer, 0, length);
        }
        return new String(baos.toByteArray());
    }

}
