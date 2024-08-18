package com.cloudinary.okhttp;

import com.cloudinary.utils.ObjectUtils;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.FormBody;
import org.cloudinary.json.JSONObject;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class ApiUtils {

    public static void setTimeouts(Request.Builder requestBuilder, Map options) {
        // No direct equivalent in OkHttp, but timeouts are handled in OkHttpClient itself
    }

    static List<Param> prepareParams(Map<String, ?> params) {
        List<Param> requestParams = new ArrayList<>();

        for (Map.Entry<String, ?> param : params.entrySet()) {
            String key = param.getKey();
            Object value = param.getValue();

            if (value instanceof Iterable) {
                // If the value is an Iterable, handle each item individually
                for (Object single : (Iterable<?>) value) {
                    requestParams.add(new Param(key + "[]", ObjectUtils.asString(single)));
                }
            } else if (value instanceof Map) {
                // Convert Map to JSON string manually to avoid empty object issues
                JSONObject jsonObject = new JSONObject();
                for (Map.Entry<?, ?> entry : ((Map<?, ?>) value).entrySet()) {
                    jsonObject.put(entry.getKey().toString(), entry.getValue());
                }
                requestParams.add(new Param(key, jsonObject.toString()));
            } else {
                // Handle simple key-value pairs
                requestParams.add(new Param(key, ObjectUtils.asString(value)));
            }
        }

        return requestParams;
    }

    // Helper class to store parameters as key-value pairs
    static class Param {
        String name;
        String value;

        Param(String name, String value) {
            this.name = name;
            this.value = value;
        }

        String getKey() {
            return name;
        }

        String getValue() {
            return value;
        }
    }

}
