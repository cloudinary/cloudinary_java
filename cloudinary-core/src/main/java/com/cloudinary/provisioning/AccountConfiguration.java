package com.cloudinary.provisioning;

import com.cloudinary.utils.StringUtils;

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
        if (StringUtils.isBlank(accountId)) throw new IllegalArgumentException("Account id must be provided in account url");

        if (uri.getUserInfo() == null) throw new IllegalArgumentException("Full credentials (key+secret) must be provided in account url");
        String[] credentials = uri.getUserInfo().split(":");
        if (credentials.length < 2 ||
                StringUtils.isBlank(credentials[0]) ||
                StringUtils.isBlank(credentials[1])) {
            throw new IllegalArgumentException("Full credentials (key+secret) must be provided in account url");
        }

        return new AccountConfiguration(accountId, credentials[0], credentials[1]);
    }
}
