package com.cloudinary.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {
    public static final String EMPTY = "";

    /**
     * Join a list of Strings
     *
     * @param list      strings to join
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
     *
     * @param array     strings to join
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
     *
     * @param collection strings to join
     * @param separator  the separator to insert between the strings
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
     *
     * @param array      strings to join
     * @param separator  the separator to insert between the strings
     * @param startIndex the string to start from
     * @param endIndex   the last string to join
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
     *
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
     *
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
     * Method for html escaping a String
     *
     * @param input The String to escape
     * @return The escaped String
     * @see HtmlEscape#escapeTextArea(String)
     */
    public static String escapeHtml(String input) {
        return HtmlEscape.escapeTextArea(input);
    }

    /**
     * Verify that the input has non whitespace characters in it
     *
     * @param input a String-like object
     * @return true if input has non whitespace characters in it
     */
    public static boolean isNotBlank(Object input) {
        if (input == null) return false;
        return !isBlank(input.toString());
    }

    /**
     * Verify that the input has non whitespace characters in it
     *
     * @param input a String
     * @return true if input has non whitespace characters in it
     */
    public static boolean isNotBlank(String input) {
        return !isBlank(input);
    }

    /**
     * Verify that the input has no characters
     *
     * @param input a string
     * @return true if input is null or has no characters
     */
    public static boolean isEmpty(String input) {
        return input == null || input.length() == 0;
    }

    /**
     * Verify that the input is an empty string or contains only whitespace characters.<br>
     * see {@link Character#isWhitespace(char)}
     *
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
     *
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

    public static boolean isRemoteUrl(String file) {
        return file.matches("ftp:.*|https?:.*|s3:.*|gs:.*|data:[^;]*;base64,([a-zA-Z0-9/+\n=]+)");
    }

    /**
     * Replaces the unsafe characters in url with url-encoded values.
     * This is based on {@link java.net.URLEncoder#encode(String, String)}
     * @param url The url to encode
     * @param unsafe Regex pattern of unsafe caracters
     * @param charset
     * @return An encoded url string
     */
    public static String urlEncode(String url, Pattern unsafe, Charset charset) {
        StringBuffer sb = new StringBuffer(url.length());
        Matcher matcher = unsafe.matcher(url);
        while (matcher.find()) {
            String str = matcher.group(0);
            byte[] bytes = str.getBytes(charset);
            StringBuilder escaped = new StringBuilder(str.length() * 3);

            for (byte aByte : bytes) {
                escaped.append('%');
                char ch = Character.forDigit((aByte >> 4) & 0xF, 16);
                escaped.append(ch);
                ch = Character.forDigit(aByte & 0xF, 16);
                escaped.append(ch);
            }

            matcher.appendReplacement(sb, Matcher.quoteReplacement(escaped.toString().toLowerCase()));
        }

        matcher.appendTail(sb);
        return sb.toString();
    }

    /**
     * Merge all consecutive underscores and spaces into a single underscore, e.g. "ab___c_  _d" becomes "ab_c_d"
     *
     * @param s String to process
     * @return The resulting string.
     */
    public static String mergeToSingleUnderscore(String s) {
        StringBuffer buffer = new StringBuffer();
        boolean inMerge = false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == ' ' || c == '_') {
                if (!inMerge) {
                    buffer.append('_');
                }
                inMerge = true;

            } else {
                inMerge = false;
                buffer.append(c);
            }
        }

        return buffer.toString();
    }

    /**
     * Checks whether the String fits the template for a transformation variable -  $[a-zA-Z][a-zA-Z0-9]+
     * e.g.  $a4, $Bd, $abcdef, etc
     *
     * @param s The string to test
     * @return Whether it's a variable or not
     */
    public static boolean isVariable(String s) {
        if (s == null ||
                s.length() < 3 ||
                !s.startsWith("$") ||
                !Character.isLetter(s.charAt(1))) {
            return false;
        }

        // check that the rest of the string is comprised of letters and digits only:
        for (int i = 2; i < s.length(); i++) {
            char c = s.charAt(i);
            if (!Character.isLetterOrDigit(c)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Replaces the char c in the string S, if it's the first character in the string.
     * @param s The string to search
     * @param c The character to replace
     * @param replacement The string to replace the character in S
     * @return The string with the character replaced (or the original string if the char is not found)
     */
    public static String replaceIfFirstChar(String s, char c, String replacement) {
        return s.charAt(0) == c ? replacement + s.substring(1) : s;
    }

    /**
     * Check if the given string starts with http:// or https://
     * @param s The string to check
     * @return Whether it's an http url or not
     */
    public static boolean isHttpUrl(String s) {
        String lowerCaseSource = s.toLowerCase();
        return lowerCaseSource.startsWith("https:/") || lowerCaseSource.startsWith("http:/");
    }

    /**
     * Remove all consecutive chars c from the beginning of the string
     * @param s String to process
     * @param c Char to search for
     * @return The string stripped from the starting chars.
     */
    public static String removeStartingChars(String s, char c) {
        int lastToRemove = -1;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == c) {
                lastToRemove = i;
                continue;
            }

            if (s.charAt(i) != c) {
                break;
            }
        }

        if (lastToRemove < 0) return s;
        return s.substring(lastToRemove + 1);
    }

    /**
     * Checks whether the url contains a versioning string (v + number, e.g. v12345)
     * @param url The url to check
     * @return Whether a version string is contained within the url
     */
    public static boolean hasVersionString(String url) {
        boolean inVersion = false;
        for (int i = 0; i < url.length(); i++) {
            char c = url.charAt(i);
            if (c == 'v') {
                inVersion = true;
            } else if (Character.isDigit(c) && inVersion) {
                return true;
            } else {
                inVersion = false;
            }


        }

        return false;
    }

    /**
     * Merges all occurrences of multiple slashes into a single slash (e.g. "a///b//c/d" becomes "a/b/c/d")
     * @param url The string to process
     * @return The resulting string with merged slashes.
     */
    public static String mergeSlashesInUrl(String url) {
        StringBuilder builder = new StringBuilder();
        boolean prevIsColon = false;
        boolean inMerge = false;
        for (int i = 0; i < url.length(); i++) {
            char c = url.charAt(i);
            if (c == ':') {
                prevIsColon = true;
                builder.append(c);
            } else {
                if (c == '/') {
                    if (prevIsColon) {
                        builder.append(c);
                        inMerge = false;
                    } else {
                        if (!inMerge) {
                            builder.append(c);
                        }
                        inMerge = true;
                    }
                } else {
                    inMerge = false;
                    builder.append(c);
                }

                prevIsColon = false;
            }
        }

        return builder.toString();
    }
}
