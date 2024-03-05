package com.cloudinary.metadata;

import org.cloudinary.json.JSONObject;

import java.text.ParseException;

/**
 * Represents a single metadata field. Use one of the derived classes in the metadata API calls.
 * @param <T>
 */
public class MetadataField<T> extends JSONObject {

    public static final String DEFAULT_VALUE = "default_value";
    public static final String EXTERNAL_ID = "external_id";
    public static final String LABEL = "label";
    public static final String MANDATORY = "mandatory";
    public static final String TYPE = "type";
    public static final String VALIDATION = "validation";
    public static final String RESTRICTIONS = "restrictions";

    public MetadataField(MetadataFieldType type) {
        put(TYPE, type.toString());
    }

    public MetadataField(String type) {
        put(TYPE, type);
    }

    /**
     * The type of the field.
     * @return String with the name of the type.
     */
    public MetadataFieldType getType() {
        return MetadataFieldType.valueOf(optString(TYPE).toUpperCase());
    }

    /**
     * Get the id of the field.
     * @return String, field id.
     */
    public String getExternalId() {
        return optString(EXTERNAL_ID);
    }

    /**
     * Set the id of the string (auto-generated if this is left blank).
     * @param externalId The id to set.
     */
    public void setExternalId(String externalId) {
        put(EXTERNAL_ID, externalId);
    }

    /**
     * Get the label of the field
     * @return String, the label of the field.
     */
    public String getLabel() {
        return optString(LABEL);
    }

    /**
     * Sets the label of the field
     * @param label The label to set.
     */
    public void setLabel(String label) {
        put(LABEL, label);
    }

    /**
     * Cehcks whether the field is mandatory.
     * @return Boolean indicating whether the field is mandatory.
     */
    public boolean isMandatory() {
        return optBoolean(MANDATORY);
    }

    /**
     * Sets a boolean indicating whether this fields needs to be mandatory.
     * @param mandatory The boolean to set.
     */
    public void setMandatory(Boolean mandatory) {
        put(MANDATORY, mandatory);
    }

    /**
     * Gets the default value of this field.
     * @return The default value
     * @throws ParseException If the stored value can't be parsed to the correct type.
     */
    public T getDefaultValue() throws ParseException {
        //noinspection unchecked
        return (T)opt(DEFAULT_VALUE);
    }

    /**
     * Set the default value of the field
     * @param defaultValue The value to set.
     */
    public void setDefaultValue(T defaultValue) {
        put(DEFAULT_VALUE, defaultValue);
    }

    /**
     * Get the validation rules of this field.
     * @return The validation rules.
     */
    public MetadataValidation getValidation() {
        return (MetadataValidation) optJSONObject(VALIDATION);
    }

    /**
     * Set the validation rules of this field.
     * @param validation The rules to set.
     */
    public void setValidation(MetadataValidation validation) {
        put(VALIDATION, validation);
    }

    /**
     * Get the data source definition of this field.
     * @return The data source.
     */
    public MetadataDataSource getDataSource() {
        return (MetadataDataSource) optJSONObject("datasource");
    }

    /**
     * Set the datasource for the field.
     * @param dataSource The datasource to set.
     */
    public void setDataSource(MetadataDataSource dataSource) {
        put("datasource", dataSource);
    }

    /**
     * Set the restrictions rules of this field.
     * @param restrictions The rules to set.
     */
    public void setRestrictions(Restrictions restrictions) {
        put(RESTRICTIONS, restrictions.toHash());
    }
}