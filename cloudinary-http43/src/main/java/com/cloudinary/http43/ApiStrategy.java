package com.cloudinary.http43;

import com.cloudinary.Api;
import com.cloudinary.Api.HttpMethod;
import com.cloudinary.Cloudinary;
import com.cloudinary.api.ApiResponse;
import com.cloudinary.api.exceptions.GeneralError;
import com.cloudinary.http43.api.Response;
import com.cloudinary.utils.Base64Coder;
import com.cloudinary.utils.ObjectUtils;
import com.cloudinary.utils.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.cloudinary.json.JSONException;
import org.cloudinary.json.JSONObject;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.cloudinary.http43.ApiUtils.prepareParams;
import static com.cloudinary.http43.ApiUtils.setTimeouts;

public class ApiStrategy extends com.cloudinary.strategies.AbstractApiStrategy {

    private CloseableHttpClient client = null;

    @Override
    public void init(Api api) {
        super.init(api);

        HttpClientBuilder clientBuilder = HttpClients.custom();
        clientBuilder.useSystemProperties().setUserAgent(Cloudinary.USER_AGENT + " ApacheHTTPComponents/4.3");

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
        if (apiKey == null) throw new IllegalArgumentException("Must supply api_key");
        String apiSecret = ObjectUtils.asString(options.get("api_secret"), this.api.cloudinary.config.apiSecret);
        if (apiSecret == null) throw new IllegalArgumentException("Must supply api_secret");


        String apiUrl = StringUtils.join(Arrays.asList(prefix, "v1_1", cloudName), "/");
        for (String component : uri) {
            apiUrl = apiUrl + "/" + component;
        }
        HttpUriRequest request = prepareRequest(method, apiUrl, params, options);

        request.setHeader("Authorization", "Basic " + Base64Coder.encodeString(apiKey + ":" + apiSecret));

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

    /**
     * Prepare a request with the URL and parameters based on the HTTP method used
     * @param method the HTTP method: GET, PUT, POST, DELETE
     * @param apiUrl the cloudinary API URI
     * @param params the parameters to pass to the server
     * @return an HTTP request
     * @throws URISyntaxException
     * @throws UnsupportedEncodingException
     */
    private HttpUriRequest prepareRequest(HttpMethod method, String apiUrl, Map<String, ?> params, Map options) throws URISyntaxException, UnsupportedEncodingException {
        URI apiUri;
        URIBuilder apiUrlBuilder = new URIBuilder(apiUrl);
        List<NameValuePair> parameters;
        HttpRequestBase request;
        parameters = prepareParams(params);
        if(method == HttpMethod.GET) {
            apiUrlBuilder.setParameters(parameters);
            apiUri = apiUrlBuilder.build();
            request = new HttpGet(apiUri);
        } else {
            apiUri = apiUrlBuilder.build();
            switch (method) {
                case PUT:
                    request = new HttpPut(apiUri);
                    break;
                case DELETE: //uses HttpPost instead of HttpDelete
                    parameters.add(new BasicNameValuePair("_method", "delete"));
                    //continue with POST
                case POST:
                    request = new HttpPost(apiUri);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown HTTP method");
            }
            ((HttpEntityEnclosingRequestBase) request).setEntity(new UrlEncodedFormEntity(parameters));
        }

        setTimeouts(request, options);
        return request;
    }


}
