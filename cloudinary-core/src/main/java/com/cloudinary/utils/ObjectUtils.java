package com.cloudinary.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.cloudinary.json.JSONArray;
import org.cloudinary.json.JSONException;
import org.cloudinary.json.JSONObject;


public class ObjectUtils {

    public static String asString(Object value) {
        if (value == null) {
            return null;
        } else {
            return value.toString();
        }
    }

    public static String asString(Object value, String defaultValue) {
        if (value == null) {
            return defaultValue;
        } else {
            return value.toString();
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static List asArray(Object value) {
        if (value == null) {
            return Collections.EMPTY_LIST;
        } else if (value instanceof int[]) {
            List array = new ArrayList();
            for (int i : (int[]) value) {
                array.add(new Integer(i));
            }
            return array;
        } else if (value instanceof Object[]) {
            return Arrays.asList((Object[]) value);
        } else if (value instanceof List) {
            return (List) value;
        } else {
            List array = new ArrayList();
            array.add(value);
            return array;
        }
    }

    public static Boolean asBoolean(Object value, Boolean defaultValue) {
        if (value == null) {
            return defaultValue;
        } else if (value instanceof Boolean) {
            return (Boolean) value;
        } else {
            return "true".equals(value);
        }
    }

    public static Float asFloat(Object value) {
        if (value == null) {
            return null;
        } else if (value instanceof Float) {
            return (Float) value;
        } else {
            return Float.parseFloat(value.toString());
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static Map asMap(Object... values) {
        if (values.length % 2 != 0)
            throw new RuntimeException("Usage - (key, value, key, value, ...)");
        Map result = new HashMap(values.length / 2);
        for (int i = 0; i < values.length; i += 2) {
            result.put(values[i], values[i + 1]);
        }
        return result;
    }

    @SuppressWarnings("rawtypes")
    public static Map emptyMap() {
        return Collections.EMPTY_MAP;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static String encodeMap(Object arg) {
        if (arg != null && arg instanceof Map) {
            Map<String, String> mapArg = (Map<String, String>) arg;
            HashSet out = new HashSet();
            for (Map.Entry<String, String> entry : mapArg.entrySet()) {
                out.add(entry.getKey() + "=" + entry.getValue());
            }
            return StringUtils.join(out.toArray(), "|");
        } else if (arg == null) {
            return null;
        } else {
            return arg.toString();
        }
    }

    public static Map<String, ? extends Object> only(Map<String, ? extends Object> hash, String... keys) {
        Map<String, Object> result = new HashMap<String, Object>();
        for (String key : keys) {
            if (hash.containsKey(key)) {
                result.put(key, hash.get(key));
            }
        }
        return result;
    }

    @SuppressWarnings("rawtypes")
    public static Map<String, Object> toMap(JSONObject object) throws JSONException {
        @SuppressWarnings("unchecked")
        Map<String, Object> map = new HashMap();
        Iterator keys = object.keys();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            map.put(key, fromJson(object.get(key)));
        }
        return map;
    }

    private static Object fromJson(Object json) throws JSONException {
        if (json == JSONObject.NULL) {
            return null;
        } else if (json instanceof JSONObject) {
            return toMap((JSONObject) json);
        } else if (json instanceof JSONArray) {
            return toList((JSONArray) json);
        } else {
            return json;
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static List toList(JSONArray array) throws JSONException {
        List list = new ArrayList();
        for (int i = 0; i < array.length(); i++) {
            list.add(fromJson(array.get(i)));
        }
        return list;
    }

    public static Integer asInteger(Object value, Integer defaultValue) {
        if (value == null) {
            return defaultValue;
        } else if (value instanceof Integer) {
            return (Integer) value;
        } else {
            return Integer.parseInt(value.toString());
        }
    }

}
