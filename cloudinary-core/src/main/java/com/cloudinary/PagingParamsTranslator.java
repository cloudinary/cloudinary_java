package com.cloudinary;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

class PagingParamsTranslator {
    static Map<String, ?> toMap(PagingParams params) {
        if (params == null) {
            return Collections.emptyMap();
        }

        Map<String, Object> result = new HashMap<String, Object>();
        if (params.getMaxResults() != null) {
            result.put("max_results", params.getMaxResults());
        }
        if (params.getNextCursor() != null) {
            result.put("next_cursor", params.getNextCursor());
        }
        return result;
    }
}
