package com.cloudinary.metadata;

import java.util.HashMap;

public class Restrictions {

    HashMap restrictions = new HashMap();

    public Restrictions setRestriction(String key, Object value) {
        restrictions.put(key, value);
        return this;
    }

    public Restrictions setReadOnlyUI(Boolean value) {
        return setRestriction("readonly_ui", value);
    }

    public Restrictions setReadOnlyUI() {
        return this.setReadOnlyUI(true);
    }
}
