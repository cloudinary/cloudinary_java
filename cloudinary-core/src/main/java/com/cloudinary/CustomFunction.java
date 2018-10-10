package com.cloudinary;

import com.cloudinary.utils.Base64Coder;

/**
 * Helper class to generate a custom function params to be used in {@link Transformation#customFunction(CustomFunction)}.
 */
public class CustomFunction extends BaseParam{

    private CustomFunction(String... components) {
        super(components);
    }

    /**
     * Generate a web-assembly custom action param to send to {@link Transformation#customFunction(CustomFunction)}
     * @param publicId The public id of the web-assembly file
     * @return A new instance of custom action param
     */
    public static CustomFunction wasm(String publicId){
        return new CustomFunction("wasm", publicId);
    }

    /**
     * Generate a remote lambda custom action param to send to {@link Transformation#customFunction(CustomFunction)}
     * @param url   The public url of the aws lambda function
     * @return A new instance of custom action param
     */
    public static CustomFunction remote(String url){
        return new CustomFunction("remote", Base64Coder.encodeURLSafeString(url));
    }
}
