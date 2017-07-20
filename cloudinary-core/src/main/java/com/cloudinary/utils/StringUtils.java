package com.cloudinary.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;

public class StringUtils {
    public static final String EMPTY = "";

    /**
     * Join a list of Strings
     * @param list strings to join
     * @param separator the separator to insert between the strings
     * @return a string made of the strings in list separated by separator
     */
    public static String join(List<String> list, String separator) {
        if (list == null) {
            return null;
        }

        return join(list.toArray(), separator, 0, list.size());
    }

    /**
     * Join a array of Strings
     * @param array strings to join
     * @param separator the separator to insert between the strings
     * @return a string made of the strings in array separated by separator
     */
    public static String join(Object[] array, String separator) {
        if (array == null) {
            return null;
        }
        return join(array, separator, 0, array.length);
    }

    /**
     * Join a collection of Strings
     * @param collection strings to join
     * @param separator the separator to insert between the strings
     * @return a string made of the strings in collection separated by separator
     */
    public static String join(Collection<String> collection, String separator) {
        if (collection == null) {
            return null;
        }
        return join(collection.toArray(new String[collection.size()]), separator, 0, collection.size());
    }

    /**
     * Join a array of Strings from startIndex to endIndex
     * @param array strings to join
     * @param separator the separator to insert between the strings
     * @param startIndex the string to start from
     * @param endIndex the last string to join
     * @return a string made of the strings in array separated by separator
     */
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

    /**
     * Convert an array of bytes to a string of hex values
     * @param bytes bytes to convert
     * @return a string of hex values.
     */
    public static String encodeHexString(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    /**
     * Convert a string of hex values to an array of bytes
     * @param s a string of two digit Hex numbers. The length of string to parse must be even.
     * @return bytes representation of the string
     */
    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];

        if (len % 2 != 0) {
            throw new IllegalArgumentException("Length of string to parse must be even.");
        }

        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
        }

        return data;
    }

    /**
     * {@see HtmlEscape.escapeHtml}
     */
    public static String escapeHtml(String input) {
        return HtmlEscape.escapeTextArea(input);
    }

    /**
     * Verify that the input has non whitespace characters in it
     * @param input a String-like object
     * @return true if input has non whitespace characters in it
     */
    public static boolean isNotBlank(Object input) {
        if (input == null) return false;
        return !isBlank(input.toString());
    }

    /**
     * Verify that the input has non whitespace characters in it
     * @param input a String
     * @return true if input has non whitespace characters in it
     */
    public static boolean isNotBlank(String input) {
        return !isBlank(input);
    }

    /**
     * Verify that the input has no characters
     * @param input a string
     * @return true if input is null or has no characters
     */
    public static boolean isEmpty(String input) {
        return input == null || input.length() == 0;
    }

    /**
     * Verify that the input is an empty string or contains only whitespace characters.<br>
     *     {@see Character.isWhitespace}
     * @param input a string
     * @return true if input is an empty string or contains only whitespace characters
     */
    public static boolean isBlank(String input) {
        int strLen;
        if (input == null || (strLen = input.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(input.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Read the entire input stream in 1KB chunks
     * @param in input stream to read from
     * @return a String generated from the input stream
     * @throws IOException thrown by the input stream
     */
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
