package com.cloudinary.provisioning;

import com.cloudinary.Api;
import com.cloudinary.api.RateLimit;
import com.cloudinary.api.exceptions.GeneralError;
import com.cloudinary.utils.Base64Coder;
import com.cloudinary.utils.ObjectUtils;
import com.cloudinary.utils.StringUtils;
import org.cloudinary.json.JSONException;
import org.cloudinary.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.ParseException;
import java.util.*;

public class Account {
    private final Configuration configuration;


    public static final String SUB_ACCOUNTS = "sub_accounts";
    public static final String USERS = "users";
    public static final String USER_GROUPS = "user_groups";
    private final String PROVISIONING = "provisioning";
    private final String ACCOUNTS = "accounts";
    private final String accountId;
    private final String key;
    private final String secret;

    public Account() {
        String provisioningData = System.getProperty("CLOUDINARY_PROVISIONING_CONFIG", System.getenv("CLOUDINARY_PROVISIONING_CONFIG"));
        if (provisioningData != null) {
            this.configuration = Configuration.from(provisioningData);
            this.accountId = configuration.accountId;
            this.key = configuration.provisioningApiKey;
            this.secret = configuration.provisioningApiSecret;
        } else {
            throw new IllegalArgumentException("Must provide configuration instance or set an ENV variable: CLOUDINARY_PROVISIONING_CONFIG=account_id:key:secret");
        }
    }

