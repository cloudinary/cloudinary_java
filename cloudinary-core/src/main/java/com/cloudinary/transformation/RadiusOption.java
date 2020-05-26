package com.cloudinary.transformation;

import com.cloudinary.utils.StringUtils;

public class RadiusOption {
    public static String toExpression(Object[] radiusOption) {
        if (radiusOption == null) {
            return null;
        }

        if (radiusOption.length == 0 || radiusOption.length > 4) {
            throw new IllegalArgumentException("Radius array should contain between 1 and 4 values");
        }

        return StringUtils.join(normalizeRadiusOptions(radiusOption), ":");
    }

    private static Object[] normalizeRadiusOptions(Object[] radiusOptions) {
        Object[] workingCopy = radiusOptions.clone();
        for (int i = 0; i < workingCopy.length; i++) {
            if (workingCopy[i] == null
                    || (workingCopy[i] instanceof String && ((String) workingCopy[i]).isEmpty())) {
                workingCopy[i] = 0;
            }
        }

        return workingCopy;
    }
}
