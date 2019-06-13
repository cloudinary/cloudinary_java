package com.cloudinary.strategies;

import java.util.Map;

import com.cloudinary.Api;
import com.cloudinary.Api.HttpMethod;
import com.cloudinary.api.ApiResponse;
import com.cloudinary.utils.ObjectUtils;

public abstract class AbstractApiStrategy {
    protected Api api;

    public void init(Api api) {
        this.api = api;
    }

    @SuppressWarnings("rawtypes")
    public abstract ApiResponse callApi(HttpMethod method, Iterable<String> uri, Map<String, ? extends Object> params, Map options) throws Exception;

    protected String getJsonStringFromParams(Map<String, ?> params) {
        if (params.containsKey("json")){
            return params.get("json").toString();
        }

        return ObjectUtils.toJSON(params).toString();
    }
}
