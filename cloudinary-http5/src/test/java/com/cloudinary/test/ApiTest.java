package com.cloudinary.test;

import com.cloudinary.Cloudinary;
import com.cloudinary.api.ApiResponse;
import com.cloudinary.http5.ApiStrategy;
import com.cloudinary.utils.ObjectUtils;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.util.Timeout;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Map;
import java.util.UUID;

import static com.cloudinary.utils.ObjectUtils.asMap;


public class ApiTest extends AbstractApiTest {

    @Test
    public void testBuildRequestConfig_withProxyAndTimeout() {
        Cloudinary cloudinary = new Cloudinary("cloudinary://test:test@test.com");
        cloudinary.config.proxyHost = "127.0.0.1";
        cloudinary.config.proxyPort = 8080;
        cloudinary.config.timeout = 15;

        RequestConfig requestConfig = ((ApiStrategy)cloudinary.api().getStrategy()).buildRequestConfig();

        assert(requestConfig.getProxy() != null);
        HttpHost proxy = requestConfig.getProxy();
        assert("127.0.0.1" == proxy.getHostName());
        assert(8080 == proxy.getPort());

        assert(15000 == requestConfig.getConnectionRequestTimeout().toMilliseconds());
        assert(15000 == requestConfig.getResponseTimeout().toMilliseconds());
    }

    @Test
    public void testBuildRequestConfig_withoutProxy() {
        Cloudinary cloudinary = new Cloudinary("cloudinary://test:test@test.com");
        cloudinary.config.timeout = 10;

        RequestConfig requestConfig = ((ApiStrategy)cloudinary.api().getStrategy()).buildRequestConfig();

        assert(requestConfig.getProxy() == null);
        assert(10000 == requestConfig.getConnectionRequestTimeout().toMilliseconds());
        assert(10000 == requestConfig.getResponseTimeout().toMilliseconds());
    }

    @Category(TimeoutTest.class)
    @Test(expected = Exception.class)
    public void testConnectTimeoutParameter() throws Exception {
        Map<String, Object> options = asMap(
                "max_results", 500,
                "connect_timeout", 0.2);

        try {
            System.out.println("Setting connect timeout to 100 ms");
            ApiResponse result = cloudinary.api().resources(options);
            System.out.println("Request completed without timeout");
        } catch (Exception e) {
            throw new Exception("Connection timeout", e);
        }
    }

    @Category(TimeoutTest.class)
    @Test(expected = Exception.class)
    public void testTimeoutParameter() throws Exception {
        // Set a very short request timeout to trigger a timeout exception
        Map<String, Object> options = asMap(
                "max_results", 500,
                "timeout", Timeout.ofMilliseconds(1000)); // Set the timeout to 1 second

        try {
            ApiResponse result = cloudinary.api().resources(options);
        } catch (Exception e) {
            // Convert IOException to SocketTimeoutException if appropriate
            throw new Exception("Socket timeout");
        }
    }

    @Category(TimeoutTest.class)
    @Test(expected = Exception.class)
    public void testUploaderTimeoutParameter() throws Exception {
        Cloudinary cloudinary = new Cloudinary("cloudinary://test:test@test.com");
        cloudinary.config.uploadPrefix = "https://10.255.255.1";
        String publicId = UUID.randomUUID().toString();
        // Set a very short request timeout to trigger a timeout exception
        Map<String, Object> options = asMap(
                "max_results", 500,
                "timeout", Timeout.ofMilliseconds(10)); // Set the timeout to 1 second

        try {
           Map result = cloudinary.uploader().addContext(asMap("caption", "new caption"), new String[]{publicId, "no-such-id"}, options);
        } catch (Exception e) {
            // Convert IOException to SocketTimeoutException if appropriate
            throw new Exception("Socket timeout");
        }
    }

}