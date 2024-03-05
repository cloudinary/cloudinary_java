package com.cloudinary.metadata;

import java.util.HashMap;

/**
 * Represents the restrictions metadata field.
 */
public class Restrictions {

    private final HashMap restrictions = new HashMap();

    /**
     * Set the custom field into restrictions.
     * @param key The key of the field.
     * @param value The value of the field.
     */
    public Restrictions setRestriction(String key, Object value) {
        restrictions.put(key, value);
        return this;
    }

    /**
     * Set the read only ui field.
     * @param value The read only ui value.
     */
    public Restrictions setReadOnlyUI(Boolean value) {
        return setRestriction("readonly_ui", value);
    }

    /**
     * Set the read only ui field to true.
     */
    public Restrictions setReadOnlyUI() {
        return this.setReadOnlyUI(true);
    }

    public HashMap toHash() {
        return restrictions;
    }
}
