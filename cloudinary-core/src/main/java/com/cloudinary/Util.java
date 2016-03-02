package com.cloudinary;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.cloudinary.utils.ObjectUtils;
import com.cloudinary.utils.StringUtils;
import org.cloudinary.json.JSONObject;

public class Util {
    static final String[] BOOLEAN_UPLOAD_OPTIONS = new String[]{"backup", "exif", "faces", "colors", "image_metadata", "use_filename", "unique_filename",
            "eager_async", "invalidate", "discard_original_filename", "overwrite", "phash", "return_delete_token"};

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static final Map<String, Object> buildUploadParams(Map options) {
        if (options == null)
            options = ObjectUtils.emptyMap();
        Map<String, Object> params = new HashMap<String, Object>();

        params.put("public_id", (String) options.get("public_id"));
        params.put("callback", (String) options.get("callback"));
        params.put("format", (String) options.get("format"));
        params.put("type", (String) options.get("type"));
        for (String attr : BOOLEAN_UPLOAD_OPTIONS) {
            Boolean value = ObjectUtils.asBoolean(options.get(attr), null);
            if (value != null)
                params.put(attr, value.toString());
        }

        params.put("notification_url", (String) options.get("notification_url"));
        params.put("eager_notification_url", (String) options.get("eager_notification_url"));
        params.put("proxy", (String) options.get("proxy"));
        params.put("folder", (String) options.get("folder"));
        params.put("allowed_formats", StringUtils.join(ObjectUtils.asArray(options.get("allowed_formats")), ","));
        params.put("moderation", options.get("moderation"));
        Object responsive_breakpoints = options.get("responsive_breakpoints");
        if (responsive_breakpoints != null) {
            params.put("responsive_breakpoints", JSONObject.wrap(responsive_breakpoints));
        }
        params.put("upload_preset", options.get("upload_preset"));

        if (options.get("signature") == null) {
            params.put("eager", buildEager((List<Transformation>) options.get("eager")));
            Object transformation = options.get("transformation");
            if (transformation != null) {
                if (transformation instanceof Transformation) {
                    transformation = ((Transformation) transformation).generate();
                }
                params.put("transformation", transformation.toString());
            }
            processWriteParameters(options, params);
        } else {
            params.put("eager", (String) options.get("eager"));
            params.put("transformation", (String) options.get("transformation"));
            params.put("headers", (String) options.get("headers"));
            params.put("tags", (String) options.get("tags"));
            params.put("face_coordinates", (String) options.get("face_coordinates"));
            params.put("context", (String) options.get("context"));
            params.put("ocr", (String) options.get("ocr"));
            params.put("raw_convert", (String) options.get("raw_convert"));
            params.put("categorization", (String) options.get("categorization"));
            params.put("detection", (String) options.get("detection"));
            params.put("similarity_search", (String) options.get("similarity_search"));
            params.put("auto_tagging", (String) options.get("auto_tagging"));
        }
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

    @SuppressWarnings("unchecked")
    public static final void processWriteParameters(Map<String, Object> options, Map<String, Object> params) {
        if (options.get("headers") != null)
            params.put("headers", buildCustomHeaders(options.get("headers")));
        if (options.get("tags") != null)
            params.put("tags", StringUtils.join(ObjectUtils.asArray(options.get("tags")), ","));
        if (options.get("face_coordinates") != null)
            params.put("face_coordinates", Coordinates.parseCoordinates(options.get("face_coordinates")).toString());
        if (options.get("custom_coordinates") != null)
            params.put("custom_coordinates", Coordinates.parseCoordinates(options.get("custom_coordinates")).toString());
        if (options.get("context") != null)
            params.put("context", ObjectUtils.encodeMap(options.get("context")));
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
            params.put("auto_tagging", ObjectUtils.asFloat(options.get("auto_tagging")));
    }

    @SuppressWarnings("unchecked")
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
                builder.append(header.getKey()).append(": ").append(header.getValue()).append("\n");
            }
            return builder.toString();
        }
    }

    @SuppressWarnings("rawtypes")
    public static void clearEmpty(Map params) {
        for (Iterator iterator = params.values().iterator(); iterator.hasNext(); ) {
            Object value = iterator.next();
            if (value == null || "".equals(value)) {
                iterator.remove();
            }
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static final Map<String, Object> buildArchiveParams(Map options, String targetFormat) {
        if (options == null)
            options = ObjectUtils.emptyMap();
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("type", options.get("type"));
        params.put("mode", options.get("mode"));
        params.put("target_format", targetFormat);
        params.put("target_public_id", options.get("target_public_id"));
        params.put("flatten_folders", ObjectUtils.asBoolean(options.get("flatten_folders"), false));
        params.put("flatten_transformations", ObjectUtils.asBoolean(options.get("flatten_transformations"), false));
        params.put("use_original_filename", ObjectUtils.asBoolean(options.get("use_original_filename"), false));
        params.put("async", ObjectUtils.asBoolean(options.get("async"), false));
        params.put("keep_derived", ObjectUtils.asBoolean(options.get("keep_derived"), false));
        params.put("notification_url", options.get("notification_url"));
        if (options.get("target_tags") != null)
            params.put("target_tags", ObjectUtils.asArray(options.get("target_tags")));
        if (options.get("tags") != null)
            params.put("tags", ObjectUtils.asArray(options.get("tags")));
        if (options.get("public_ids") != null)
            params.put("public_ids", ObjectUtils.asArray(options.get("public_ids")));
        if (options.get("prefixes") != null)
            params.put("prefixes", ObjectUtils.asArray(options.get("prefixes")));
        if (options.get("transformations") != null)
            params.put("transformations", buildEager((List<Transformation>) options.get("transformations")));
        if (options.get("timestamp") != null)
            params.put("timestamp", options.get("timestamp"));
        else
            params.put("timestamp", Util.timestamp());
        return params;
    }

    protected static String timestamp() {
        return new Long(System.currentTimeMillis() / 1000L).toString();
    }
}