    public Account(Configuration configuration) {
        this.configuration = configuration;
        this.accountId = configuration.accountId;
        this.key = configuration.provisioningApiKey;
        this.secret = configuration.provisioningApiSecret;
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
    public ApiResponse getSubAccount(String subAccountId, Map options) throws Exception {
        List<String> uri = Arrays.asList(PROVISIONING, ACCOUNTS, accountId, SUB_ACCOUNTS, subAccountId);
        return callAccountApi(Api.HttpMethod.GET, uri, Collections.emptyMap(), options);
    }

    public ApiResponse getSubAccounts(Boolean enabled, List<String> ids, String prefix, Map options) throws Exception {
        List<String> uri = Arrays.asList(PROVISIONING, ACCOUNTS, accountId, SUB_ACCOUNTS);
        return callAccountApi(Api.HttpMethod.GET, uri,
                ObjectUtils.asMap("accountId", accountId, "enabled", enabled, "ids", ids, "prefix", prefix), options);
    }

    public ApiResponse createSubAccount(String newCloudName, String name, Map customAttributes, boolean enabled, String baseAccount, Map options) throws Exception {
        if (options == null) {
            options = new HashMap();
        }

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

    public ApiResponse updateSubAccount(String subAccountId, String cloudName, String name, Map<String, String> customAttributes, Boolean enabled, Map options) throws Exception {
        if (options == null) {
            options = new HashMap();
        }

        options.put("content_type", "json");

        List<String> uri = Arrays.asList(PROVISIONING, ACCOUNTS, accountId, SUB_ACCOUNTS, subAccountId);

        return callAccountApi(Api.HttpMethod.PUT, uri, ObjectUtils.asMap(
                "cloud_name", cloudName,
                "name", name,
                "custom_attributes", customAttributes,
                "enabled", enabled),
                options);
    }

    public ApiResponse deleteSubAccount(String subAccountId, Map options) throws Exception {
        List<String> uri = Arrays.asList(PROVISIONING, ACCOUNTS, accountId, SUB_ACCOUNTS, subAccountId);
        return callAccountApi(Api.HttpMethod.DELETE, uri, Collections.emptyMap(), options);
    }

    // Users
    public ApiResponse getUser(String userId, Map options) throws Exception {
        List<String> uri = Arrays.asList(PROVISIONING, ACCOUNTS, accountId, USERS, userId);
        return callAccountApi(Api.HttpMethod.GET, uri, Collections.emptyMap(), options);
    }

    public ApiResponse getUsers(Boolean pending, List<String> emails, String prefix, String subAccountId, Role role, String since, Map options) throws Exception {
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

    public ApiResponse createUser(String email, String name, Role role, List<String> subAccountsIds, Map options) throws Exception {
        List<String> uri = Arrays.asList(PROVISIONING, ACCOUNTS, accountId, USERS);
        return performUserAction(Api.HttpMethod.POST, uri, email, name, role, subAccountsIds, options);
    }

    public ApiResponse updateUser(String userId, String email, String name, Role role, List<String> subAccountsIds, Map options) throws Exception {
        List<String> uri = Arrays.asList(PROVISIONING, ACCOUNTS, accountId, USERS, userId);
        return performUserAction(Api.HttpMethod.PUT, uri, email, name, role, subAccountsIds, options);
    }

    public ApiResponse deleteUser(String userId, Map options) throws Exception {
        List<String> uri = Arrays.asList(PROVISIONING, ACCOUNTS, accountId, USERS, userId);
        return callAccountApi(Api.HttpMethod.DELETE, uri, Collections.emptyMap(), options);
    }

    // Groups
    public ApiResponse createUserGroup(String name, Map options) throws Exception {
        List<String> uri = Arrays.asList(PROVISIONING, ACCOUNTS, accountId, USER_GROUPS);
        return callAccountApi(Api.HttpMethod.POST, uri, ObjectUtils.asMap("name", name), options);
    }

    public ApiResponse updateUserGroup(String groupId, String name, Map options) throws Exception {
        List<String> uri = Arrays.asList(PROVISIONING, ACCOUNTS, accountId, USER_GROUPS, groupId);
        return callAccountApi(Api.HttpMethod.PUT, uri, ObjectUtils.asMap("name", name), options);
    }

    public ApiResponse deleteUserGroup(String groupId, Map options) throws Exception {
        List<String> uri = Arrays.asList(PROVISIONING, ACCOUNTS, accountId, USER_GROUPS, groupId);
        return callAccountApi(Api.HttpMethod.DELETE, uri, Collections.emptyMap(), options);
    }

    public ApiResponse addUserToGroup(String groupId, String userId, Map options) throws Exception {
        List<String> uri = Arrays.asList(PROVISIONING, ACCOUNTS, accountId, USER_GROUPS, groupId, USERS, userId);
        return callAccountApi(Api.HttpMethod.POST, uri, Collections.emptyMap(), options);
    }

    public ApiResponse removeUserFromGroup(String groupId, String userId, Map options) throws Exception {
        List<String> uri = Arrays.asList(PROVISIONING, ACCOUNTS, accountId, USER_GROUPS, groupId, USERS, userId);
        return callAccountApi(Api.HttpMethod.DELETE, uri, Collections.emptyMap(), options);
    }

    public ApiResponse getUserGroup(String groupId, Map options) throws Exception {
        List<String> uri = Arrays.asList(PROVISIONING, ACCOUNTS, accountId, USER_GROUPS, groupId);
        return callAccountApi(Api.HttpMethod.GET, uri, Collections.emptyMap(), options);
    }

    public ApiResponse listUserGroups(Map options) throws Exception {
        List<String> uri = Arrays.asList(PROVISIONING, ACCOUNTS, accountId, USER_GROUPS);
        return callAccountApi(Api.HttpMethod.GET, uri, Collections.emptyMap(), options);
    }

    public ApiResponse listUserGroupUsers(String groupId, Map options) throws Exception {
        List<String> uri = Arrays.asList(PROVISIONING, ACCOUNTS, accountId, USER_GROUPS, groupId, USERS);
        return callAccountApi(Api.HttpMethod.GET, uri, Collections.emptyMap(), options);
    }

    private ApiResponse performUserAction(Api.HttpMethod method, List<String> uri, String email, String name, Role role, List<String> subAccountsIds, Map options) throws Exception {
        if (options == null) {
            options = new HashMap();
        }

        options.put("content_type", "json");

        return callAccountApi(method, uri, ObjectUtils.asMap(
                "email", email,
                "name", name,
                "role", role == null ? null : role.value,
                "sub_account_ids", subAccountsIds),
                options);
    }

    private ApiResponse callAccountApi(Api.HttpMethod method, List<String> uri, Map params, Map options) throws Exception {
        if (options == null)
            options = ObjectUtils.emptyMap();

        String prefix = ObjectUtils.asString(options.get("upload_prefix"), "https://api.cloudinary.com");
        String apiKey = ObjectUtils.asString(options.get("provisioning_api_key"), this.key);
        if (apiKey == null) throw new IllegalArgumentException("Must supply provisioning_api_key");
        String apiSecret = ObjectUtils.asString(options.get("provisioning_api_secret"), secret);
        if (apiSecret == null) throw new IllegalArgumentException("Must supply provisioningapi_secret");


        String apiUrl = StringUtils.join(Arrays.asList(prefix, "v1_1"), "/");
        for (String component : uri) {
            apiUrl = apiUrl + "/" + component;
        }

        return getApiResponse(method, params, options, apiKey, apiSecret, apiUrl);
    }

    private ApiResponse getApiResponse(Api.HttpMethod method, Map params, Map options, String apiKey, String apiSecret, String apiUrl) throws Exception {
        HttpURLConnection con = prepareRequest(method, apiUrl, params, options);

        int code = con.getResponseCode();
        InputStream responseStream = code == 200 ? con.getInputStream() : con.getErrorStream();
        String responseData = StringUtils.read(responseStream);

        Class<? extends Exception> exceptionClass = Api.CLOUDINARY_API_ERROR_CLASSES.get(code);
        if (code != 200 && exceptionClass == null) {
            throw new GeneralError("Server returned unexpected status code - " + code + " - " + responseData);
        }
        Map result;

        try {
            JSONObject responseJSON = new JSONObject(responseData);
            result = ObjectUtils.toMap(responseJSON);
        } catch (JSONException e) {
            throw new RuntimeException("Invalid JSON response from server " + e.getMessage());
        }

        if (code == 200) {
            return new ApiResponse(result);
        } else {
            String message = (String) ((Map) result.get("error")).get("message");
            Constructor<? extends Exception> exceptionConstructor = exceptionClass.getConstructor(String.class);
            throw exceptionConstructor.newInstance(message);
        }
    }

    private HttpURLConnection prepareRequest(Api.HttpMethod method, String apiUrl, Map<String, ?> params, Map options) throws URISyntaxException, IOException {

        String contentType = ObjectUtils.asString(options.get("content_type"), "urlencoded");
        HttpURLConnection con;

        String content = null;
        if (method == Api.HttpMethod.GET) {
            con = (HttpURLConnection) new URL(apiUrl + "?" + buildQueryParams(params)).openConnection();
            con.setRequestMethod("GET");
        } else {
            con = (HttpURLConnection) new URL(apiUrl).openConnection();
            con.setDoOutput(true);
            Map<String, Object> paramsCopy = new HashMap<String, Object>((Map<String, Object>) params);
            switch (method) {
                case PUT:
                    con.setRequestMethod("PUT");
                    break;
                case DELETE: //uses HttpPost instead of HttpDelete
                    paramsCopy.put("_method", "delete");
                    //continue with POST
                case POST:
                    con.setRequestMethod("POST");
                    break;
                default:
                    throw new IllegalArgumentException("Unknown HTTP method");
            }

            if (contentType.equals("json")) {
                JSONObject asJSON = ObjectUtils.toJSON(paramsCopy);
                content = asJSON.toString();
                con.addRequestProperty("content-type", "application/json");
            } else {
                content = buildQueryParams(paramsCopy);
            }
        }

        con.addRequestProperty("Authorization", "Basic " + Base64Coder.encodeString(key + ":" + secret));

        if (StringUtils.isNotBlank(content)) {
            byte[] input = content.getBytes("utf-8");
            OutputStream os = con.getOutputStream();
            os.write(input, 0, input.length);
            os.flush();
            os.close();
        }

        int timeout = configuration.getTimeout();
        if (timeout > 0) {
            con.setConnectTimeout(timeout * 1000);
            con.setReadTimeout(timeout * 1000);
        }

        return con;
    }

    public static final class ApiResponse extends HashMap implements com.cloudinary.api.ApiResponse {
        public ApiResponse(Map result) {
            super(result);
        }

        @Override
        public Map<String, RateLimit> rateLimits() throws ParseException {
            return null;
        }

        @Override
        public RateLimit apiRateLimit() throws ParseException {
            return null;
        }
    }

    private String buildQueryParams(Map<String, ?> params) throws UnsupportedEncodingException {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry entry : params.entrySet()) {
            if (StringUtils.isNotBlank(entry.getValue())) {
                String encodedKey = URLEncoder.encode(entry.getKey().toString(), "utf8");
                String encodedValue = URLEncoder.encode(entry.getValue().toString(), "utf8");
                builder.append(encodedKey).append("=").append(encodedValue).append("&");
            }
        }

        // TODO shouldn't have last &
        return builder.toString();
    }
}