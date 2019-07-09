package com.cloudinary.provisioning;

public class Configuration {
    private static final String SEPERATOR = ":";
    String accountId;
    String provisioningApiKey;
    String provisioningApiSecret;
    private int timeout = 0;

    public Configuration(String accountId, String provisioningApiKey, String provisioningApiSecret) {
        this.accountId = accountId;
        this.provisioningApiKey = provisioningApiKey;
        this.provisioningApiSecret = provisioningApiSecret;
    }

    public static Configuration from(String provisioningData) {
        String[] split = provisioningData.split(SEPERATOR);
        return new Configuration(split[0], split[1], split[2]);
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
}
