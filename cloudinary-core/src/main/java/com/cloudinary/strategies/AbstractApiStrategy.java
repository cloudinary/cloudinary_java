package com.cloudinary.strategies;

import com.cloudinary.Api;
import com.cloudinary.Api.HttpMethod;
import com.cloudinary.api.ApiResponse;
import java.util.Map;


public abstract class AbstractApiStrategy {
    protected Api api;

    public void init(Api api) {
        this.api = api;
    }

    @SuppressWarnings("rawtypes")
    public abstract ApiResponse callApi(HttpMethod method, String apiUrl, Map<String, ? extends Object> params, Map options, String authorizationHeader) throws Exception;

    public abstract ApiResponse callAccountApi(HttpMethod method, String apiUrl, Map<String, ? extends Object> params, Map options, String authorizationHeader) throws Exception;
}
