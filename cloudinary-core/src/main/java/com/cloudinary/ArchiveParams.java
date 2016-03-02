package com.cloudinary;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ArchiveParams {
    public static final String FORMAT_ZIP = "zip";

    public static final String MODE_DOWNLOAD = "download";
    public static final String MODE_CREATE = "create";

    private String resourceType = "image";
    private String type = null;
    private String mode = MODE_CREATE;
    private String targetFormat = null;
    private String targetPublicId = null;
    private boolean flattenFolders = false;
    private boolean flattenTransformations = false;
    private boolean useOriginalFilename = false;
    private boolean async = false;
    private boolean keepDerived = false;
    private String notificationUrl = null;
    private String[] targetTags = null;
    private String[] tags = null;
    private String[] publicIds = null;
    private String[] prefixes = null;
    private Transformation[] transformations = null;

    public String resourceType() {
        return resourceType;
    }

    public ArchiveParams resourceType(String resourceType) {
        if (resourceType == null)
            throw new IllegalArgumentException("resource type must be non-null");
        this.resourceType = resourceType;
        return this;
    }

    public String type() {
        return type;
    }

    public ArchiveParams type(String type) {
        this.type = type;
        return this;
    }

    public String mode() {
        return mode;
    }

    public ArchiveParams mode(String mode) {
        this.mode = mode;
        return this;
    }

    public String targetFormat() {
        return targetFormat;
    }

    public ArchiveParams targetFormat(String targetFormat) {
        this.targetFormat = targetFormat;
        return this;
    }

    public String targetPublicId() {
        return targetPublicId;
    }

    public ArchiveParams targetPublicId(String targetPublicId) {
        this.targetPublicId = targetPublicId;
        return this;
    }

    public boolean isFlattenFolders() {
        return flattenFolders;
    }

    public ArchiveParams flattenFolders(boolean flattenFolders) {
        this.flattenFolders = flattenFolders;
        return this;
    }

    public boolean isFlattenTransformations() {
        return flattenTransformations;
    }

    public ArchiveParams flattenTransformations(boolean flattenTransformations) {
        this.flattenTransformations = flattenTransformations;
        return this;
    }

    public boolean isUseOriginalFilename() {
        return useOriginalFilename;
    }

    public ArchiveParams useOriginalFilename(boolean useOriginalFilename) {
        this.useOriginalFilename = useOriginalFilename;
        return this;
    }

    public boolean isAsync() {
        return async;
    }

    public ArchiveParams async(boolean async) {
        this.async = async;
        return this;
    }

    public boolean isKeepDerived() {
        return keepDerived;
    }

    public ArchiveParams keepDerived(boolean keepDerived) {
        this.keepDerived = keepDerived;
        return this;
    }

    public String notificationUrl() {
        return notificationUrl;
    }

    public ArchiveParams notificationUrl(String notificationUrl) {
        this.notificationUrl = notificationUrl;
        return this;
    }

    public String[] targetTags() {
        return targetTags;
    }

    public ArchiveParams targetTags(String[] targetTags) {
        this.targetTags = targetTags;
        return this;
    }

    public String[] tags() {
        return tags;
    }

    public ArchiveParams tags(String[] tags) {
        this.tags = tags;
        return this;
    }

    public String[] publicIds() {
        return publicIds;
    }

    public ArchiveParams publicIds(String[] publicIds) {
        this.publicIds = publicIds;
        return this;
    }

    public String[] prefixes() {
        return prefixes;
    }

    public ArchiveParams prefixes(String[] prefixes) {
        this.prefixes = prefixes;
        return this;
    }

    public Transformation[] transformations() {
        return transformations;
    }

    public ArchiveParams transformations(Transformation[] transformations) {
        this.transformations = transformations;
        return this;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("resource_type", resourceType);
        params.put("type", type);
        params.put("mode", mode);
        if (targetPublicId != null)
            params.put("target_public_id", targetPublicId);
        params.put("flatten_folders", flattenFolders);
        params.put("flatten_transformations", flattenTransformations);
        params.put("use_original_filename", useOriginalFilename);
        params.put("async", async);
        params.put("keep_derived", keepDerived);
        if (notificationUrl != null)
            params.put("notification_url", notificationUrl);
        if (targetTags != null)
            params.put("target_tags", targetTags);
        if (tags != null)
            params.put("tags", tags);
        if (publicIds != null)
            params.put("public_ids", publicIds);
        if (prefixes != null)
            params.put("prefixes", prefixes);
        if (transformations != null) {
            params.put("transformations", Arrays.asList(transformations));
        }
        return params;
    }
}
