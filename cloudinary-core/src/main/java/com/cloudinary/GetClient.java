package com.cloudinary;

import com.cloudinary.cache.ResponsiveBreakpointPayload;
import com.cloudinary.strategies.AbstractGetStrategy;
import com.cloudinary.utils.StringUtils;
import org.cloudinary.json.JSONArray;
import org.cloudinary.json.JSONObject;

import java.util.Locale;

public class GetClient {
    private Cloudinary cloudinary;
    private AbstractGetStrategy strategy;

    public GetClient(Cloudinary cloudinary, AbstractGetStrategy getStrategy) {
        this.cloudinary = cloudinary;
        this.strategy = getStrategy;
        this.strategy.init(cloudinary);
    }

    public ResponsiveBreakpointPayload getBreakpoints(Url baseUrl, int minWidth, int maxWidth, int bytesStepSize, int maxImages) throws Exception {
        baseUrl.transformation().chain().rawTransformation(String.format(Locale.getDefault(), "w_auto:breakpoints_%d_%d_%d_%d:json", minWidth, maxWidth, bytesStepSize, maxImages));

        String breakpointsUrl = baseUrl.generate();

        String breakpointsStr = strategy.get(breakpointsUrl);
        if (StringUtils.isNotBlank(breakpointsStr)) {
            JSONArray arr = new JSONObject(breakpointsStr).getJSONArray("breakpoints");

            int[] breakpoints = new int[arr.length()];
            for (int i = 0; i < arr.length(); i++) {
                breakpoints[i] = arr.getInt(i);
            }

            return new ResponsiveBreakpointPayload(breakpoints);
        }

        return null;
    }
}
