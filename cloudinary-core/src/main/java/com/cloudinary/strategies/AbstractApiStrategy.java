package com.cloudinary.strategies;

import com.cloudinary.Api;
import com.cloudinary.Api.HttpMethod;
import com.cloudinary.SmartUrlEncoder;
import com.cloudinary.api.ApiResponse;
import com.cloudinary.utils.Base64Coder;
import com.cloudinary.utils.ObjectUtils;
import com.cloudinary.utils.StringUtils;
import java.util.Arrays;
import java.util.Map;


public abstract class AbstractApiStrategy {
    protected Api api;

    public void init(Api api) {
        this.api = api;
    }

    protected String createApiUrl (Iterable<String> uri, Map options){
        String version = ObjectUtils.asString(options.get("api_version"), "v1_1");
        String prefix = ObjectUtils.asString(options.get("upload_prefix"), ObjectUtils.asString(this.api.cloudinary.config.uploadPrefix, "https://api.cloudinary.com"));
        String cloudName = ObjectUtils.asString(options.get("cloud_name"), this.api.cloudinary.config.cloudName);
        if (cloudName == null) throw new IllegalArgumentException("Must supply cloud_name");
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
