package com.cloudinary.provisioning;

import com.cloudinary.Api;
import com.cloudinary.Cloudinary;
import com.cloudinary.api.ApiResponse;
import com.cloudinary.utils.ObjectUtils;

import java.util.*;

public class Account {
    private final Configuration configuration;


    private static final String SUB_ACCOUNTS = "sub_accounts";
    private static final String USERS = "users";
    private static final String USER_GROUPS = "user_groups";
    private final String PROVISIONING = "provisioning";
    private final String ACCOUNTS = "accounts";
    private final String accountId;
    private final String key;
    private final String secret;
    private final Api api;

    public Account(Cloudinary cloudinary) {
        String provisioningData = System.getProperty("CLOUDINARY_PROVISIONING_CONFIG", System.getenv("CLOUDINARY_PROVISIONING_CONFIG"));
        if (provisioningData != null) {
            this.configuration = Configuration.from(provisioningData);
            this.accountId = configuration.accountId;
            this.key = configuration.provisioningApiKey;
            this.secret = configuration.provisioningApiSecret;
        } else {
            throw new IllegalArgumentException("Must provide configuration instance or set an ENV variable: CLOUDINARY_PROVISIONING_CONFIG=account_id:key:secret");
        }
        
        this.api = cloudinary.api();
    }

    public Account(Configuration configuration, Cloudinary cloudinary) {
        this.configuration = configuration;
        this.api = cloudinary.api();
        this.accountId = configuration.accountId;
        this.key = configuration.provisioningApiKey;
        this.secret = configuration.provisioningApiSecret;
    }

    private ApiResponse callAccountApi(Api.HttpMethod method, List<String> uri, Map<String, Object> params, Map<String, Object> options) throws Exception {
        options = verifyOptions(options);

        if (!options.containsKey("provisioning_api_key")) {
            options.put("provisioning_api_key", key);
            options.put("provisioning_api_secret", secret);
        }

        return api.getStrategy().callAccountApi(method, uri, params, options);
    }


    // TODO verify current list of roles
    public enum Role {
        MASTER_ADMIN("master_admin"),
        ADMIN("admin"),
        BILLING("billing"),
        TECHNICAL_ADMIN("technical_admin"),
        EDITOR("editor");

        private final String value;

        Role(String value) {
            this.value = value;
        }


        @Override
        public String toString() {
            return value;
        }
    }

    // Sub accounts
    public ApiResponse getSubAccount(String subAccountId, Map<String, Object> options) throws Exception {
        List<String> uri = Arrays.asList(PROVISIONING, ACCOUNTS, accountId, SUB_ACCOUNTS, subAccountId);
        return callAccountApi(Api.HttpMethod.GET, uri, Collections.<String, Object>emptyMap(), options);
    }
    
    public ApiResponse getSubAccounts(Boolean enabled, List<String> ids, String prefix, Map<String, Object> options) throws Exception {
        List<String> uri = Arrays.asList(PROVISIONING, ACCOUNTS, accountId, SUB_ACCOUNTS);
        return callAccountApi(Api.HttpMethod.GET, uri,
                ObjectUtils.asMap("accountId", accountId, "enabled", enabled, "ids", ids, "prefix", prefix), options);
    }

    public ApiResponse createSubAccount(String newCloudName, String name, Map customAttributes, boolean enabled, String baseAccount, Map<String, Object> options) throws Exception {
        options = verifyOptions(options);
        options.put("content_type", "json");

        List<String> uri = Arrays.asList(PROVISIONING, ACCOUNTS, accountId, SUB_ACCOUNTS);

        return callAccountApi(Api.HttpMethod.POST, uri, ObjectUtils.asMap(
                "cloud_name", newCloudName,
                "name", name,
                "custom_attributes", customAttributes,
                "enabled", enabled,
                "from_base_account", baseAccount),
                options);
    }

    public ApiResponse updateSubAccount(String subAccountId, String cloudName, String name, Map<String, String> customAttributes, Boolean enabled, Map<String, Object> options) throws Exception {
        options = verifyOptions(options);
        options.put("content_type", "json");

        List<String> uri = Arrays.asList(PROVISIONING, ACCOUNTS, accountId, SUB_ACCOUNTS, subAccountId);

        return callAccountApi(Api.HttpMethod.PUT, uri, ObjectUtils.asMap(
                "cloud_name", cloudName,
                "name", name,
                "custom_attributes", customAttributes,
                "enabled", enabled),
                options);
    }

    public ApiResponse deleteSubAccount(String subAccountId, Map<String, Object> options) throws Exception {
        List<String> uri = Arrays.asList(PROVISIONING, ACCOUNTS, accountId, SUB_ACCOUNTS, subAccountId);
        return callAccountApi(Api.HttpMethod.DELETE, uri, Collections.<String, Object>emptyMap(), options);
    }

    // Users
    public ApiResponse getUser(String userId, Map<String, Object> options) throws Exception {
        List<String> uri = Arrays.asList(PROVISIONING, ACCOUNTS, accountId, USERS, userId);
        return callAccountApi(Api.HttpMethod.GET, uri, Collections.<String, Object>emptyMap(), options);
    }

