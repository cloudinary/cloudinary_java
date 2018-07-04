package com.cloudinary;

import java.util.Map;

/**
 * This class is used to configure tag generation options when calling {@link Url#imageTag(String, TagOptions)}
 */
public class TagOptions {
    private Map<? extends String, ? extends String> attributes;
    private Srcset srcset;

    /**
     * Set attributes to use in the generated tag
     * @param attributes A map of values to add as attributes to the tag
     * @return
     */
    public TagOptions attributes(Map<String, String> attributes) {
        this.attributes = attributes;
        return this;
    }

    /**
     * Send an {@link Srcset} instance fo configure srcset generation for tag.
     * @param srcset
     * @return
     */
    public TagOptions srcset(Srcset srcset){
        this.srcset = srcset;
        return this;
    }

    Map<? extends String, ? extends String> getAttributes() {
        return attributes;
    }

    /**
     * Get the srcset configuration
     * @return The srcset instance
     */
    public Srcset getSrcset() {
        return srcset;
    }
}
