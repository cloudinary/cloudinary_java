package com.cloudinary;

import java.util.Map;

import android.text.TextUtils;

public class Util {
	protected static final void processWriteParameters(
			Map<String, Object> options, Map<String, Object> params) {
		if (options.get("headers") != null)
			params.put("headers", buildCustomHeaders(options.get("headers")));
		if (options.get("tags") != null)
			params.put("tags", TextUtils.join(",", 
					Cloudinary.asArray(options.get("tags"))));
		if (options.get("face_coordinates") != null)
			params.put("face_coordinates", options.get("face_coordinates")
					.toString());
		if (options.get("context") != null)
			params.put("context", Cloudinary.encodeMap(options.get("context")));
		if (options.get("ocr") != null)
			params.put("ocr", options.get("ocr"));
		if (options.get("raw_convert") != null)
			params.put("raw_convert", options.get("raw_convert"));
		if (options.get("categorization") != null)
			params.put("categorization", options.get("categorization"));
		if (options.get("detection") != null)
			params.put("detection", options.get("detection"));
		if (options.get("similarity_search") != null)
			params.put("similarity_search", options.get("similarity_search"));
		if (options.get("auto_tagging") != null)
			params.put("auto_tagging",
					Cloudinary.asFloat(options.get("auto_tagging")));
	}

	protected static final String buildCustomHeaders(Object headers) {
		if (headers == null) {
			return null;
		} else if (headers instanceof String) {
			return (String) headers;
		} else if (headers instanceof Object[]) {
			return TextUtils.join("\n", (Object[]) headers) + "\n";
		} else {
			Map<String, String> headersMap = (Map<String, String>) headers;
			StringBuilder builder = new StringBuilder();
			for (Map.Entry<String, String> header : headersMap.entrySet()) {
				builder.append(header.getKey()).append(": ")
						.append(header.getValue()).append("\n");
			}
			return builder.toString();
		}
	}
}