    public ApiResponse getUsers(Boolean pending, List<String> emails, String prefix, String subAccountId, Role role, String since, Map<String, Object> options) throws Exception {
        List<String> uri = Arrays.asList(PROVISIONING, ACCOUNTS, accountId, USERS);
        return callAccountApi(Api.HttpMethod.GET, uri,
                ObjectUtils.asMap("accountId", accountId,
                        "pending", pending,
                        "emails", emails,
                        "prefix", prefix,
                        "sub_account_id", subAccountId,
                        "role", role,
                        "since", since), options);
    }

    public ApiResponse createUser(String email, String name, Role role, List<String> subAccountsIds, Map<String, Object> options) throws Exception {
        List<String> uri = Arrays.asList(PROVISIONING, ACCOUNTS, accountId, USERS);
        return performUserAction(Api.HttpMethod.POST, uri, email, name, role, subAccountsIds, options);
    }

    public ApiResponse updateUser(String userId, String email, String name, Role role, List<String> subAccountsIds, Map<String, Object> options) throws Exception {
        List<String> uri = Arrays.asList(PROVISIONING, ACCOUNTS, accountId, USERS, userId);
        return performUserAction(Api.HttpMethod.PUT, uri, email, name, role, subAccountsIds, options);
    }

    public ApiResponse deleteUser(String userId, Map<String, Object> options) throws Exception {
        List<String> uri = Arrays.asList(PROVISIONING, ACCOUNTS, accountId, USERS, userId);
        return callAccountApi(Api.HttpMethod.DELETE, uri, Collections.<String, Object>emptyMap(), options);
    }

    // Groups
    public ApiResponse createUserGroup(String name, Map<String, Object> options) throws Exception {
        List<String> uri = Arrays.asList(PROVISIONING, ACCOUNTS, accountId, USER_GROUPS);
        return callAccountApi(Api.HttpMethod.POST, uri, ObjectUtils.asMap("name", name), options);
    }

    public ApiResponse updateUserGroup(String groupId, String name, Map<String, Object> options) throws Exception {
        List<String> uri = Arrays.asList(PROVISIONING, ACCOUNTS, accountId, USER_GROUPS, groupId);
        return callAccountApi(Api.HttpMethod.PUT, uri, ObjectUtils.asMap("name", name), options);
    }

    public ApiResponse deleteUserGroup(String groupId, Map<String, Object> options) throws Exception {
        List<String> uri = Arrays.asList(PROVISIONING, ACCOUNTS, accountId, USER_GROUPS, groupId);
        return callAccountApi(Api.HttpMethod.DELETE, uri, Collections.<String, Object>emptyMap(), options);
    }

    public ApiResponse addUserToGroup(String groupId, String userId, Map<String, Object> options) throws Exception {
        List<String> uri = Arrays.asList(PROVISIONING, ACCOUNTS, accountId, USER_GROUPS, groupId, USERS, userId);
        return callAccountApi(Api.HttpMethod.POST, uri, Collections.<String, Object>emptyMap(), options);
    }

    public ApiResponse removeUserFromGroup(String groupId, String userId, Map<String, Object> options) throws Exception {
        List<String> uri = Arrays.asList(PROVISIONING, ACCOUNTS, accountId, USER_GROUPS, groupId, USERS, userId);
        return callAccountApi(Api.HttpMethod.DELETE, uri, Collections.<String, Object>emptyMap(), options);
    }

    public ApiResponse getUserGroup(String groupId, Map<String, Object> options) throws Exception {
        List<String> uri = Arrays.asList(PROVISIONING, ACCOUNTS, accountId, USER_GROUPS, groupId);
        return callAccountApi(Api.HttpMethod.GET, uri, Collections.<String, Object>emptyMap(), options);
    }

    public ApiResponse listUserGroups(Map<String, Object> options) throws Exception {
        List<String> uri = Arrays.asList(PROVISIONING, ACCOUNTS, accountId, USER_GROUPS);
        return callAccountApi(Api.HttpMethod.GET, uri, Collections.<String, Object>emptyMap(), options);
    }

    public ApiResponse listUserGroupUsers(String groupId, Map<String, Object> options) throws Exception {
        List<String> uri = Arrays.asList(PROVISIONING, ACCOUNTS, accountId, USER_GROUPS, groupId, USERS);
        return callAccountApi(Api.HttpMethod.GET, uri, Collections.<String, Object>emptyMap(), options);
    }

    private ApiResponse performUserAction(Api.HttpMethod method, List<String> uri, String email, String name, Role role, List<String> subAccountsIds, Map<String, Object> options) throws Exception {
        options = verifyOptions(options);
        options.put("content_type", "json");

        return callAccountApi(method, uri, ObjectUtils.asMap(
                "email", email,
                "name", name,
                "role", role == null ? null : role.value,
                "sub_account_ids", subAccountsIds),
                options);
    }

    private Map<String, Object> verifyOptions(Map<String, Object> options) {
        if (options == null) {
            return new HashMap<String, Object>(2); // Two, since api key and secret will be populated later
        }

        return options;
    }
}