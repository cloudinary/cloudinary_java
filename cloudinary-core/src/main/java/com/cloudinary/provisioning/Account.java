package com.cloudinary.provisioning;

import com.cloudinary.Api;
import com.cloudinary.Cloudinary;
import com.cloudinary.Util;
import com.cloudinary.api.ApiResponse;
import com.cloudinary.utils.ObjectUtils;
import java.util.*;

/**
 * Entry point class for all account and provisioning API actions: Manage users, cloud names and user groups.
 */
public class Account {
    private static final String CLOUDINARY_ACCOUNT_URL = "CLOUDINARY_ACCOUNT_URL";
    public static final String PROVISIONING = "provisioning";
    public static final String ACCOUNTS = "accounts";
    public static final String SUB_ACCOUNTS = "sub_accounts";
    public static final String USERS = "users";
    public static final String USER_GROUPS = "user_groups";
    public static final String ACCESS_KEYS = "access_keys";

    private final AccountConfiguration configuration;
    private final String accountId;
    private final String key;
    private final String secret;
    private final Api api;

    /**
     * Create a new instance to use the account API. The account information will be extracted from
     * an environment variable CLOUDINARY_ACCOUNT_URL. If it's missing an exception will be thrown.
     *
     * @param cloudinary A cloudinary instance. This is used to fetch the correct network configuration.
     */
    public Account(Cloudinary cloudinary) {
        String provisioningData = System.getProperty(CLOUDINARY_ACCOUNT_URL, System.getenv(CLOUDINARY_ACCOUNT_URL));
        if (provisioningData != null) {
            this.configuration = AccountConfiguration.from(provisioningData);
            this.accountId = configuration.accountId;
            this.key = configuration.provisioningApiKey;
            this.secret = configuration.provisioningApiSecret;
        } else {
            throw new IllegalArgumentException("Must provide configuration instance or set an ENV variable: " +
                    "CLOUDINARY_ACCOUNT_URL=account://provisioning_api_key:provisioning_api_secret@account_id");
        }

        this.api = cloudinary.api();
    }

    /**
     * Create a new instance to use the account API. The account information will be extracted from
     *
     * @param accountConfiguration Account configuration to use in requests.
     * @param cloudinary           A cloudinary instance. This is used to fetch the correct network configuration.
     */
    public Account(AccountConfiguration accountConfiguration, Cloudinary cloudinary) {
        this.configuration = accountConfiguration;
        this.api = cloudinary.api();
        this.accountId = accountConfiguration.accountId;
        this.key = accountConfiguration.provisioningApiKey;
        this.secret = accountConfiguration.provisioningApiSecret;
    }

    private ApiResponse callAccountApi(Api.HttpMethod method, List<String> uri, Map<String, Object> params, Map<String, Object> options) throws Exception {
        options = verifyOptions(options);

        if (options.containsKey("provisioning_api_key")){
            if (!options.containsKey("provisioning_api_secret")){
                throw new IllegalArgumentException("When providing key or secret through options, both must be provided");
            }
        } else {
            if (options.containsKey("provisioning_api_secret")){
                throw new IllegalArgumentException("When providing key or secret through options, both must be provided");
            }
            options.put("provisioning_api_key", key);
            options.put("provisioning_api_secret", secret);
        }

        Util.clearEmpty(params);
        return api.getStrategy().callAccountApi(method, uri, params, options);
    }

    /**
     * A user role to use in the user management API (create/update user).
     */
    public enum Role {
        MASTER_ADMIN("master_admin"),
        ADMIN("admin"),
        TECHNICAL_ADMIN("technical_admin"),
        BILLING("billing"),
        REPORTS("reports"),
        MEDIA_LIBRARY_ADMIN("media_library_admin"),
        MEDIA_LIBRARY_USER("media_library_user");

        private final String serializedValue;

        Role(String serializedValue) {
            this.serializedValue = serializedValue;
        }

        @Override
        public String toString() {
            return serializedValue;
        }
    }

    // Sub accounts
    /**
     * Get details of a specific sub account
     *
     * @param subAccountId The id of the sub account
     * @return the sub account details.
     * @throws Exception If the request fails
     */
    public ApiResponse subAccount(String subAccountId) throws Exception {
        return subAccount(subAccountId, Collections.<String, Object>emptyMap());
    }

