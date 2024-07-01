package com.cloudinary.metadata;
import java.util.HashMap;
import java.util.Map;

public class MetadataRuleCondition {
    String metadata_field_id;
    Boolean populated;
    Map<String, String> includes;
    String equals;

    public MetadataRuleCondition(String metadata_field_id, Boolean populated, Map<String, String> includes, String equals) {
        this.metadata_field_id = metadata_field_id;
        this.populated = populated;
        this.includes = includes;
        this.equals = equals;
    }

    public String getMetadata_field_id() {
        return metadata_field_id;
    }

    public void setMetadata_field_id(String metadata_field_id) {
        this.metadata_field_id = metadata_field_id;
    }

    public Boolean getPopulated() {
        return populated;
    }

    public void setPopulated(Boolean populated) {
        this.populated = populated;
    }

    public Map<String, String> getIncludes() {
        return includes;
    }

    public void setIncludes(Map<String, String> includes) {
        this.includes = includes;
    }

    public String getEquals() {
        return equals;
    }

    public void setEquals(String equals) {
        this.equals = equals;
    }

    public Map asMap() {
        Map result = new HashMap(4);
        result.put("metadata_field_id", metadata_field_id);
        result.put("populated", populated);
        result.put("includes", includes);
        result.put("equals", equals);
        return result;
    }
}
