package com.cloudinary;

import com.cloudinary.utils.StringUtils;

import java.util.List;
import java.util.Map;

public class EagerTransformation extends Transformation<EagerTransformation> {

    @SuppressWarnings("rawtypes")
    public EagerTransformation(List<Map> transformations) {
        super(transformations);
    }

    public EagerTransformation() {
        super();
    }

    @Override
    protected void addFormat(List<String> components) {
        if (StringUtils.isNotBlank(format)){
            components.add(format);
        }
    }
}
