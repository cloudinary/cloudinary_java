package com.cloudinary;

import com.cloudinary.utils.StringUtils;

import org.cloudinary.json.JSONObject;

public class ResponsiveBreakpoint extends JSONObject {
    private Transformation transformation = null;
    private String format = "";

    public ResponsiveBreakpoint() {
        put("create_derived", true);
    }

    public boolean isCreateDerived() {
        return optBoolean("create_derived");
    }

    public ResponsiveBreakpoint createDerived(boolean createDerived) {
        put("create_derived", createDerived);
        return this;
    }

    public Transformation transformation() {
        return transformation;
    }

    public ResponsiveBreakpoint transformation(Transformation transformation) {
        this.transformation = transformation;
        updateTransformationKey();
        return this;
    }


    public ResponsiveBreakpoint format(String format) {
        this.format = format;
        updateTransformationKey();
        return this;
    }

    public String format() {
        return format;
    }

    private synchronized void updateTransformationKey() {
        String transformationStr = transformation == null ? "" : transformation.generate();
        if (StringUtils.isNotBlank(format)){
            transformationStr += "/" + format;
        }

        put("transformation", transformationStr);
    }

    public int maxWidth() {
        return optInt("max_width");
    }

    public ResponsiveBreakpoint maxWidth(int maxWidth) {
        put("max_width", maxWidth);
        return this;
    }

    public int minWidth() {
        return optInt("min_width");
    }

    public ResponsiveBreakpoint minWidth(Integer minWidth) {
        put("min_width", minWidth);
        return this;
    }

    public int bytesStep() {
        return optInt("bytes_step");
    }

    public ResponsiveBreakpoint bytesStep(Integer bytesStep) {
        put("bytes_step", bytesStep);
        return this;
    }

    public int maxImages() {
        return optInt("max_images");
    }

    public ResponsiveBreakpoint maxImages(Integer maxImages) {
        put("max_images", maxImages);
        return this;
    }
}
