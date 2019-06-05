package com.cloudinary.metadata;

import org.cloudinary.json.JSONArray;
import org.cloudinary.json.JSONObject;

import java.util.List;

public class MetadataDataSource extends JSONObject {
    public MetadataDataSource(List<Entry> entries) {
        put("values", new JSONArray(entries.toArray()));
    }

    public static class Entry extends JSONObject {
        public Entry(String externalId, String value){
            setExternalId(externalId);
            setValue(value);
        }

        public Entry(String value){
            this(null, value);
        }

        public void setExternalId(String externalId) {
            put("external_id", externalId);
        }

        public String getExternalId() {
            return optString("external_id");
        }

        public void setValue(String value) {
            put("value", value);
        }

        public String getValue() {
            return optString("value");
        }

        public void setState(String state) {
            put("state", state);
        }

        public String getState() {
            return optString("state");
        }
    }
}
