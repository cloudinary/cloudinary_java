package com.cloudinary.strategies;

import java.io.IOException;
import java.util.Map;

import com.cloudinary.Cloudinary;
import com.cloudinary.ProgressCallback;
import com.cloudinary.Uploader;
import com.cloudinary.utils.ObjectUtils;
import com.cloudinary.utils.StringUtils;

public abstract class AbstractUploaderStrategy {
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
            // the only method (so far) that doesn't need resource_type
            return StringUtils.join(new String[]{cloudinary, "v1_1", cloud_name, action}, "/");
        } else {
            String resource_type = ObjectUtils.asString(options.get("resource_type"), "image");
            return StringUtils.join(new String[]{cloudinary, "v1_1", cloud_name, resource_type, action}, "/");
        }
    }
}