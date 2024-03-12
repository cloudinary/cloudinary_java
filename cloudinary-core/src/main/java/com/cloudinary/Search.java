package com.cloudinary;

import com.cloudinary.api.ApiResponse;
import com.cloudinary.utils.Base64Coder;
import com.cloudinary.utils.ObjectUtils;
import com.cloudinary.utils.StringUtils;
import org.cloudinary.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Search {

    protected final Api api;
    private ArrayList<HashMap<String, Object>> sortByParam;
    private ArrayList<String> aggregateParam;
    private ArrayList<String> withFieldParam;
    private HashMap<String, Object> params;
    private ArrayList<String> fields;

    private int ttl = 300;

    Search(Cloudinary cloudinary) {
        this.api = cloudinary.api();
        this.params = new HashMap<String, Object>();
        this.sortByParam = new ArrayList<HashMap<String, Object>>();
        this.aggregateParam = new ArrayList<String>();
        this.withFieldParam = new ArrayList<String>();
        this.fields = new ArrayList<String>();
    }

    public Search ttl(int ttl) {
        this.ttl = ttl;
        return this;
    }
    public Search expression(String value) {
        this.params.put("expression", value);
        return this;
    }

    public Search maxResults(Integer value) {
        this.params.put("max_results", value);
        return this;
    }

    public Search nextCursor(String value) {
        this.params.put("next_cursor", value);
        return this;
    }

    public Search aggregate(String field) {
        if (!aggregateParam.contains(field)) {
            aggregateParam.add(field);
        }
        return this;
    }

    public Search withField(String field) {
        if (!withFieldParam.contains(field)) {
            withFieldParam.add(field);
        }
        return this;
    }

    public Search sortBy(String field, String dir) {
        HashMap<String, Object> sortBucket = new HashMap<String, Object>();
        sortBucket.put(field, dir);
        for (int i = 0; i < sortByParam.size(); i++) {
            if (sortByParam.get(i).containsKey(field)){
                sortByParam.add(i, sortBucket);
                return this;
            }
        }
        sortByParam.add(sortBucket);
        return this;
    }

    public Search fields(String field) {
        if (!fields.contains(field)) {
            fields.add(field);
        }
        return this;
    }

    public HashMap<String, Object> toQuery() {
        HashMap<String, Object> queryParams = new HashMap<String, Object>(this.params);
        if (withFieldParam.size() > 0) {
            queryParams.put("with_field", withFieldParam);
        }
        if(sortByParam.size() > 0) {
            queryParams.put("sort_by", sortByParam);
        }
        if(aggregateParam.size() > 0) {
            queryParams.put("aggregate", aggregateParam);
        }
        if(fields.size() > 0) {
            queryParams.put("fields", fields);
        }
        return queryParams;
    }

    public ApiResponse execute() throws Exception {
        Map<String, String> options = ObjectUtils.asMap("content_type", "json");
        return this.api.callApi(Api.HttpMethod.POST, Arrays.asList("resources", "search"), this.toQuery(), options);
    }


    public String toUrl() throws Exception {
        return toUrl(null, null);
    }

    public String toUrl(String nextCursor) throws Exception {
        return toUrl(null, nextCursor);
    }
    /***
     Creates a signed Search URL that can be used on the client side.
     ***/
    public String toUrl(Integer ttl, String nextCursor) throws Exception {
        String nextCursorParam = nextCursor;
        String apiSecret = api.cloudinary.config.apiSecret;
        if (apiSecret == null) throw new IllegalArgumentException("Must supply api_secret");
        if(ttl == null) {
            ttl = this.ttl;
        }
        HashMap queryParams = toQuery();
        if(nextCursorParam == null) {
            nextCursorParam = (String) queryParams.get("next_cursor");
        }
        queryParams.remove("next_cursor");
        JSONObject json = ObjectUtils.toJSON(queryParams);
        String base64Query = Base64Coder.encodeURLSafeString(json.toString());
        String signature = StringUtils.encodeHexString(Util.hash(String.format("%d%s%s", ttl, base64Query, apiSecret), SignatureAlgorithm.SHA256));
        String prefix = Url.unsignedDownloadUrlPrefix(null,api.cloudinary.config);

        return String.format("%s/search/%s/%d/%s%s", prefix, signature, ttl, base64Query,nextCursorParam != null && !nextCursorParam.isEmpty() ? "/"+nextCursorParam : "");
    }
}
