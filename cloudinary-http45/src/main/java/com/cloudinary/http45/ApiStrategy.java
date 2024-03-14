package com.cloudinary.http45;

import com.cloudinary.Api;
import com.cloudinary.Api.HttpMethod;
import com.cloudinary.Cloudinary;
import com.cloudinary.api.ApiResponse;
import com.cloudinary.api.exceptions.GeneralError;
import com.cloudinary.http45.api.Response;
import com.cloudinary.utils.Base64Coder;
import com.cloudinary.utils.ObjectUtils;
import com.cloudinary.utils.StringUtils;
import org.apache.http.Consts;
import org.apache.http.HttpHost;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.cloudinary.json.JSONException;
import org.cloudinary.json.JSONObject;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.cloudinary.http45.ApiUtils.prepareParams;
import static com.cloudinary.http45.ApiUtils.setTimeouts;

public class ApiStrategy extends com.cloudinary.strategies.AbstractApiStrategy {

    private CloseableHttpClient client = null;

    @Override
    public void init(Api api) {
        super.init(api);

        HttpClientBuilder clientBuilder = HttpClients.custom();
        clientBuilder.useSystemProperties().setUserAgent(this.api.cloudinary.getUserAgent() + " ApacheHTTPComponents/4.5");

        // If the configuration specifies a proxy then apply it to the client
        if (api.cloudinary.config.proxyHost != null && api.cloudinary.config.proxyPort != 0) {
            HttpHost proxy = new HttpHost(api.cloudinary.config.proxyHost, api.cloudinary.config.proxyPort);
            clientBuilder.setProxy(proxy);
        }

        HttpClientConnectionManager connectionManager = (HttpClientConnectionManager) api.cloudinary.config.properties.get("connectionManager");
        if (connectionManager != null) {
            clientBuilder.setConnectionManager(connectionManager);
        }

        int timeout = this.api.cloudinary.config.timeout;
        if (timeout > 0) {
            RequestConfig config = RequestConfig.custom()
                    .setSocketTimeout(timeout * 1000)
                    .setConnectTimeout(timeout * 1000)
                    .build();
            clientBuilder.setDefaultRequestConfig(config);
        }

        this.client = clientBuilder.build();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public ApiResponse callApi(HttpMethod method, Iterable<String> uri, Map<String, ?> params, Map options) throws Exception {
        if (options == null)
            options = ObjectUtils.emptyMap();

        String prefix = ObjectUtils.asString(options.get("upload_prefix"), ObjectUtils.asString(this.api.cloudinary.config.uploadPrefix, "https://api.cloudinary.com"));
        String cloudName = ObjectUtils.asString(options.get("cloud_name"), this.api.cloudinary.config.cloudName);
        if (cloudName == null) throw new IllegalArgumentException("Must supply cloud_name");
        String apiKey = ObjectUtils.asString(options.get("api_key"), this.api.cloudinary.config.apiKey);
        String apiSecret = ObjectUtils.asString(options.get("api_secret"), this.api.cloudinary.config.apiSecret);
        String oauthToken = ObjectUtils.asString(options.get("oauth_token"), this.api.cloudinary.config.oauthToken);

        validateAuthorization(apiKey, apiSecret, oauthToken);

        String version = (String) options.get("api_version");
        String apiUrl = createApiUrl(uri, prefix, cloudName, version);
        HttpUriRequest request = prepareRequest(method, apiUrl, params, options);

        request.setHeader("Authorization", getAuthorizationHeaderValue(apiKey, apiSecret, oauthToken));

        return getApiResponse(request);
    }

    private ApiResponse getApiResponse(HttpUriRequest request) throws Exception {
        String responseData = null;
        int code = 0;
        CloseableHttpResponse response = client.execute(request);
        try {
            code = response.getStatusLine().getStatusCode();
            InputStream responseStream = response.getEntity().getContent();
            responseData = StringUtils.read(responseStream);
        } finally {
            response.close();
        }

        Class<? extends Exception> exceptionClass = Api.CLOUDINARY_API_ERROR_CLASSES.get(code);
        if (code != 200 && exceptionClass == null) {
            throw new GeneralError("Server returned unexpected status code - " + code + " - " + responseData);
        }
        Map result;

        try {
            JSONObject responseJSON = new JSONObject(responseData);
            result = ObjectUtils.toMap(responseJSON);
        } catch (JSONException e) {
            throw new RuntimeException("Invalid JSON response from server " + e.getMessage());
        }

        if (code == 200) {
            return new Response(response, result);
        } else {
            String message = (String) ((Map) result.get("error")).get("message");
            Constructor<? extends Exception> exceptionConstructor = exceptionClass.getConstructor(String.class);
            throw exceptionConstructor.newInstance(message);
        }
    }

    @Override
    public ApiResponse callAccountApi(HttpMethod method, Iterable<String> uri, Map<String, ?> params, Map options) throws Exception {
        if (options == null)
            options = ObjectUtils.emptyMap();

        String prefix = ObjectUtils.asString(options.get("upload_prefix"), "https://api.cloudinary.com");
        String apiKey = ObjectUtils.asString(options.get("provisioning_api_key"));
        if (apiKey == null) throw new IllegalArgumentException("Must supply provisioning_api_key");
        String apiSecret = ObjectUtils.asString(options.get("provisioning_api_secret"));
        if (apiSecret == null) throw new IllegalArgumentException("Must supply provisioning_api_secret");

        String apiUrl = StringUtils.join(Arrays.asList(prefix, "v1_1"), "/");
        for (String component : uri) {
            apiUrl = apiUrl + "/" + component;
        }

        HttpUriRequest request = prepareRequest(method, apiUrl, params, options);

        request.setHeader("Authorization", getAuthorizationHeaderValue(apiKey, apiSecret, null));

        return getApiResponse(request);
    }

    /**
     * Prepare a request with the URL and parameters based on the HTTP method used
     *
     * @param method the HTTP method: GET, PUT, POST, DELETE
     * @param apiUrl the cloudinary API URI
     * @param params the parameters to pass to the server
     * @return an HTTP request
     * @throws URISyntaxException
     * @throws UnsupportedEncodingException
     */
    private HttpUriRequest prepareRequest(HttpMethod method, String apiUrl, Map<String, ?> params, Map options) throws URISyntaxException, UnsupportedEncodingException {
        URI apiUri;
        HttpRequestBase request;

        String contentType = ObjectUtils.asString(options.get("content_type"), "urlencoded");
        URIBuilder apiUrlBuilder = new URIBuilder(apiUrl);
        List<NameValuePair> urlEncodedParams = prepareParams(params);

        if (method == HttpMethod.GET) {
            apiUrlBuilder.setParameters(prepareParams(params));
            apiUri = apiUrlBuilder.build();
            request = new HttpGet(apiUri);
        } else {
            Map<String,Object> paramsCopy =  new HashMap<String, Object>((Map<String,Object>) params);
            apiUri = apiUrlBuilder.build();
            switch (method) {
                case PUT:
                    request = new HttpPut(apiUri);
                    break;
                case DELETE: //uses HttpPost instead of HttpDelete
                    paramsCopy.put("_method", "delete");
                    //continue with POST
                case POST:
                    request = new HttpPost(apiUri);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown HTTP method");
            }
            if (contentType.equals("json")) {
                JSONObject asJSON = ObjectUtils.toJSON(paramsCopy);
                StringEntity requestEntity = new StringEntity(asJSON.toString(), ContentType.APPLICATION_JSON);
                ((HttpEntityEnclosingRequestBase) request).setEntity(requestEntity);
            } else {
                ((HttpEntityEnclosingRequestBase) request).setEntity(new UrlEncodedFormEntity(prepareParams(paramsCopy), Consts.UTF_8));
            }
        }

        setTimeouts(request, options);
        return request;
    }
}
