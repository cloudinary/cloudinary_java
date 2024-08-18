package com.cloudinary.test;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.HashMap;
import java.util.Map;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class ApiTest extends AbstractApiTest {

    @Category(TimeoutTest.class)
    @Test(expected = IOException.class)
    public void testTimeoutException() throws Exception {
        // Set up the OkHttpClient with a short timeout to trigger a timeout exception
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(1, TimeUnit.MILLISECONDS)
                .build();

        // Simulate a request to trigger the timeout
        Request request = new Request.Builder()
                .url("https://api.cloudinary.com/v1_1/resource")
                .build();

        try (Response response = client.newCall(request).execute()) {
            // Process the response (if any)
            Map<String, Object> result = new HashMap<>();
            // You would need to implement the processing of the response similar to your API's logic
        } catch (IOException e) {
            // Rethrow the exception to satisfy the @Test(expected = ...) clause
            throw e;
        }
    }
}

