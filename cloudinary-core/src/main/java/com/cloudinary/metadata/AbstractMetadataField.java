package com.cloudinary.metadata;

import com.cloudinary.api.ApiResponse;
import org.cloudinary.json.JSONObject;

import java.util.List;
import java.util.Map;

public class AbstractMetadataField<T> extends JSONObject {

    AbstractMetadataField(MetadataFieldType type) {
        put("type", type.toString());
    }

    public static List<AbstractMetadataField> from(Map<String,Object> response) {
        return null;
    }

    public MetadataFieldType getType() {
        return MetadataFieldType.valueOf(optString("type").toUpperCase());
    }

    public String getExternalId() {
        return optString("external_id");
    }

    public void setExternalId(String externalId) {
        put("external_id", externalId);
    }

    public String getLabel() {
        return optString("label");
    }

    public void setLabel(String label) {
        put("label", label);
    }

    public boolean isMandatory() {
        return optBoolean("mandatory");
    }

    public void setMandatory(Boolean mandatory) {
        put("mandatory", mandatory);
    }

    public T getDefaultValue() {
        //noinspection unchecked
        return (T)opt("default_value");
    }

    public void setDefaultValue(T defaultValue) {
        put("default_value", defaultValue);
    }

    public MetadataValidation getValidation() {
        return (MetadataValidation) optJSONObject("validation");
    }

    public void setValidation(MetadataValidation validation) {
        put("validation", validation);
    }

    public MetadataDataSource getDataSource() {
        return (MetadataDataSource) optJSONObject("datasource");
    }

    public void setDataSource(MetadataDataSource dataSource) {
        put("datasource", dataSource);
    }
}