    /**
     * Get details of a specific sub account
     *
     * @param subAccountId The id of the sub account
     * @param options      Generic advanced options map, see online documentation.
     * @return the sub account details.
     * @throws Exception If the request fails
     */
    public ApiResponse subAccount(String subAccountId, Map<String, Object> options) throws Exception {
        List<String> uri = Arrays.asList(PROVISIONING, ACCOUNTS, accountId, "sub_accounts", subAccountId);
        return callAccountApi(Api.HttpMethod.GET, uri, Collections.<String, Object>emptyMap(), options);
    }

    /**
     * Get a list of sub accounts.
     *
     * @param enabled Optional. Whether to fetch enabled or disabled accounts. Default is all.
     * @param ids     Optional. List of sub-account IDs. Up to 100. When provided, other filters are ignored.
     * @param prefix  Optional. Search by prefix of the sub-account name. Case-insensitive.
     * @return the list of sub-accounts details.
     * @throws Exception If the request fails
     */
    public ApiResponse subAccounts(Boolean enabled, List<String> ids, String prefix) throws Exception {
        return subAccounts(enabled, ids, prefix, Collections.<String, Object>emptyMap());
    }

    /**
     * Get a list of sub accounts.
     *
     * @param enabled Optional. Whether to fetch enabled or disabled accounts. Default is all.
     * @param ids     Optional. List of sub-account IDs. Up to 100. When provided, other filters are ignored.
     * @param prefix  Optional. Search by prefix of the sub-account name. Case-insensitive.
     * @param options Generic advanced options map, see online documentation.
     * @return the list of sub-accounts details.
     * @throws Exception If the request fails
     */
    public ApiResponse subAccounts(Boolean enabled, List<String> ids, String prefix, Map<String, Object> options) throws Exception {
        List<String> uri = Arrays.asList(PROVISIONING, ACCOUNTS, accountId, "sub_accounts");
        return callAccountApi(Api.HttpMethod.GET, uri,
                ObjectUtils.asMap("accountId", accountId, "enabled", enabled, "ids", ids, "prefix", prefix), options);
    }

    /**
     * @param name             Required. The name displayed in the management console.
     * @param cloudName        Optional, unique (case insensitive)
     * @param customAttributes Custom attributes associated with the sub-account, as a map of key/value pairs.
     * @param enabled          Optional. Whether to create the account as enabled (default is enabled).
     * @param baseAccount      Optional. ID of sub-account from which to copy settings
     * @return details of the created sub-account
     * @throws Exception If the request fails
     */
    public ApiResponse createSubAccount(String name, String cloudName, Map customAttributes, boolean enabled, String baseAccount) throws Exception {
        return createSubAccount(name, cloudName, customAttributes, enabled, baseAccount, Collections.<String, Object>emptyMap());
    }

    /**
     * @param name             Required. The name displayed in the management console.
     * @param cloudName        Optional, unique (case insensitive)
     * @param customAttributes Custom attributes associated with the sub-account, as a map of key/value pairs.
     * @param enabled          Optional. Whether to create the account as enabled (default is enabled).
     * @param baseAccount      Optional. ID of sub-account from which to copy settings
     * @param options          Generic advanced options map, see online documentation.
     * @return details of the created sub-account
     * @throws Exception If the request fails
     */
    public ApiResponse createSubAccount(String name, String cloudName, Map customAttributes, boolean enabled, String baseAccount, Map<String, Object> options) throws Exception {
        options = verifyOptions(options);
        options.put("content_type", "json");

        List<String> uri = Arrays.asList(PROVISIONING, ACCOUNTS, accountId, "sub_accounts");

        return callAccountApi(Api.HttpMethod.POST, uri, ObjectUtils.asMap(
                "cloud_name", cloudName,
                "name", name,
                "custom_attributes", customAttributes,
                "enabled", enabled,
                "base_sub_account_id", baseAccount),
                options);
    }

    /**
     * @param subAccountId     The id of the sub-account to update
     * @param name             The name displayed in the management console.
     * @param cloudName        The cloud name to set.
     * @param customAttributes ACustom attributes associated with the sub-account, as a map of key/value pairs.
     * @param enabled          Set the sub-account as enabled or not.
     * @return details of the updated sub-account
     * @throws Exception If the request fails
     */
    public ApiResponse updateSubAccount(String subAccountId, String name, String cloudName, Map<String, String> customAttributes, Boolean enabled) throws Exception {
        return updateSubAccount(subAccountId, name, cloudName, customAttributes, enabled, Collections.<String, Object>emptyMap());
    }

