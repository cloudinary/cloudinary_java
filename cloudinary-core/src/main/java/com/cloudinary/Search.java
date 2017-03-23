package com.cloudinary;

import com.cloudinary.api.ApiResponse;
import com.cloudinary.utils.ObjectUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Search {

    private Cloudinary cloudinary;
    private ArrayList<HashMap<String, Object>> sortByParam;
    private ArrayList<String> aggregateParam;
    private ArrayList<String> withFieldParam;
    private HashMap<String, Object> params;

    Search(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
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
        aggregateParam.add(field);
        return this;
    }

    public Search withField(String field) {
        withFieldParam.add(field);
        return this;
    }

    public Search sortBy(String field, String dir) {
        HashMap<String, Object> sortBucket = new HashMap<String, Object>();
        sortBucket.put(field, dir);
        sortByParam.add(sortBucket);
        return this;
    }

    public HashMap<String, Object> toQuery() {
        HashMap<String, Object> queryParams = new HashMap<String, Object>(this.params);
        queryParams.put("with_field", withFieldParam);
        queryParams.put("sort_by", sortByParam);
        queryParams.put("aggregate", aggregateParam);
        return queryParams;
    }

    public ApiResponse execute() throws Exception {
        Map<String, String> options = ObjectUtils.asMap("content_type", "json");
        return this.cloudinary.api().callApi(Api.HttpMethod.POST, Arrays.asList("resources", "search"), this.toQuery(), options);
    }
}