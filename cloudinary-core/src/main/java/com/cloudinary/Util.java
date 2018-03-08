package com.cloudinary;

import com.cloudinary.utils.ObjectUtils;
import com.cloudinary.utils.StringUtils;
import org.cloudinary.json.JSONObject;

import java.util.*;

public class Util {
    static final String[] BOOLEAN_UPLOAD_OPTIONS = new String[]{"backup", "exif", "faces", "colors", "image_metadata", "use_filename", "unique_filename",
            "eager_async", "invalidate", "discard_original_filename", "overwrite", "phash", "return_delete_token", "async"};

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
            putBoolean(attr, options, params);
        }

        params.put("notification_url", (String) options.get("notification_url"));
        params.put("eager_notification_url", (String) options.get("eager_notification_url"));
        params.put("proxy", (String) options.get("proxy"));
        params.put("folder", (String) options.get("folder"));
        params.put("allowed_formats", StringUtils.join(ObjectUtils.asArray(options.get("allowed_formats")), ","));
        params.put("moderation", options.get("moderation"));
        params.put("access_mode", (String) options.get("access_mode"));
        Object responsive_breakpoints = options.get("responsive_breakpoints");
        if (responsive_breakpoints != null) {
            params.put("responsive_breakpoints", JSONObject.wrap(responsive_breakpoints));
        }
        params.put("upload_preset", options.get("upload_preset"));

        if (options.get("signature") == null) {
            putEager("eager", options, params);
            Object transformation = options.get("transformation");
            if (transformation != null) {
                if (transformation instanceof Transformation) {
                    transformation = ((Transformation) transformation).generate();
                }
                params.put("transformation", transformation.toString());
            }
            processWriteParameters(options, params);
        } else {
            // if there's a signature, it means all the params are already serialized so
            // we don't need to construct them, just pass the value as is:
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
            params.put("access_control", (String) options.get("access_control"));
        }
        return params;
    }

    protected static final String buildEager(List<? extends Transformation> transformations) {
        if (transformations == null) {
            return null;
        }

        List<String> eager = new ArrayList<String>();
        for (Transformation transformation : transformations) {
            String transformationString = transformation.generate();
            if (StringUtils.isNotBlank(transformationString)) {
                eager.add(transformationString);
            }
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
            params.put("context", encodeContext(options.get("context")));
        if (options.get("access_control") != null) {
            params.put("access_control", encodeAccessControl(options.get("access_control")));
        }
        putObject("ocr", options, params);
        putObject("raw_convert", options, params);
        putObject("categorization", options, params);
        putObject("detection", options, params);
        putObject("similarity_search", options, params);
        putObject("background_removal", options, params);
        if (options.get("auto_tagging") != null)
            params.put("auto_tagging", ObjectUtils.asFloat(options.get("auto_tagging")));
    }

    protected static String encodeAccessControl(Object accessControl) {
        if (accessControl instanceof AccessControlRule) {
            accessControl = Arrays.asList(accessControl);
        }

        return JSONObject.wrap(accessControl).toString();
    }

    protected static String encodeContext(Object context) {
        if (context != null && context instanceof Map) {
            Map<String, String> mapArg = (Map<String, String>) context;
            HashSet out = new HashSet();
            for (Map.Entry<String, String> entry : mapArg.entrySet()) {
                final String value = entry.getValue().replaceAll("([=\\|])","\\\\$1");
                out.add(entry.getKey() + "=" + value);
            }
            return StringUtils.join(out.toArray(), "|");
        } else if (context == null) {
            return null;
        } else {
            return context.toString();
        }
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
        Map<String, Object> params = new HashMap<String, Object>();
        if (options != null && options.size() > 0){
            params.put("type", options.get("type"));
            params.put("mode", options.get("mode"));
            params.put("target_format", targetFormat);
            params.put("target_public_id", options.get("target_public_id"));
            putBoolean("flatten_folders", options, params);
            putBoolean("flatten_transformations", options, params);
            putBoolean("use_original_filename", options, params);
            putBoolean("async", options, params);
            putBoolean("keep_derived", options, params);
            params.put("notification_url", options.get("notification_url"));
            putArray("target_tags", options, params);
            putArray("tags", options, params);
            putArray("public_ids", options, params);
            putArray("prefixes", options, params);
            putEager("transformations", options, params);
            putObject("timestamp", options, params, Util.timestamp());
            putBoolean("skip_transformation_name", options, params);
            putBoolean("allow_missing", options, params);
            putObject("expires_at", options, params);
        }
        return params;
    }

    private static void putEager(String name, Map from, Map<String, Object> to) {
        final Object transformations = from.get(name);
        if (transformations != null)
            to.put(name, buildEager((List<Transformation>) transformations));
    }

    private static void putBoolean(String name, Map from, Map<String, Object> to) {
        final Object value = from.get(name);
        if(value != null){
            to.put(name, ObjectUtils.asBoolean(value));
        }
    }

    private static void putObject(String name, Map from, Map<String, Object> to) {
        putObject(name, from, to, null);
    }

    private static void putObject(String name, Map from, Map<String, Object> to, Object defaultValue) {
        final Object value = from.get(name);
        if (value != null){
            to.put(name, value);
        } else if(defaultValue != null){
            to.put(name, defaultValue);
        }
    }

    private static void putArray(String name, Map from, Map<String, Object> to) {
        final Object value = from.get(name);
        if (value != null){
            to.put(name, ObjectUtils.asArray(value));
        }
    }

    protected static String timestamp() {
        return Long.toString(System.currentTimeMillis() / 1000L);
    }
}
