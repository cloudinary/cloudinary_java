package com.cloudinary.test;

import com.cloudinary.api.ApiResponse;
import com.cloudinary.utils.ObjectUtils;
import org.apache.http.conn.ConnectTimeoutException;
import org.junit.Test;

import java.net.SocketTimeoutException;
import java.util.Map;

public class ApiTest extends AbstractApiTest {

    @Test(expected = ConnectTimeoutException.class)
    public void testConnectTimeoutParameter() throws Exception {
        // should allow listing resources
        Map options = ObjectUtils.asMap(
                "max_results", 500,
                "connect_timeout", 1);
        ApiResponse result = cloudinary.api().resources(options);
    }

    @Test(expected = SocketTimeoutException.class)
    public void testTimeoutParameter() throws Exception {
        // should allow listing resources
        Map options = ObjectUtils.asMap(
                "max_results", 500,
                "timeout", 1);
        ApiResponse result = cloudinary.api().resources(options);
    }

}
