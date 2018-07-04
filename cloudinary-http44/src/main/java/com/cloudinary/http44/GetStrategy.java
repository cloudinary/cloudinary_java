package com.cloudinary.http44;

import com.cloudinary.Cloudinary;
import com.cloudinary.strategies.AbstractGetStrategy;
import com.cloudinary.utils.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.io.InputStream;

public class GetStrategy extends AbstractGetStrategy {

    private CloseableHttpClient client = null;

    @Override
    public void init(Cloudinary cloudinary) {
        super.init(cloudinary);

        HttpClientBuilder clientBuilder = HttpClients.custom();
        clientBuilder.useSystemProperties().setUserAgent(Cloudinary.USER_AGENT + " ApacheHTTPComponents/4.4");

        // If the configuration specifies a proxy then apply it to the client
        if (cloudinary.config.proxyHost != null && cloudinary.config.proxyPort != 0) {
            HttpHost proxy = new HttpHost(cloudinary.config.proxyHost, cloudinary.config.proxyPort);
            clientBuilder.setProxy(proxy);
        }

        HttpClientConnectionManager connectionManager = (HttpClientConnectionManager) cloudinary.config.properties.get("connectionManager");
        if (connectionManager != null) {
            clientBuilder.setConnectionManager(connectionManager);
        }

        this.client = clientBuilder.build();
    }

    @Override
    public String get(String url) throws IOException {
        HttpGet getMethod = new HttpGet(url);
        int code = 0;
        CloseableHttpResponse response = client.execute(getMethod);
        try {
            code = response.getStatusLine().getStatusCode();

            if (code == 200) {
                InputStream responseStream = response.getEntity().getContent();
                return StringUtils.read(responseStream);
            }
        } finally {
            response.close();
        }

        return null;
    }
}
