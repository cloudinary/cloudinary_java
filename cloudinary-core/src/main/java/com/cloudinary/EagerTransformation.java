package com.cloudinary;

import com.cloudinary.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EagerTransformation extends Transformation<EagerTransformation> {
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

    @Override
    public String generate(Map options) {
        List<String> eager = new ArrayList<String>();
        eager.add(super.generate(options));

        if (StringUtils.isNotBlank(format)){
            eager.add(format);
        }

        return StringUtils.join(eager, "/");
    }
}
