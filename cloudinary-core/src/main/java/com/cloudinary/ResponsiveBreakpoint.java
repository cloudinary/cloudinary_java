package com.cloudinary;

import org.cloudinary.json.JSONObject;

public class ResponsiveBreakpoint extends JSONObject {
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
        return (Transformation) opt("transformation");
    }

    public ResponsiveBreakpoint transformation(Transformation transformation) {
        put("transformation", transformation);
        return this;
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
