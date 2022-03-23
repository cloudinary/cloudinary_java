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
    public String generate(Iterable<Map> optionsList) {
        List<String> components = new ArrayList<String>();
        for (Map options : optionsList) {
            if (options.size() > 0) {
                components.add(super.generate(options));
            }
        }

        if (format != null){
            components.add(format);
        }

        return StringUtils.join(components, "/");
    }

    @Override
    public String generate(Map options) {
        List<String> eager = new ArrayList<String>();
        eager.add(super.generate(options));

        if (format != null){
            eager.add(format);
        }

        return StringUtils.join(eager, "/");
    }
}
