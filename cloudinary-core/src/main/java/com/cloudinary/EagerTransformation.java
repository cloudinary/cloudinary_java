package com.cloudinary;

import java.util.List;
import java.util.Map;

public class EagerTransformation extends Transformation {
    protected String format;

    @SuppressWarnings("rawtypes")
    public EagerTransformation(List<Map> transformations) {
        super(transformations);
    }

    public EagerTransformation() {
        super();
    }

    public EagerTransformation format(String format) {
        this.format = format;
        return this;
    }

    public String getFormat() {
        return format;
    }
}
