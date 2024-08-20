package com.cloudinary.okhttp;

import com.cloudinary.Api;
import com.cloudinary.Api.HttpMethod;
import com.cloudinary.Cloudinary;
import com.cloudinary.api.ApiResponse;
import com.cloudinary.api.exceptions.GeneralError;
import com.cloudinary.okhttp.api.ApiResponseWrapper;
import com.cloudinary.utils.Base64Coder;
import com.cloudinary.utils.ObjectUtils;
import com.cloudinary.utils.StringUtils;
import okhttp3.*;
import org.cloudinary.json.JSONException;
import org.cloudinary.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.*;

import static com.cloudinary.okhttp.ApiUtils.prepareParams;
import static com.cloudinary.okhttp.ApiUtils.setTimeouts;

public class ApiStrategy extends com.cloudinary.strategies.AbstractApiStrategy {

    private OkHttpClient client;

    @Override
    public void init(Api api) {
        super.init(api);

        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();

        int timeout = this.api.cloudinary.config.timeout;
        if (timeout > 0) {
            clientBuilder
                    .connectTimeout(timeout, java.util.concurrent.TimeUnit.SECONDS)
                    .readTimeout(timeout, java.util.concurrent.TimeUnit.SECONDS)
                    .writeTimeout(timeout, java.util.concurrent.TimeUnit.SECONDS);
        }

        this.client = clientBuilder.build();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public ApiResponse callApi(HttpMethod method, Iterable<String> uri, Map<String, ?> params, Map options) throws Exception {
        if (options == null)
            options = ObjectUtils.emptyMap();

        String apiKey = ObjectUtils.asString(options.get("api_key"), this.api.cloudinary.config.apiKey);
        String apiSecret = ObjectUtils.asString(options.get("api_secret"), this.api.cloudinary.config.apiSecret);
        String oauthToken = ObjectUtils.asString(options.get("oauth_token"), this.api.cloudinary.config.oauthToken);

        validateAuthorization(apiKey, apiSecret, oauthToken);

        String apiUrl = createApiUrl(uri, options);
        Request request = prepareRequest(method, apiUrl, params, options);

        request = request.newBuilder().header("Authorization", getAuthorizationHeaderValue(apiKey, apiSecret, oauthToken)).build();

        return getApiResponse(request);
    }

    private ApiResponse getApiResponse(Request request) throws Exception {
        String responseData = null;
        int code = 0;
        okhttp3.Response response = null;

        try {
            response = client.newCall(request).execute();
            code = response.code();
            if (response.body() != null) {
                responseData = response.body().string();
            }
        } catch (IOException e) {
            throw new GeneralError("Error executing request: " + e.getMessage());
        }

        if (code != 200) {
            Map<String, Object> result;
            try {
                JSONObject responseJSON = new JSONObject(responseData);
                result = ObjectUtils.toMap(responseJSON);
            } catch (JSONException e) {
                throw new RuntimeException("Invalid JSON response from server " + e.getMessage());
            }

            // Extract the error message from the result map
            String message = (String) ((Map<String, Object>) result.get("error")).get("message");

            // Get the appropriate exception class based on status code
            Class<? extends Exception> exceptionClass = Api.CLOUDINARY_API_ERROR_CLASSES.get(code);
            if (exceptionClass != null) {
                Constructor<? extends Exception> exceptionConstructor = exceptionClass.getConstructor(String.class);
                throw exceptionConstructor.newInstance(message);
            } else {
                throw new GeneralError("Server returned unexpected status code - " + code + " - " + responseData);
            }
        }

        Map<String, Object> result;
        try {
            JSONObject responseJSON = new JSONObject(responseData);
            result = ObjectUtils.toMap(responseJSON);
        } catch (JSONException e) {
            throw new RuntimeException("Invalid JSON response from server " + e.getMessage());
        }

        return new ApiResponseWrapper(response, result);
    }

    @Override
    public ApiResponse callAccountApi(HttpMethod method, Iterable<String> uri, Map<String, ?> params, Map options) throws Exception {
        if (options == null) {
            options = ObjectUtils.emptyMap();
        }

        String prefix = ObjectUtils.asString(options.get("upload_prefix"), "https://api.cloudinary.com");
        String apiKey = ObjectUtils.asString(options.get("provisioning_api_key"));
        if (apiKey == null) throw new IllegalArgumentException("Must supply provisioning_api_key");
        String apiSecret = ObjectUtils.asString(options.get("provisioning_api_secret"));
        if (apiSecret == null) throw new IllegalArgumentException("Must supply provisioning_api_secret");

        String apiUrl = StringUtils.join(Arrays.asList(prefix, "v1_1"), "/");
        for (String component : uri) {
            apiUrl = apiUrl + "/" + component;
        }

        // Prepare the request
        Request request = prepareRequest(method, apiUrl, params, options);

        // Add authorization header
        String authorizationHeaderValue = getAuthorizationHeaderValue(apiKey, apiSecret, null);
        request = request.newBuilder()
                .addHeader("Authorization", authorizationHeaderValue)
                .build();

        // Execute the request and return the response
        return getApiResponse(request);
    }



    private Request prepareRequest(HttpMethod method, String apiUrl, Map<String, ?> params, Map<String, ?> options) {
        Request.Builder requestBuilder = new Request.Builder().url(apiUrl);
        RequestBody requestBody = null;

        String contentType = ObjectUtils.asString(options.get("content_type"), "urlencoded");

        if (method == HttpMethod.GET) {
            HttpUrl.Builder urlBuilder = HttpUrl.parse(apiUrl).newBuilder();
            for (ApiUtils.Param param : prepareParams(params)) {
                urlBuilder.addQueryParameter(param.getKey(), param.getValue());
            }
            requestBuilder.url(urlBuilder.build());
        } else {
            if (contentType.equals("json")) {
                JSONObject json = ObjectUtils.toJSON(params);
                requestBody = RequestBody.create(MediaType.get("application/json; charset=utf-8"), json.toString());
            } else {
                FormBody.Builder formBuilder = new FormBody.Builder();
                for (ApiUtils.Param param : prepareParams(params)) {
                    if(param.getValue() != null) {
                        formBuilder.add(param.getKey(), param.getValue());
                    }
                }
                requestBody = formBuilder.build();
            }
            switch (method) {
                case PUT:
                    requestBuilder.put(requestBody);
                    break;
                case DELETE:
                    requestBuilder.delete(requestBody);
                    break;
                case POST:
                    requestBuilder.post(requestBody);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown HTTP method");
            }
        }

        setTimeouts(requestBuilder, options);
        return requestBuilder.build();
    }
}
