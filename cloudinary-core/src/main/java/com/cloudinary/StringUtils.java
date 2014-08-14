package com.cloudinary;

import java.util.List;
import java.util.Collection;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang.StringEscapeUtils;

public class StringUtils {
	public static final String EMPTY = "";

	public static String join(List<String> list, String separator) {
		if (list == null) {
			return null;
		}
		
		return join( list.toArray(), separator, 0, list.size());
	}

	
	public static String join(Object[] array, String separator) {
		if (array == null) {
			return null;
		}
		return join(array, separator, 0, array.length);
	}

	public static String join(Collection<String> collection,String separator) {
		if (collection == null) {
			return null;
		}
		
		return join( collection.toArray(new String[collection.size()]), separator, 0, collection.size());
	}

	public static String join(final Object[] array, String separator,
			final int startIndex, final int endIndex) {
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

	public static String encodeHexString(byte[] bytes) {
		return Hex.encodeHexString(bytes);
	}

	public static String escapeHtml(String input) {
		return StringEscapeUtils.escapeHtml(input);
	}

	public static boolean isNotBlank(String input) {
		return !isBlank(input);
	}

	public static String encodeBase64URLSafeString(byte[] input) {
		return Base64.encodeBase64URLSafeString(input);
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

}
