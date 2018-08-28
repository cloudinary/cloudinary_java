package com.cloudinary;

import com.cloudinary.utils.Base64Coder;

/**
 * Helper class to generate a custom action params to be used in {@link Transformation#customAction(CustomAction)}.
 */
public class CustomAction extends BaseParam{

    private CustomAction(String... components) {
        super(components);
    }

    /**
     * Generate a web-assembly custom action param to send to {@link Transformation#customAction(CustomAction)}
     * @param publicId The public id of the web-assembly file
     * @return A new instance of custom action param
     */
    public static CustomAction wasm(String publicId){
        return new CustomAction("wasm", publicId);
    }

    /**
     * Generate a remote lambda custom action param to send to {@link Transformation#customAction(CustomAction)}
     * @param url   The public url of the aws lambda function
     * @return A new instance of custom action param
     */
    public static CustomAction remote(String url){
        return new CustomAction("remote", Base64Coder.encodeString(url));
    }
}
