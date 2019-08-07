package com.cloudinary.provisioning;

import java.net.URI;

public class AccountConfiguration {
    private static final String SEPARATOR = ":";
    String accountId;
    String provisioningApiKey;
    String provisioningApiSecret;

    public AccountConfiguration(String accountId, String provisioningApiKey, String provisioningApiSecret) {
        this.accountId = accountId;
        this.provisioningApiKey = provisioningApiKey;
        this.provisioningApiSecret = provisioningApiSecret;
    }

    public static AccountConfiguration from(String accountUrl) {
        URI uri = URI.create(accountUrl);
        String accountId = uri.getHost();
        String[] creds = uri.getUserInfo().split(":");
        if (creds.length < 2) throw new IllegalArgumentException("Full account information must be provided in account url");
        return new AccountConfiguration(accountId, creds[0], creds[1]);
    }
}
