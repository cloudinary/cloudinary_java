package com.cloudinary;

import com.cloudinary.utils.ObjectUtils;
import com.cloudinary.utils.StringUtils;
import org.cloudinary.json.JSONObject;

import java.util.*;

public class Util {
    static final String[] BOOLEAN_UPLOAD_OPTIONS = new String[]{"backup", "exif", "faces", "colors", "image_metadata", "use_filename", "unique_filename",
            "eager_async", "invalidate", "discard_original_filename", "overwrite", "phash", "return_delete_token", "async", "quality_analysis", "cinemagraph_analysis"};

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

    public static Map buildMultiParams(Map options) {
        Map<String, Object> params = new HashMap<String, Object>();

        Object transformation = options.get("transformation");
        if (transformation != null) {
            if (transformation instanceof Transformation) {
                transformation = ((Transformation) transformation).generate();
            }
            params.put("transformation", transformation.toString());
        }
        params.put("tag", options.get("tag"));
        if (options.containsKey("urls")) {
            params.put("urls", Arrays.asList((String[]) options.get("urls")));
        }
        params.put("notification_url", (String) options.get("notification_url"));
        params.put("format", (String) options.get("format"));
        params.put("async", ObjectUtils.asBoolean(options.get("async"), false).toString());
        params.put("mode", options.get("mode"));
        putObject("timestamp", options, params, Util.timestamp());

        return params;
    }

    public static Map<String, Object> buildGenerateSpriteParams(Map options) {
        HashMap<String, Object> params = new HashMap<String, Object>();
        Object transParam = options.get("transformation");
        Transformation transformation = null;
        if (transParam instanceof Transformation) {
            transformation = new Transformation((Transformation) transParam);
        } else if (transParam instanceof String) {
            transformation = new Transformation().rawTransformation((String) transParam);
        } else {
            transformation = new Transformation();
        }
        String format = (String) options.get("format");
        if (format != null) {
            transformation.fetchFormat(format);
        }
        params.put("transformation", transformation.generate());
        params.put("tag", options.get("tag"));
        if (options.containsKey("urls")) {
            params.put("urls", Arrays.asList((String[]) options.get("urls")));
        }
        params.put("notification_url", (String) options.get("notification_url"));
        params.put("async", ObjectUtils.asBoolean(options.get("async"), false).toString());
        params.put("mode", options.get("mode"));
        putObject("timestamp", options, params, Util.timestamp());

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
        if (options.get("metadata") != null)
            params.put("metadata", encodeContext(options.get("metadata")));
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
        if (context instanceof Map) {
            Map<String, Object> mapArg = (Map<String, Object>) context;
            HashSet out = new HashSet();
            for (Map.Entry<String, Object> entry : mapArg.entrySet()) {
                final String value;
                if (entry.getValue() instanceof List) {
                    value = encodeList(((List) entry.getValue()).toArray());
                } else if (entry.getValue() instanceof String[]) {
                    value = encodeList((String[]) entry.getValue());
                } else {
                    value = entry.getValue().toString();
                }
                out.add(entry.getKey() + "=" + encodeSingleContextString(value));
            }
            return StringUtils.join(out.toArray(), "|");
        } else if (context == null) {
            return null;
        } else {
            return context.toString();
        }
    }

    private static String encodeList(Object[] list) {
        StringBuilder builder = new StringBuilder("[");

        boolean first = true;
        for (Object s : list) {
            if (!first) {
                builder.append(",");
            }

            builder.append("\"").append(encodeSingleContextString(s.toString())).append("\"");
            first = false;
        }

        return builder.append("]").toString();
    }

    private static String encodeSingleContextString(String value) {
        return value.replaceAll("([=\\|])", "\\\\$1");
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
        if (options != null && options.size() > 0) {
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
            putArray("fully_qualified_public_ids", options, params);
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
        if (value != null) {
            to.put(name, ObjectUtils.asBoolean(value));
        }
    }

    private static void putObject(String name, Map from, Map<String, Object> to) {
        putObject(name, from, to, null);
    }

    private static void putObject(String name, Map from, Map<String, Object> to, Object defaultValue) {
        final Object value = from.get(name);
        if (value != null) {
            to.put(name, value);
        } else if (defaultValue != null) {
            to.put(name, defaultValue);
        }
    }

    private static void putArray(String name, Map from, Map<String, Object> to) {
        final Object value = from.get(name);
        if (value != null) {
            to.put(name, ObjectUtils.asArray(value));
        }
    }

    protected static String timestamp() {
        return Long.toString(System.currentTimeMillis() / 1000L);
    }

    /**
     * Encodes passed string value into a sequence of bytes using the UTF-8 charset.
     *
     * @param string string value to encode
     * @return byte array representing passed string value
     */
    public static byte[] getUTF8Bytes(String string) {
        try {
            return string.getBytes("UTF-8");
        } catch (java.io.UnsupportedEncodingException e) {
            throw new RuntimeException("Unexpected exception", e);
        }
    }

    /**
     * Calculates signature, or hashed message authentication code (HMAC) of provided parameters name-value pairs and
     * secret value using supported hashing algorithm.
     * <p>
     * Argument for hashing function is built by joining sorted parameter name-value pairs into single string in the
     * same fashion as HTTP GET method uses, and concatenating the result with secret value in the end. Method supports
     * arrays/collections as parameter values. In this case, the elements of array/collection are joined into single
     * comma-delimited string prior to inclusion into the result.
     *
     * @param paramsToSign  parameter name-value pairs list represented as instance of {@link Map}
     * @param apiSecret     secret value
     * @param algorithmType type of hashing algorithm to use for calculation of HMAC
     * @return hex-string representation of signature calculated based on provided parameters map and secret
     */
    public static String produceSignature(Map<String, Object> paramsToSign, String apiSecret, Signer algorithmType) {
        Collection<String> params = new ArrayList<String>();
        for (Map.Entry<String, Object> param : new TreeMap<String, Object>(paramsToSign).entrySet()) {
            if (param.getValue() instanceof Collection) {
                params.add(param.getKey() + "=" + StringUtils.join((Collection) param.getValue(), ","));
            } else if (param.getValue() instanceof Object[]) {
                params.add(param.getKey() + "=" + StringUtils.join((Object[]) param.getValue(), ","));
            } else {
                if (StringUtils.isNotBlank(param.getValue())) {
                    params.add(param.getKey() + "=" + param.getValue().toString());
                }
            }
        }
        String to_sign = StringUtils.join(params, "&");
        byte[] digest = algorithmType.sign(to_sign + apiSecret);
        return StringUtils.encodeHexString(digest);
    }
}