    /**
     * @param subAccountId     The id of the sub-account to update
     * @param name             The name displayed in the management console.
     * @param cloudName        The cloud name to set.
     * @param customAttributes ACustom attributes associated with the sub-account, as a map of key/value pairs.
     * @param enabled          Set the sub-account as enabled or not.
     * @param options          Generic advanced options map, see online documentation.
     * @return details of the updated sub-account
     * @throws Exception If the request fails
     */
    public ApiResponse updateSubAccount(String subAccountId, String name, String cloudName, Map<String, String> customAttributes, Boolean enabled, Map<String, Object> options) throws Exception {
        options = verifyOptions(options);
        options.put("content_type", "json");

        List<String> uri = Arrays.asList(PROVISIONING, ACCOUNTS, accountId, "sub_accounts", subAccountId);

        return callAccountApi(Api.HttpMethod.PUT, uri, ObjectUtils.asMap(
                "cloud_name", cloudName,
                "name", name,
                "custom_attributes", customAttributes,
                "enabled", enabled),
                options);
    }

    /**
     * Deletes the sub-account.
     *
     * @param subAccountId The id of the sub-account to delete
     * @return result message.
     * @throws Exception If the request fails.
     */
    public ApiResponse deleteSubAccount(String subAccountId) throws Exception {
        return deleteSubAccount(subAccountId, Collections.<String, Object>emptyMap());
    }

    /**
     * Deletes the sub-account.
     *
     * @param subAccountId The id of the sub-account to delete
     * @param options      Generic advanced options map, see online documentation.
     * @return result message.
     * @throws Exception If the request fails.
     */
    public ApiResponse deleteSubAccount(String subAccountId, Map<String, Object> options) throws Exception {
        List<String> uri = Arrays.asList(PROVISIONING, ACCOUNTS, accountId, "sub_accounts", subAccountId);
        return callAccountApi(Api.HttpMethod.DELETE, uri, Collections.<String, Object>emptyMap(), options);
    }

    // Users
    /**
     * Get details of a specific user.
     *
     * @param userId  The id of the user to fetch
     * @return details of the user.
     * @throws Exception If the request fails.
     */
    public ApiResponse user(String userId) throws Exception {
        return user(userId,null);
    }

    /**
     * Get details of a specific user.
     *
     * @param userId  The id of the user to fetch
     * @param options Generic advanced options map, see online documentation.
     * @return details of the user.
     * @throws Exception If the request fails.
     */
    public ApiResponse user(String userId, Map<String, Object> options) throws Exception {
        List<String> uri = Arrays.asList(PROVISIONING, ACCOUNTS, accountId, USERS, userId);
        return callAccountApi(Api.HttpMethod.GET, uri, Collections.<String, Object>emptyMap(), options);
    }

    /**
     * Get a list of the users according to filters.
     *
     * @param pending      Optional. Limit results to pending users (true), users that are not pending (false), or all users (null)
     * @param userIds      Optionals. List of user IDs. Up to 100
     * @param prefix       Optional. Search by prefix of the user's name or email. Case-insensitive
     * @param subAccountId Optional. Return only users who have access to the given sub-account
     * @return the users' details.
     * @throws Exception If the request fails.
     */
    public ApiResponse users(Boolean pending, List<String> userIds, String prefix, String subAccountId) throws Exception {
        return users(pending, userIds, prefix, subAccountId,null);
    }

    /**
     * Get a list of the users according to filters.
     *
     * @param pending      Optional. Limit results to pending users (true), users that are not pending (false), or all users (null)
     * @param userIds      Optionals. List of user IDs. Up to 100
     * @param prefix       Optional. Search by prefix of the user's name or email. Case-insensitive
     * @param subAccountId Optional. Return only users who have access to the given sub-account
     * @param options      Generic advanced options map, see online documentation.
     * @return the users' details.
     * @throws Exception If the request fails.
     */
    public ApiResponse users(Boolean pending, List<String> userIds, String prefix, String subAccountId, Map<String, Object> options) throws Exception {
        List<String> uri = Arrays.asList(PROVISIONING, ACCOUNTS, accountId, USERS);
        return callAccountApi(Api.HttpMethod.GET, uri,
                ObjectUtils.asMap("accountId", accountId,
                        "pending", pending,
                        "ids", userIds,
                        "prefix", prefix,
                        "sub_account_id", subAccountId), options);
    }

    /**
     * Create a new user.
     *
     * @param name           Required. Username.
     * @param email          Required. User's email.
     * @param role           Required. User's role.
     * @param subAccountsIds Optional. Sub-accounts for which the user should have access.
     *                       If not provided or empty, user should have access to all accounts.
     * @return The newly created user details.
     * @throws Exception If the request fails.
     */
    public ApiResponse createUser(String name, String email, Role role, List<String> subAccountsIds) throws Exception {
        return createUser(name, email, role, subAccountsIds, null);
    }

