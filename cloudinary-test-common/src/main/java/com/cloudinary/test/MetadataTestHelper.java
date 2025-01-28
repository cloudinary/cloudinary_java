package com.cloudinary.test;

import com.cloudinary.Api;
import com.cloudinary.api.ApiResponse;
import com.cloudinary.metadata.MetadataField;
import com.cloudinary.metadata.MetadataValidation;
import com.cloudinary.metadata.StringMetadataField;

public final class MetadataTestHelper {
    private MetadataTestHelper() {}

    public static StringMetadataField newFieldInstance(String label, Boolean mandatory) throws Exception {
        StringMetadataField field = new StringMetadataField();
        field.setLabel(label);
        field.setMandatory(mandatory);
        field.setAllowDynamicListValues(true);
        field.setValidation(new MetadataValidation.StringLength(3, 9));
        field.setDefaultValue("val_test");
        return field;
    }

    public static ApiResponse addFieldToAccount(Api api, MetadataField field) throws Exception {
        ApiResponse apiResponse = api.addMetadataField(field);
        return apiResponse;
    }
}

