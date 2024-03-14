package com.cloudinary.strategies;

import com.cloudinary.Api;
import com.cloudinary.Api.HttpMethod;
import com.cloudinary.SmartUrlEncoder;
import com.cloudinary.api.ApiResponse;
import com.cloudinary.utils.Base64Coder;
import com.cloudinary.utils.StringUtils;
import java.util.Arrays;
import java.util.Map;


public abstract class AbstractApiStrategy {
    protected Api api;

    public void init(Api api) {
        this.api = api;
    }

    protected String createApiUrl (Iterable<String> uri, String prefix, String cloudName, String v){
        String version = "v1_1";
        if(v != null) {
            version = v;
        }
        String apiUrl = StringUtils.join(Arrays.asList(prefix, version, cloudName), "/");
        for (String component : uri) {
            component = SmartUrlEncoder.encode(component);
            apiUrl = apiUrl + "/" + component;

        }
            return apiUrl;
    }

    @SuppressWarnings("rawtypes")
    public abstract ApiResponse callApi(HttpMethod method, Iterable<String> uri, Map<String, ? extends Object> params, Map options) throws Exception;

    public abstract ApiResponse callAccountApi(HttpMethod method, Iterable<String> uri, Map<String, ? extends Object> params, Map options) throws Exception;

    protected String getAuthorizationHeaderValue(String apiKey, String apiSecret, String oauthToken) {
        if (oauthToken != null){
            return "Bearer " + oauthToken;
        } else {
            return "Basic " + Base64Coder.encodeString(apiKey + ":" + apiSecret);
        }
    }

    protected void validateAuthorization(String apiKey, String apiSecret, String oauthToken) {
        if (oauthToken == null) {
            if (apiKey == null) throw new IllegalArgumentException("Must supply api_key");
            if (apiSecret == null) throw new IllegalArgumentException("Must supply api_secret");
        }
    }
}
