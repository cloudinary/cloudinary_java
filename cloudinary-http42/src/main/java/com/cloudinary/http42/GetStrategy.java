package com.cloudinary.http42;

import com.cloudinary.Cloudinary;
import com.cloudinary.strategies.AbstractGetStrategy;
import com.cloudinary.utils.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.io.InputStream;

public class GetStrategy extends AbstractGetStrategy {

    @Override
    public String get(String apiUrl) throws IOException {

        ClientConnectionManager connectionManager = (ClientConnectionManager) cloudinary.config.properties.get("connectionManager");
        HttpClient client = new DefaultHttpClient(connectionManager);

        // If the configuration specifies a proxy then apply it to the client
        if (cloudinary.config.proxyHost != null && cloudinary.config.proxyPort != 0) {
            HttpHost proxy = new HttpHost(cloudinary.config.proxyHost, cloudinary.config.proxyPort);
            client.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
        }

        HttpGet getMethod = new HttpGet(apiUrl);
        getMethod.setHeader("User-Agent", Cloudinary.USER_AGENT + " ApacheHTTPComponents/4.2");
        HttpResponse response = client.execute(getMethod);
        int code = response.getStatusLine().getStatusCode();

        // TODO
        if (code == 200) {
            InputStream responseStream = response.getEntity().getContent();
            String responseData = StringUtils.read(responseStream);
            return responseData;
        }

        return null;
    }
}