    /**
     * Create a new user.
     *
     * @param name           Required. Username.
     * @param email          Required. User's email.
     * @param role           Required. User's role.
     * @param subAccountsIds Optional. Sub-accounts for which the user should have access.
     *                       If not provided or empty, user should have access to all accounts.
     * @param options        Generic advanced options map, see online documentation.
     * @return The newly created user details.
     * @throws Exception If the request fails.
     */
    public ApiResponse createUser(String name, String email, Role role, List<String> subAccountsIds, Map<String, Object> options) throws Exception {
        return createUser(name, email, role, null, subAccountsIds, options);
    }

    /**
     * Create a new user.
     *
     * @param name           Required. Username.
     * @param email          Required. User's email.
     * @param role           Required. User's role.
     * @param enabled        Optional. User's status (enabled or disabled).
     * @param subAccountsIds Optional. Sub-accounts for which the user should have access.
     *                       If not provided or empty, user should have access to all accounts.
     * @param options        Generic advanced options map, see online documentation.
     * @return The newly created user details.
     * @throws Exception If the request fails.
     */
    public ApiResponse createUser(String name, String email, Role role, Boolean enabled, List<String> subAccountsIds, Map<String, Object> options) throws Exception {
        List<String> uri = Arrays.asList(PROVISIONING, ACCOUNTS, accountId, USERS);
        return performUserAction(Api.HttpMethod.POST, uri, email, name, role, enabled, subAccountsIds, options);
    }

    /**
     * Update an existing user.
     *
     * @param userId         The id of the user to update.
     * @param name           Username.
     * @param email          User's email.
     * @param role           User's role.
     * @param subAccountsIds Sub-accounts for which the user should have access.
     *                       If not provided or empty, user should have access to all accounts.
     * @return The updated user details
     * @throws Exception If the request fails.
     */
    public ApiResponse updateUser(String userId, String name, String email, Role role, List<String> subAccountsIds) throws Exception {
        return updateUser(userId, name, email, role, subAccountsIds,null);
    }

    /**
     * Update an existing user.
     *
     * @param userId         The id of the user to update.
     * @param name           Username.
     * @param email          User's email.
     * @param role           User's role.
     * @param subAccountsIds Sub-accounts for which the user should have access.
     *                       If not provided or empty, user should have access to all accounts.
     * @param options        Generic advanced options map, see online documentation.
     * @return The updated user details
     * @throws Exception If the request fails.
     */
    public ApiResponse updateUser(String userId, String name, String email, Role role, List<String> subAccountsIds, Map<String, Object> options) throws Exception {
        return updateUser(userId, name ,email ,role ,null , subAccountsIds , options);
    }

    /**
     * Update an existing user.
     *
     * @param userId         The id of the user to update.
     * @param name           Username.
     * @param email          User's email.
     * @param role           User's role.
     * @param enabled        User's status (enabled or disabled)
     * @param subAccountsIds Sub-accounts for which the user should have access.
     *                       If not provided or empty, user should have access to all accounts.
     * @param options        Generic advanced options map, see online documentation.
     * @return The updated user details
     * @throws Exception If the request fails.
     */
    public ApiResponse updateUser(String userId, String name, String email, Role role, Boolean enabled, List<String> subAccountsIds, Map<String, Object> options) throws Exception {
        List<String> uri = Arrays.asList(PROVISIONING, ACCOUNTS, accountId, USERS, userId);
        return performUserAction(Api.HttpMethod.PUT, uri, email, name, role, enabled, subAccountsIds, options);
    }

    /**
     * Delete a user.
     *
     * @param userId  Id of the user to delete.
     * @return result message.
     * @throws Exception
     */
    public ApiResponse deleteUser(String userId) throws Exception {
        return deleteUser(userId,null);
    }

    /**
     * Delete a user.
     *
     * @param userId  Id of the user to delete.
     * @param options Generic advanced options map, see online documentation.
     * @return result message.
     * @throws Exception
     */
    public ApiResponse deleteUser(String userId, Map<String, Object> options) throws Exception {
        List<String> uri = Arrays.asList(PROVISIONING, ACCOUNTS, accountId, USERS, userId);
        return callAccountApi(Api.HttpMethod.DELETE, uri, Collections.<String, Object>emptyMap(), options);
    }

