package com.cloudinary.metadata;

import java.util.ArrayList;

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
}
