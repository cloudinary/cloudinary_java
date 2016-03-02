package com.cloudinary.android;

import java.util.Map;

import com.cloudinary.Api.HttpMethod;
import com.cloudinary.api.ApiResponse;
import com.cloudinary.strategies.AbstractApiStrategy;

public class ApiStrategy extends AbstractApiStrategy {

    @SuppressWarnings("rawtypes")
    @Override
    public ApiResponse callApi(HttpMethod method, Iterable<String> uri, Map<String, ?> params, Map options) throws Exception {
        throw new Exception("Administration API is not supported for mobile applications.");
    }

}