    // Groups
    /**
     * Create a new user group
     * @param name Required. Name for the group.
     * @return The newly created group.
     * @throws Exception If the request fails
     */
    public ApiResponse createUserGroup(String name) throws Exception {
        return createUserGroup(name,null);
    }

    /**
     * Create a new user group
     * @param name Required. Name for the group.
     * @param options Generic advanced options map, see online documentation.
     * @return The newly created group.
     * @throws Exception If the request fails
     */
    public ApiResponse createUserGroup(String name, Map<String, Object> options) throws Exception {
        List<String> uri = Arrays.asList(PROVISIONING, ACCOUNTS, accountId, USER_GROUPS);
        return callAccountApi(Api.HttpMethod.POST, uri, ObjectUtils.asMap("name", name), options);
    }

    /**
     * Update an existing user group
     *
     * @param groupId The id of the group to update
     * @param name The name of the group.
     * @return The updated group.
     * @throws Exception If the request fails
     */
    public ApiResponse updateUserGroup(String groupId, String name) throws Exception {
        return updateUserGroup(groupId, name,null);
    }

    /**
     * Update an existing user group
     *
     * @param groupId The id of the group to update
     * @param name The name of the group.
     * @param options Generic advanced options map, see online documentation.
     * @return The updated group.
     * @throws Exception If the request fails
     */
    public ApiResponse updateUserGroup(String groupId, String name, Map<String, Object> options) throws Exception {
        List<String> uri = Arrays.asList(PROVISIONING, ACCOUNTS, accountId, USER_GROUPS, groupId);
        return callAccountApi(Api.HttpMethod.PUT, uri, ObjectUtils.asMap("name", name), options);
    }

    /**
     * Delete a user group
     *
     * @param groupId The group id to delete
     * @return A result message.
     * @throws Exception if the request fails.
     */
    public ApiResponse deleteUserGroup(String groupId) throws Exception {
        return deleteUserGroup(groupId,null);
    }

    /**
     * Delete a user group
     *
     * @param groupId The group id to delete
     * @param options Generic advanced options map, see online documentation.
     * @return A result message.
     * @throws Exception if the request fails.
     */
    public ApiResponse deleteUserGroup(String groupId, Map<String, Object> options) throws Exception {
        List<String> uri = Arrays.asList(PROVISIONING, ACCOUNTS, accountId, USER_GROUPS, groupId);
        return callAccountApi(Api.HttpMethod.DELETE, uri, Collections.<String, Object>emptyMap(), options);
    }

    /**
     * Add an existing user to a group.
     * @param groupId The group id.
     * @param userId The user id to add.
     * @throws Exception If the request fails
     */
    public ApiResponse addUserToGroup(String groupId, String userId) throws Exception {
        return addUserToGroup(groupId, userId,null);
    }
    /**
     * Add an existing user to a group.
     * @param groupId The group id.
     * @param userId The user id to add.
     * @param options Generic advanced options map, see online documentation.
     * @throws Exception If the request fails
     */
    public ApiResponse addUserToGroup(String groupId, String userId, Map<String, Object> options) throws Exception {
        List<String> uri = Arrays.asList(PROVISIONING, ACCOUNTS, accountId, USER_GROUPS, groupId, USERS, userId);
        return callAccountApi(Api.HttpMethod.POST, uri, Collections.<String, Object>emptyMap(), options);
    }

    /**
     * Removes a user from a group.
     * @param groupId The group id.
     * @param userId The id of the user to remove
     * @return A result message
     * @throws Exception If the request fails.
     */
    public ApiResponse removeUserFromGroup(String groupId, String userId) throws Exception {
        return removeUserFromGroup(groupId, userId,null);
    }
    /**
     * Removes a user from a group.
     * @param groupId The group id.
     * @param userId The id of the user to remove
     * @param options Generic advanced options map, see online documentation.
     * @return A result message
     * @throws Exception If the request fails.
     */
    public ApiResponse removeUserFromGroup(String groupId, String userId, Map<String, Object> options) throws Exception {
        List<String> uri = Arrays.asList(PROVISIONING, ACCOUNTS, accountId, USER_GROUPS, groupId, USERS, userId);
        return callAccountApi(Api.HttpMethod.DELETE, uri, Collections.<String, Object>emptyMap(), options);
    }

    /**
     * Get details of a group.
     * @param groupId The group id to fetch
     * @return Details of the group.
     * @throws Exception If the request fails.
     */
    public ApiResponse userGroup(String groupId) throws Exception {
        return userGroup(groupId,null);
    }

