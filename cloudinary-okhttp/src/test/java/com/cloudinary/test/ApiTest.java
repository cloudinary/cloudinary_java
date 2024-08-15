package com.cloudinary.test;

import org.apache.http.conn.ConnectTimeoutException;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ApiTest extends AbstractApiTest {
    @Category(TimeoutTest.class)
    @Test(expected = ConnectTimeoutException.class)
    public void testTimeoutException() throws Exception {
        // should allow listing resources
        Map<String, Object> options = new HashMap<String, Object>();
        options.put("timeout", Integer.valueOf(1));

        Map result = api.resources(options);
        Map resource = findByAttr((List<Map>) result.get("resources"), "public_id", "api_test");

    }
}
