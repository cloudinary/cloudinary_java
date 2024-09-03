package com.cloudinary.test;

import com.cloudinary.api.ApiResponse;
import com.cloudinary.utils.ObjectUtils;
import org.apache.hc.core5.util.Timeout;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.net.SocketTimeoutException;
import java.util.Map;


public class ApiTest extends AbstractApiTest {

    @Category(TimeoutTest.class)
    @Test(expected = Exception.class)
    public void testConnectTimeoutParameter() throws Exception {
        Map<String, Object> options = ObjectUtils.asMap(
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
        Map<String, Object> options = ObjectUtils.asMap(
                "max_results", 500,
                "timeout", Timeout.ofMilliseconds(1000)); // Set the timeout to 1 second

        try {
            ApiResponse result = cloudinary.api().resources(options);
        } catch (Exception e) {
            // Convert IOException to SocketTimeoutException if appropriate
            throw new Exception("Socket timeout");
        }
    }
}

