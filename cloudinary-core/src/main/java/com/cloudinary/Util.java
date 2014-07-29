package com.cloudinary;

import org.apache.commons.lang.StringUtils;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Util {
	static final String[] BOOLEAN_UPLOAD_OPTIONS = new String[] {
		"backup", "exif", "faces", "colors", "image_metadata", "use_filename", "unique_filename", 
		"eager_async", "invalidate", "discard_original_filename", "overwrite", "phash", "return_delete_token"};
	
	protected static final Map<String, Object> buildUploadParams(Map options) {
        if (options == null) options = Cloudinary.emptyMap();
		Map<String, Object> params = new HashMap<String, Object>();
		Object transformation = options.get("transformation");
		if (transformation != null) {
			if (transformation instanceof Transformation) {
				transformation = ((Transformation) transformation).generate();
			}
			params.put("transformation", transformation.toString());
		}
		params.put("public_id", (String) options.get("public_id"));
		params.put("callback", (String) options.get("callback"));
		params.put("format", (String) options.get("format"));
		params.put("type", (String) options.get("type"));
		for (String attr : BOOLEAN_UPLOAD_OPTIONS) {
			Boolean value = Cloudinary.asBoolean(options.get(attr), null);
			if (value != null)
				params.put(attr, value.toString());			
		}
		params.put("eager", buildEager((List<Transformation>) options.get("eager")));
		params.put("notification_url", (String) options.get("notification_url"));
		params.put("eager_notification_url", (String) options.get("eager_notification_url"));
		params.put("proxy", (String) options.get("proxy"));
		params.put("folder", (String) options.get("folder"));
		params.put("allowed_formats", StringUtils.join(Cloudinary.asArray(options.get("allowed_formats")), ","));
		params.put("moderation", options.get("moderation"));
		params.put("upload_preset", options.get("upload_preset"));
		
		processWriteParameters(options, params);
		return params;
	}
	
	protected static final String buildEager(List<? extends Transformation> transformations) {
		if (transformations == null) {
			return null;
		}
		List<String> eager = new ArrayList<String>();
		for (Transformation transformation : transformations) {
			List<String> single_eager = new ArrayList<String>();
			String transformationString = transformation.generate();
			if (StringUtils.isNotBlank(transformationString)) {
				single_eager.add(transformationString);
			}
			if (transformation instanceof EagerTransformation) {
				EagerTransformation eagerTransformation = (EagerTransformation) transformation;
				if (StringUtils.isNotBlank(eagerTransformation.getFormat())) {
					single_eager.add(eagerTransformation.getFormat());
				}
			}
			eager.add(StringUtils.join(single_eager, "/"));
		}
		return StringUtils.join(eager, "|");
	}
	
	protected static final void processWriteParameters(
			Map<String, Object> options, Map<String, Object> params) {
		if (options.get("headers") != null)
			params.put("headers", buildCustomHeaders(options.get("headers")));
		if (options.get("tags") != null)
			params.put("tags", StringUtils.join(
					Cloudinary.asArray(options.get("tags")), ","));
		if (options.get("face_coordinates") != null)
			params.put("face_coordinates", Coordinates.parseCoordinates(options.get("face_coordinates"))
					.toString());
		if (options.get("custom_coordinates") != null)
			params.put("custom_coordinates", Coordinates.parseCoordinates(options.get("custom_coordinates"))
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
		if (options.get("background_removal") != null)
			params.put("background_removal", options.get("background_removal"));
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
			return StringUtils.join((Object[]) headers, "\n") + "\n";
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
	
	protected static void clearEmpty(Map params){
		for (Iterator iterator = params.values().iterator(); iterator.hasNext();) {
			Object value = iterator.next();
			if (value == null || "".equals(value)) {
				iterator.remove();
			}
		}
	}
}
