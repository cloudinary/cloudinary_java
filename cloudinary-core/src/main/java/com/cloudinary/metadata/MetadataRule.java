package com.cloudinary.metadata;

import com.cloudinary.utils.ObjectUtils;

import java.util.HashMap;
import java.util.Map;

public class MetadataRule {
    String metadataFieldId;
    String name;
    MetadataRuleCondition condition;
    MetadataRuleResult result;

    public MetadataRule(String metadataFieldId, String name, MetadataRuleCondition condition, MetadataRuleResult result) {
        this.metadataFieldId = metadataFieldId;
        this.name = name;
        this.condition = condition;
        this.result = result;
    }

    public String getMetadataFieldId() {
        return metadataFieldId;
    }

    public void setMetadataFieldId(String metadataFieldId) {
        this.metadataFieldId = metadataFieldId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public MetadataRuleCondition getCondition() {
        return condition;
    }

    public void setCondition(MetadataRuleCondition condition) {
        this.condition = condition;
    }

    public MetadataRuleResult getResult() {
        return result;
    }

    public void setResult(MetadataRuleResult result) {
        this.result = result;
    }

    public Map asMap() {
        Map map = new HashMap();
        map.put("metadata_field_id", getMetadataFieldId());
        map.put("name", getName());
        if (getCondition() != null) {
            map.put("condition", ObjectUtils.toJSON(getCondition().asMap()));
        }
        if(getResult() != null) {
            map.put("result", ObjectUtils.toJSON(getResult().asMap()));
        }
        return map;
    }
}
