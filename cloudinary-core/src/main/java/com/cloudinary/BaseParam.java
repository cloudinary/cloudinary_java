package com.cloudinary;

import com.cloudinary.utils.StringUtils;

import java.util.List;

public class BaseParam {
    private String param;

    protected BaseParam(List<String> components) {
        this.param = StringUtils.join(components, ":");
    }

    protected BaseParam(String... components) {
        this.param = StringUtils.join(components, ":");
    }

    @Override
    public String toString() {
        return param;
    }
}
