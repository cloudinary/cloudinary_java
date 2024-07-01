package com.cloudinary.metadata;

import java.util.HashMap;
import java.util.Map;

public class MetadataRuleResult {
    Boolean enabled;
    String activateValues;
    String applyValues;
    Boolean setMandatory;

    public MetadataRuleResult(Boolean enabled, String activateValues, String applyValues, Boolean setMandatory) {
        this.enabled = enabled;
        this.activateValues = activateValues;
        this.applyValues = applyValues;
        this.setMandatory = setMandatory;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public String getActivateValues() {
        return activateValues;
    }

    public void setActivateValues(String activateValues) {
        this.activateValues = activateValues;
    }

    public String getApplyValues() {
        return applyValues;
    }

    public void setApplyValues(String applyValues) {
        this.applyValues = applyValues;
    }

    public Boolean getSetMandatory() {
        return setMandatory;
    }

    public void setSetMandatory(Boolean setMandatory) {
        this.setMandatory = setMandatory;
    }
    public Map asMap() {
        Map result = new HashMap(4);
        result.put("enable", enabled);
        result.put("activate_values", activateValues);
        result.put("apply_values", applyValues);
        result.put("mandatory", setMandatory);
        return result;
    }
}