    /**
     * Get details of a group.
     * @param groupId The group id to fetch
     * @param options Generic advanced options map, see online documentation.
     * @return Details of the group.
     * @throws Exception If the request fails.
     */
    public ApiResponse userGroup(String groupId, Map<String, Object> options) throws Exception {
        List<String> uri = Arrays.asList(PROVISIONING, ACCOUNTS, accountId, USER_GROUPS, groupId);
        return callAccountApi(Api.HttpMethod.GET, uri, Collections.<String, Object>emptyMap(), options);
    }

    /**
     * Gets a list of all the user groups.
     * @return The list of the groups.
     * @throws Exception If the request fails.
     */
    public ApiResponse userGroups() throws Exception {
        return userGroups(Collections.<String, Object>emptyMap());
    }

    /**
     * Gets a list of all the user groups.
     * @param options Generic advanced options map, see online documentation.
     * @return The list of the groups.
     * @throws Exception If the request fails.
     */
    public ApiResponse userGroups(Map<String, Object> options) throws Exception {
        List<String> uri = Arrays.asList(PROVISIONING, ACCOUNTS, accountId, USER_GROUPS);
        return callAccountApi(Api.HttpMethod.GET, uri, Collections.<String, Object>emptyMap(), options);
    }

    /**
     * Lists the users belonging to this user group.
     * @param groupId The id of the user group.
     * @return The list of users in that group.
     * @throws Exception If the request fails.
     */
    public ApiResponse userGroupUsers(String groupId) throws Exception {
        return userGroupUsers(groupId,null);
    }
    /**
     * Lists the users belonging to this user group.
     * @param groupId The id of the user group.
     * @param options Generic advanced options map, see online documentation.
     * @return The list of users in that group.
     * @throws Exception If the request fails.
     */
    public ApiResponse userGroupUsers(String groupId, Map<String, Object> options) throws Exception {
        List<String> uri = Arrays.asList(PROVISIONING, ACCOUNTS, accountId, USER_GROUPS, groupId, USERS);
        return callAccountApi(Api.HttpMethod.GET, uri, Collections.<String, Object>emptyMap(), options);
    }

    public ApiResponse getAccessKeys(String subAccountId, Map<String, Object> options) throws Exception {
        List<String> uri = Arrays.asList(PROVISIONING, ACCOUNTS, accountId, SUB_ACCOUNTS, subAccountId);
        return callAccountApi(Api.HttpMethod.GET, uri, Collections.<String, Object>emptyMap(), options);
    }

    public ApiResponse createAccessKey(String subAccountId, String name, Boolean enabled, Map<String, Object> options)  throws Exception {
        List<String> uri = Arrays.asList(PROVISIONING, ACCOUNTS, accountId, SUB_ACCOUNTS, subAccountId, ACCESS_KEYS);
        return callAccountApi(Api.HttpMethod.POST, uri, ObjectUtils.asMap("name", name, "enabled", enabled), options);
    }

    public ApiResponse updateAccessKey(String subAccountId, String accessKey, String name, Boolean enabled,Map<String, Object> options) throws Exception {
        List<String> uri = Arrays.asList(PROVISIONING, ACCOUNTS, accountId, SUB_ACCOUNTS, subAccountId, ACCESS_KEYS, accessKey);
        return callAccountApi(Api.HttpMethod.PUT, uri, ObjectUtils.asMap("name", name, "enabled", enabled), options);
    }

    /**
     * Private helper method for users api calls
     * @param method Http method
     * @param uri Uri to call
     * @param email user email
     * @param name user name
     * @param role user role
     * @param subAccountsIds suv accounts ids the user has access to.
     * @param options
     * @return The response of the api call.
     * @throws Exception If the request fails.
     */
    private ApiResponse performUserAction(Api.HttpMethod method, List<String> uri, String email, String name, Role role, Boolean enabled, List<String> subAccountsIds, Map<String, Object> options) throws Exception {
        options = verifyOptions(options);
        options.put("content_type", "json");

        return callAccountApi(method, uri, ObjectUtils.asMap(
                "email", email,
                "name", name,
                "role", role == null ? null : role.serializedValue,
                "enabled", enabled,
                "sub_account_ids", subAccountsIds),
                options);
    }

    private Map<String, Object> verifyOptions(Map<String, Object> options) {
        if (options == null || options == Collections.EMPTY_MAP) {
            return new HashMap<String, Object>(2); // Two, since api key and secret will be populated later
        }

        return options;
    }
}
