package com.cloudinary.http.common;

import com.cloudinary.utils.ObjectUtils;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.apache.hc.core5.util.Timeout;
import org.cloudinary.json.JSONObject;

import java.util.*;

public class ApiUtils {

    public static void setTimeouts(HttpUriRequestBase request, Map<String, ? extends Object> options) {
        RequestConfig config = request.getConfig();
        final RequestConfig.Builder builder;

        if (config != null) {
            builder = RequestConfig.copy(config);
        } else {
            builder = RequestConfig.custom();
        }

        Integer timeout = (Integer) options.get("timeout");
        if (timeout != null) {
            builder.setResponseTimeout(Timeout.ofSeconds(timeout));
        }

        Integer connectionRequestTimeout = (Integer) options.get("connection_request_timeout");
        if (connectionRequestTimeout != null) {
            builder.setConnectionRequestTimeout(Timeout.ofSeconds(connectionRequestTimeout));
        }

        Integer connectTimeout = (Integer) options.get("connect_timeout");
        if (connectTimeout != null) {
            builder.setConnectTimeout(Timeout.ofSeconds(connectTimeout));
        }

        request.setConfig(builder.build());
    }


    public static List<NameValuePair> prepareParams(Map<String, ?> params) {
        List<NameValuePair> requestParams = new ArrayList<>();

        for (Map.Entry<String, ?> param : params.entrySet()) {
            String key = param.getKey();
            Object value = param.getValue();

            if (value instanceof Iterable) {
                // If the value is an Iterable, handle each item individually
                for (Object single : (Iterable<?>) value) {
                    requestParams.add(new BasicNameValuePair(key + "[]", ObjectUtils.asString(single)));
                }
            } else if (value instanceof Map) {
                // Convert Map to JSON string manually to avoid empty object issues
                JSONObject jsonObject = new JSONObject();
                for (Map.Entry<?, ?> entry : ((Map<?, ?>) value).entrySet()) {
                    jsonObject.put(entry.getKey().toString(), entry.getValue());
                }
                requestParams.add(new BasicNameValuePair(key, jsonObject.toString()));
            } else {
                // Handle simple key-value pairs
                requestParams.add(new BasicNameValuePair(key, ObjectUtils.asString(value)));
            }
        }

        return requestParams;
    }
}
