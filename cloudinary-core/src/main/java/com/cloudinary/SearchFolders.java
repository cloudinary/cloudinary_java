package com.cloudinary;

import com.cloudinary.api.ApiResponse;
import com.cloudinary.utils.ObjectUtils;

import java.util.Arrays;
import java.util.Map;

public class SearchFolders extends Search {

    public SearchFolders(Cloudinary cloudinary) {
        super(cloudinary);
    }

    public ApiResponse execute() throws Exception {
        Map<String, String> options = ObjectUtils.asMap("content_type", "json");
        return this.api.callApi(Api.HttpMethod.POST, Arrays.asList("folders", "search"), this.toQuery(), options);
    }
}
