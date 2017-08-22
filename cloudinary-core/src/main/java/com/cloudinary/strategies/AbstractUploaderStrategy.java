package com.cloudinary.strategies;

import com.cloudinary.Cloudinary;
import com.cloudinary.ProgressCallback;
import com.cloudinary.Uploader;
import com.cloudinary.utils.ObjectUtils;
import com.cloudinary.utils.StringUtils;
import org.cloudinary.json.JSONException;
import org.cloudinary.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import com.cloudinary.utils.ObjectUtils;
import com.cloudinary.utils.StringUtils;

public abstract class AbstractUploaderStrategy {
    private final static int[] ERROR_CODES = new int[]{400, 401, 403, 404, 420, 500};
    protected Uploader uploader;

    public void init(Uploader uploader) {
        this.uploader = uploader;
    }

    public Cloudinary cloudinary() {
        return this.uploader.cloudinary();
    }

    @SuppressWarnings("rawtypes")
    public Map callApi(String action, Map<String, Object> params, Map options, Object file) throws IOException {
        return callApi(action, params, options, file, null);
    }

    public abstract Map callApi(String action, Map<String, Object> params, Map options, Object file, ProgressCallback progressCallback) throws IOException;

    protected String buildUploadUrl(String action, Map options) {
        String cloudinary = ObjectUtils.asString(options.get("upload_prefix"),
                ObjectUtils.asString(uploader.cloudinary().config.uploadPrefix, "https://api.cloudinary.com"));
        String cloud_name = ObjectUtils.asString(options.get("cloud_name"), ObjectUtils.asString(uploader.cloudinary().config.cloudName));
        if (cloud_name == null)
            throw new IllegalArgumentException("Must supply cloud_name in tag or in configuration");

        if (action.equals("delete_by_token")) {
            // delete_by_token doesn't need resource_type
            return StringUtils.join(new String[]{cloudinary, "v1_1", cloud_name, action}, "/");
        } else {
            String resource_type = ObjectUtils.asString(options.get("resource_type"), "image");
            return StringUtils.join(new String[]{cloudinary, "v1_1", cloud_name, resource_type, action}, "/");
        }
    }

    protected Map processResponse(boolean returnError, int code, String responseData) {
        String errorMessage = null;
        Map result = null;
        if (code == 200 || canParseErrorMessage(code)) {
            try {
                JSONObject responseJSON = new JSONObject(responseData);
                result = ObjectUtils.toMap(responseJSON);

                if (result.containsKey("error")) {
                    Map error = (Map) result.get("error");
                    error.put("http_code", code);
                    errorMessage = (String) error.get("message");
                }
            } catch (JSONException e) {
                errorMessage = "Invalid JSON response from server " + e.getMessage();
            }
        } else {
            errorMessage = "Server returned unexpected status code - " + code;
            if (StringUtils.isNotBlank(responseData)) {
                errorMessage += (" - " + responseData);
            }
        }

        if (StringUtils.isNotBlank(errorMessage)) {
            if (returnError) {
                // return a result containing the error instead of throwing an exception:
                if (result == null) {
                    Map error = new HashMap();
                    error.put("http_code", code);
                    error.put("message", errorMessage);
                    result = new HashMap();
                    result.put("error", error);
                } // else - Result is already built, with the error inside. Nothing to do.
            } else {
                throw new RuntimeException(errorMessage);
            }
        }

        return result;
    }

    private boolean canParseErrorMessage(int code) {
        return Arrays.binarySearch(ERROR_CODES, code) >= 0;
    }

    protected boolean requiresSigning(String action, Map options) {
        boolean unsigned = Boolean.TRUE.equals(options.get("unsigned"));
        boolean deleteByToken = "delete_by_token".equals(action);

        return !unsigned && !deleteByToken;
    }
}