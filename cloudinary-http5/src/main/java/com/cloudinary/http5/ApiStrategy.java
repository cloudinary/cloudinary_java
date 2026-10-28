package com.cloudinary.http5;


import com.cloudinary.Api;
import com.cloudinary.api.ApiResponse;
import com.cloudinary.api.exceptions.GeneralError;
import com.cloudinary.http5.api.Response;
import com.cloudinary.strategies.AbstractApiStrategy;
import com.cloudinary.utils.ObjectUtils;
import org.apache.hc.client5.http.classic.methods.*;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.net.URIBuilder;
import org.apache.hc.core5.util.Timeout;
import org.cloudinary.json.JSONException;
import org.cloudinary.json.JSONObject;
import org.cloudinary.json.JSONTokener;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.cloudinary.http5.ApiUtils.prepareParams;
import static com.cloudinary.http5.ApiUtils.setTimeouts;

public class ApiStrategy extends AbstractApiStrategy {

    private static final String APACHE_HTTP_CLIENT_VERSION = System.getProperty("apache.http.client.version", "5.6.4");

    private CloseableHttpClient client;

    public void init(Api api) {
        super.init(api);

        HttpClientBuilder clientBuilder = HttpClients.custom();
        clientBuilder.useSystemProperties().setUserAgent(this.api.cloudinary.getUserAgent() + " ApacheHttpClient/" + APACHE_HTTP_CLIENT_VERSION);

        HttpClientConnectionManager connectionManager = (HttpClientConnectionManager) api.cloudinary.config.properties.get("connectionManager");
        if (connectionManager != null) {
            clientBuilder.setConnectionManager(connectionManager);
        }

        RequestConfig requestConfig = buildRequestConfig();

        client = clientBuilder
                .setDefaultRequestConfig(requestConfig)
                .build();
    }

    public RequestConfig buildRequestConfig() {
        RequestConfig.Builder requestConfigBuilder = RequestConfig.custom();

        if (api.cloudinary.config.proxyHost != null && api.cloudinary.config.proxyPort != 0) {
            HttpHost proxy = new HttpHost(api.cloudinary.config.proxyHost, api.cloudinary.config.proxyPort);
            requestConfigBuilder.setProxy(proxy);
        }

        int timeout = this.api.cloudinary.config.timeout;
        if (timeout > 0) {
            requestConfigBuilder.setResponseTimeout(Timeout.ofSeconds(timeout))
                    .setConnectionRequestTimeout(Timeout.ofSeconds(timeout))
                    .setConnectTimeout(Timeout.ofSeconds(timeout));
        }

        return requestConfigBuilder.build();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public ApiResponse callApi(Api.HttpMethod method, String apiUrl, Map<String, ?> params, Map options, String autorizationHeader) throws Exception {
        HttpUriRequestBase request = prepareRequest(method, apiUrl, params, options);

        request.setHeader("Authorization", autorizationHeader);

        return getApiResponse(request, options);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private ApiResponse getApiResponse(HttpUriRequestBase request, Map options) throws Exception {
        boolean skipResponseBody = options != null && ObjectUtils.asBoolean(options.get("skip_response_body"), false);

        try (CloseableHttpResponse response = client.execute(request)) {
            int code = response.getCode();
            HttpEntity entity = response.getEntity();

            if (code != 200) {
                String responseData = entity != null ? EntityUtils.toString(entity, StandardCharsets.UTF_8) : null;
                throw buildError(code, responseData);
            }

            if (skipResponseBody) {
                EntityUtils.consumeQuietly(entity);
                return new Response(response, Collections.<String, Object>emptyMap());
            }

            if (entity == null) {
                throw new RuntimeException("Invalid JSON response from server: empty response body");
            }

            // Parse straight from the stream (explicit UTF-8) to avoid buffering the whole body as a String.
            Map<String, Object> result;
            try (InputStream content = entity.getContent()) {
                JSONObject responseJSON = new JSONObject(new JSONTokener(new InputStreamReader(content, StandardCharsets.UTF_8)));
                result = ObjectUtils.toMap(responseJSON);
            } catch (JSONException e) {
                if (e.getCause() instanceof IOException) {
                    // network failure while reading the body: same error type as before streaming the parse
                    throw new GeneralError("Error executing request: " + e.getMessage());
                }
                throw new RuntimeException("Invalid JSON response from server " + e.getMessage());
            }
            EntityUtils.consumeQuietly(entity);

            return new Response(response, result);
        } catch (IOException e) {
            throw new GeneralError("Error executing request: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private Exception buildError(int code, String responseData) throws Exception {
        Map<String, Object> result;
        if (responseData == null) {
            throw new RuntimeException("Invalid JSON response from server null");
        }
        try {
            result = ObjectUtils.toMap(new JSONObject(responseData));
        } catch (JSONException e) {
            throw new RuntimeException("Invalid JSON response from server " + e.getMessage());
        }

        Object error = result.get("error");
        String message = error instanceof Map ? (String) ((Map<String, Object>) error).get("message") : null;

        Class<? extends Exception> exceptionClass = Api.CLOUDINARY_API_ERROR_CLASSES.get(code);
        if (exceptionClass != null) {
            Constructor<? extends Exception> exceptionConstructor = exceptionClass.getConstructor(String.class);
            return exceptionConstructor.newInstance(message);
        }
        return new GeneralError("Server returned unexpected status code - " + code + " - " + responseData);
    }

    @Override
    public ApiResponse callAccountApi(Api.HttpMethod method, String apiUrl, Map<String, ?> params, Map options, String authorizationHeader) throws Exception {
        // Prepare the request
        HttpUriRequestBase request = prepareRequest(method, apiUrl, params, options);

        // Add authorization header

        request.setHeader("Authorization", authorizationHeader);

        // Execute the request and return the response
        return getApiResponse(request, options);
    }

    private HttpUriRequestBase prepareRequest(Api.HttpMethod method, String apiUrl, Map<String, ? extends Object> params, Map<String, ?> options) throws URISyntaxException {
        HttpUriRequestBase request;

        String contentType = ObjectUtils.asString(options.get("content_type"), "urlencoded");

        switch (method) {
            case GET:
                URIBuilder uriBuilder = new URIBuilder(apiUrl);
                for (NameValuePair param : prepareParams(params)) {
                    uriBuilder.addParameter(param.getName(), param.getValue());
                }
                request = new HttpGet(uriBuilder.toString());
                break;
            case POST:
                request = new HttpPost(apiUrl);
                setEntity((HttpUriRequestBase) request, params, contentType);
                break;
            case PUT:
                request = new HttpPut(apiUrl);
                setEntity((HttpUriRequestBase) request, params, contentType);
                break;
            case DELETE:
                request = new HttpDelete(apiUrl);
                setEntity((HttpUriRequestBase) request, params, contentType);
                break;
            default:
                throw new IllegalArgumentException("Unknown HTTP method");
        }
        setTimeouts(request, options);
        return request;
    }

    private void setEntity(HttpUriRequestBase request, Map<String, ?> params, String contentType) {
        if ("json".equals(contentType)) {
            JSONObject json = ObjectUtils.toJSON(params);
            StringEntity entity = new StringEntity(json.toString(), StandardCharsets.UTF_8);
            request.setEntity(entity);
            request.setHeader("Content-Type", "application/json");
        } else {
            List<NameValuePair> formParams = prepareParams(params);
            request.setEntity(new UrlEncodedFormEntity(formParams, StandardCharsets.UTF_8));
        }
    }
}
