package com.cloudinary.http44;

import com.cloudinary.utils.ObjectUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ApiUtils {

    public static void setTimeouts(HttpRequestBase request, Map options) {
        RequestConfig config= request.getConfig();
        final RequestConfig.Builder builder;
        if (config != null) {
            builder = RequestConfig.copy(config);
        } else {
            builder = RequestConfig.custom();
        }
        Integer timeout = (Integer) options.get("timeout");
        if(timeout != null) {
                builder.setSocketTimeout(timeout);
        }
        Integer connectionRequestTimeout = (Integer) options.get("connection_request_timeout");
        if(connectionRequestTimeout != null) {
            builder.setConnectionRequestTimeout(connectionRequestTimeout);
        }
        Integer connectTimeout = (Integer) options.get("connect_timeout");
        if(connectTimeout != null) {
            builder.setConnectTimeout(connectTimeout);
        }
        request.setConfig(builder.build());
    }

    static List<NameValuePair> prepareParams(Map<String, ?> params) {
        List<NameValuePair> requestParams = new ArrayList<NameValuePair>(params.size());
        for (Map.Entry<String, ?> param : params.entrySet()) {
            if (param.getValue() instanceof Iterable) {
                for (Object single : (Iterable<?>) param.getValue()) {
                    requestParams.add(new BasicNameValuePair(param.getKey() + "[]", ObjectUtils.asString(single)));
                }
            } else {
                requestParams.add(new BasicNameValuePair(param.getKey(), ObjectUtils.asString(param.getValue())));
            }
        }


        return requestParams;
    }
}
