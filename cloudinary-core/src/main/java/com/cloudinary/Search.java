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

    private int ttl = 300;

    Search(Cloudinary cloudinary) {
        this.api = cloudinary.api();
        this.params = new HashMap<String, Object>();
        this.sortByParam = new ArrayList<HashMap<String, Object>>();
        this.aggregateParam = new ArrayList<String>();
        this.withFieldParam = new ArrayList<String>();
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
        return queryParams;
    }

    public ApiResponse execute() throws Exception {
        Map<String, String> options = ObjectUtils.asMap("content_type", "json");
        return this.api.callApi(Api.HttpMethod.POST, Arrays.asList("resources", "search"), this.toQuery(), options);
    }


    public String toUrl() throws Exception {
        return toUrl(null);
    }
    /***
     Creates a signed Search URL that can be used on the client side.
     ***/
    public String toUrl(Map options) throws Exception {
        if(options == null) { options = ObjectUtils.asMap(); }
        String apiSecret = options.get("api_secret") != null ? (String) options.get("api_secret") : api.cloudinary.config.apiSecret;
        if (apiSecret == null) throw new IllegalArgumentException("Must supply api_secret");
        if(options.get("ttl") == null) {
            options.put("ttl", 300);
        } else {
            ttl = (int) options.get("ttl");
        }
        String nextCursor = (String) options.get("next_cursor");
        JSONObject json = ObjectUtils.toJSON(toQuery());
        String base64Query = Base64Coder.encodeURLSafeString(json.toString());
        String signature = StringUtils.encodeHexString(Util.hash(String.format("%d%s%s", ttl, base64Query, apiSecret), SignatureAlgorithm.SHA256));
        String source = options.get("source") != null ? (String) options.get("source") : "";
        String prefix = Url.unsignedDownloadUrlPrefix(source,api.cloudinary.config);

        return String.format("%s/search/%s/%d/%s%s", prefix, signature, ttl, base64Query,nextCursor != null ? nextCursor : "");
    }
}
