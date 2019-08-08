package com.cloudinary.metadata;

import org.cloudinary.json.JSONArray;
import org.cloudinary.json.JSONObject;

import java.util.List;

/**
 * Represent a data source for a given field. This is used in both 'Set' and 'Enum' field types.
 * The datasource holds a list of the valid values to be used with the corresponding metadata field.
 */
public class MetadataDataSource extends JSONObject {
    /**
     * Creates a new instance of data source with the given list of entries.
     * @param entries
     */
    public MetadataDataSource(List<Entry> entries) {
        put("values", new JSONArray(entries.toArray()));
    }

    /**
     * Represents a single entry in a datasource definition for a field.
     */
    public static class Entry extends JSONObject {
        public Entry(String externalId, String value){
            setExternalId(externalId);
            setValue(value);
        }

        /**
         * Create a new entry with a string value.
         * @param value The value to use in the entry.
         */
        public Entry(String value){
            this(null, value);
        }

        /**
         * Set the id of the entry. Will be auto-generated if left blank.
         * @param externalId
         */
        public void setExternalId(String externalId) {
            put("external_id", externalId);
        }

        /**
         * Get the id of the entry.
         * @return
         */
        public String getExternalId() {
            return optString("external_id");
        }

        /**
         * Set the value of the entry.
         * @param value The value to set.
         */
        public void setValue(String value) {
            put("value", value);
        }

        /**
         * Get the value of the entry.
         * @return The value.
         */
        public String getValue() {
            return optString("value");
        }
    }
}
