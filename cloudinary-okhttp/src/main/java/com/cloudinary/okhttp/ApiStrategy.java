package com.cloudinary.okhttp;

import com.cloudinary.Api;
import com.cloudinary.Api.HttpMethod;
import com.cloudinary.api.ApiResponse;
import com.cloudinary.api.exceptions.GeneralError;
import com.cloudinary.strategies.AbstractApiStrategy;
import com.cloudinary.utils.ObjectUtils;
import com.cloudinary.utils.StringUtils;
import okhttp3.*;
import org.cloudinary.json.JSONException;
import org.cloudinary.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.cloudinary.okhttp.ApiUtils.prepareParams;
import static com.cloudinary.okhttp.ApiUtils.setTimeouts;

public class ApiStrategy extends AbstractApiStrategy {

    private OkHttpClient client;

    @Override
    public void init(Api api) {
        super.init(api);

        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();

        // Set timeouts if specified in the config
        if (api.cloudinary.config.timeout > 0) {
            clientBuilder
                    .connectTimeout(api.cloudinary.config.timeout, java.util.concurrent.TimeUnit.SECONDS)
                    .readTimeout(api.cloudinary.config.timeout, java.util.concurrent.TimeUnit.SECONDS)
                    .writeTimeout(api.cloudinary.config.timeout, java.util.concurrent.TimeUnit.SECONDS);
        }

        // If the configuration specifies a proxy, apply it to the client
        if (api.cloudinary.config.proxyHost != null && api.cloudinary.config.proxyPort != 0) {
            java.net.Proxy proxy = new java.net.Proxy(
                    java.net.Proxy.Type.HTTP,
                    new java.net.InetSocketAddress(api.cloudinary.config.proxyHost, api.cloudinary.config.proxyPort)
            );
            clientBuilder.proxy(proxy);
        }

        this.client = clientBuilder.build();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public ApiResponse callApi(HttpMethod method, Iterable<String> uri, Map<String, ?> params, Map options) throws Exception {
        if (options == null) {
            options = ObjectUtils.emptyMap();
        }

        String apiKey = ObjectUtils.asString(options.get("api_key"), this.api.cloudinary.config.apiKey);
        String apiSecret = ObjectUtils.asString(options.get("api_secret"), this.api.cloudinary.config.apiSecret);
        String oauthToken = ObjectUtils.asString(options.get("oauth_token"), this.api.cloudinary.config.oauthToken);

        validateAuthorization(apiKey, apiSecret, oauthToken);

        String apiUrl = createApiUrl(uri, options);
        Request request = prepareRequest(method, apiUrl, params, options, apiKey, apiSecret, oauthToken);

        return getApiResponse(request);
    }

    private ApiResponse getApiResponse(Request request) throws Exception {
        try (Response response = client.newCall(request).execute()) {
            int code = response.code();
            String responseData = response.body().string();

            Class<? extends Exception> exceptionClass = Api.CLOUDINARY_API_ERROR_CLASSES.get(code);
            if (code != 200 && exceptionClass == null) {
                throw new GeneralError("Server returned unexpected status code - " + code + " - " + responseData);
            }

            Map<String, Object> result;
            try {
                JSONObject responseJSON = new JSONObject(responseData);
                result = ObjectUtils.toMap(responseJSON);
            } catch (JSONException e) {
                throw new RuntimeException("Invalid JSON response from server: " + e.getMessage());
            }

            if (code == 200) {
                return new ApiResponse(code, result, null);
            } else {
                String message = (String) ((Map) result.get("error")).get("message");
                throw exceptionClass.getConstructor(String.class).newInstance(message);
            }
        }
    }

    @Override
    public ApiResponse callAccountApi(HttpMethod method, List<String> uri, Map<String, ?> params, Map options) throws Exception {
        if (options == null) {
            options = ObjectUtils.emptyMap();
        }

        String prefix = ObjectUtils.asString(options.get("upload_prefix"), "https://api.cloudinary.com");
        String apiKey = ObjectUtils.asString(options.get("provisioning_api_key"));
        if (apiKey == null) throw new IllegalArgumentException("Must supply provisioning_api_key");
        String apiSecret = ObjectUtils.asString(options.get("provisioning_api_secret"));
        if (apiSecret == null) throw new IllegalArgumentException("Must supply provisioning_api_secret");


        String apiUrl = StringUtils.join(uri, "/");
        Request request = prepareRequest(method, apiUrl, params, options, apiKey, apiSecret, null);

        return getApiResponse(request);
    }

    private Request prepareRequest(HttpMethod method, String apiUrl, Map<String, ?> params, Map options, String apiKey, String apiSecret, String oauthToken) throws IOException {
        Request.Builder requestBuilder = new Request.Builder()
                .url(apiUrl)
                .header("Authorization", getAuthorizationHeaderValue(apiKey, apiSecret, oauthToken));

        RequestBody body = null;
        if (method == HttpMethod.GET) {
            apiUrl = prepareUrlWithParams(apiUrl, params);
            requestBuilder.url(apiUrl).get();
        } else {
            if (options != null && "json".equals(options.get("content_type"))) {
                String jsonString = ObjectUtils.toJSON(params).toString();
                body = RequestBody.create(jsonString, MediaType.parse("application/json"));
            } else {
                FormBody.Builder formBuilder = new FormBody.Builder();
                List<Map.Entry<String, String>> urlEncodedParams = prepareParams(params);
                for (Map.Entry<String, String> entry : urlEncodedParams) {
                    formBuilder.add(entry.getKey(), entry.getValue());
                }
                body = formBuilder.build();
            }
            if (method == HttpMethod.POST) {
                requestBuilder.post(body);
            } else if (method == HttpMethod.PUT) {
                requestBuilder.put(body);
            } else if (method == HttpMethod.DELETE) {
                requestBuilder.delete(body);
            }
        }
        return requestBuilder.build();
    }

    private String prepareUrlWithParams(String url, Map<String, ?> params) {
        HttpUrl.Builder urlBuilder = HttpUrl.parse(url).newBuilder();
        for (Map.Entry<String, ?> entry : params.entrySet()) {
            urlBuilder.addQueryParameter(entry.getKey(), entry.getValue().toString());
        }
        return urlBuilder.build().toString();
    }
}
