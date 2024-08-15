package com.cloudinary.okhttp;

import com.cloudinary.utils.ObjectUtils;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.RequestBody;

import java.util.Map;

public class ApiUtils {

    public static void setTimeouts(Request.Builder requestBuilder, Map<String, ?> options) {
        // OkHttp timeout settings are done at the client level, not per request
        // If you need custom timeouts per request, you would need to create a new client instance per request.
    }

    static HttpUrl.Builder prepareParams(String url, Map<String, ?> params) {
        HttpUrl.Builder urlBuilder = HttpUrl.parse(url).newBuilder();
        for (Map.Entry<String, ?> param : params.entrySet()) {
            if (param.getValue() instanceof Iterable) {
                for (Object single : (Iterable<?>) param.getValue()) {
                    urlBuilder.addQueryParameter(param.getKey() + "[]", ObjectUtils.asString(single));
                }
            } else {
                urlBuilder.addQueryParameter(param.getKey(), ObjectUtils.asString(param.getValue()));
            }
        }
        return urlBuilder;
    }

    static RequestBody prepareRequestBody(Map<String, ?> params, String contentType) {
        if ("json".equals(contentType)) {
            String jsonString = ObjectUtils.toJSON(params).toString();
            return RequestBody.create(jsonString, okhttp3.MediaType.parse("application/json"));
        } else {
            FormBody.Builder formBuilder = new FormBody.Builder();
            for (Map.Entry<String, ?> param : params.entrySet()) {
                if (param.getValue() instanceof Iterable) {
                    for (Object single : (Iterable<?>) param.getValue()) {
                        formBuilder.add(param.getKey() + "[]", ObjectUtils.asString(single));
                    }
                } else {
                    formBuilder.add(param.getKey(), ObjectUtils.asString(param.getValue()));
                }
            }
            return formBuilder.build();
        }
    }
}
