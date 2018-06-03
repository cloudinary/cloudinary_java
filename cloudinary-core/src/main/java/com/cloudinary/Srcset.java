package com.cloudinary;

import com.cloudinary.cache.ResponsiveBreakpointPayload;
import com.cloudinary.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is used to configure srcset and sizes attributes for image tags. Used with {@link TagOptions#srcset(Srcset)}
 * when calling {@link Url#imageTag(String, TagOptions)}, or directly using the Cloudinary tag lib library.
 */
public class Srcset {
    private int[] breakpoints = null;
    private boolean sizes = false;
    private int minWidth = 0;
    private int maxWidth = 0;
    private int bytesStepSize = 0;
    private int maxImages = 0;

    /**
     * Get an instance configured for specific width
     *
     * @param breakpoints An array of integers specifying the required widths, in pixels.
     */
    public Srcset(int[] breakpoints) {
        this.breakpoints = breakpoints;
    }

    /**
     * Get an instance configured for widths covering the requests range
     *
     * @param minWidth  The width of the smallest image in pixels
     * @param maxWidth  The width of the largest image in pixels
     * @param maxImages The total count of generated images.
     */
    public Srcset(int minWidth, int maxWidth, int maxImages) {
        this(minWidth, maxWidth, maxImages, 0);
    }

    /**
     * Get an instance configured for widths covering the requests range
     *
     * @param minWidth      The width of the smallest image in pixels
     * @param maxWidth      The width of the largest image in pixels
     * @param maxImages     The total count of generated images.
     * @param bytesStepSize Step size in bytes, between breakpoints
     */
    public Srcset(int minWidth, int maxWidth, int maxImages, int bytesStepSize) {
        this.minWidth = minWidth;
        this.maxWidth = maxWidth;
        this.maxImages = maxImages;
        this.bytesStepSize = bytesStepSize;
    }

    private int[] calculateBreakpoints() {
        int pixelStepSize = (int) Math.round(Math.ceil((float) (maxWidth - minWidth)) /
                (maxImages > 1 ? maxImages - 1 : maxImages));
        int curr = minWidth;
        int[] breakpoints = new int[maxImages];
        for (int i = 0; i < maxImages; i++) {
            breakpoints[i] = curr;
            curr += pixelStepSize;
        }

        return breakpoints;
    }

    /**
     * Set the sizes param to `true` to generate a sizes attribute.
     *
     * @param sizes
     * @return
     */
    public Srcset sizes(boolean sizes) {
        this.sizes = sizes;
        return this;
    }

    /**
     * Generate the srcset attributes according to the chosen config, using the cache if required.
     * Note: The
     *
     * @param cloudinary The cloudinary instance to use (For config).
     * @param publicId   The public id of the resource to generate a url for.
     * @param baseUrl    The original url used, before calling `generate()`
     * @return The srcset result to use in the tag.
     */
    SrcsetResult generateSrcset(Cloudinary cloudinary, String publicId, Url baseUrl, ResponsiveBreakpointsProvider.CacheKey cacheKey) {
        if (breakpoints == null) {
            if (cloudinary.config.useResponsiveBreakpointsProvider) {

                ResponsiveBreakpointPayload res = cloudinary.breakpointsProvider.get(
                        baseUrl.clone(),
                        cacheKey,
                        minWidth,
                        maxWidth,
                        bytesStepSize,
                        maxImages);

                if (res != null) {
                    breakpoints = res.getBreakpoints();
                }
            }

            if (breakpoints == null) {
                breakpoints = calculateBreakpoints();
            }

        }

        Url current = baseUrl.clone();

        final String baseTransform = current.transformation != null ? current.transformation.generate() : "";
        List<String> srcsetItems = new ArrayList<>(breakpoints.length);

        String generatedUrl = null;
        for (int breakpoint : breakpoints) {
            current = current.clone();
            current.transformation(new Transformation().rawTransformation(baseTransform + "/c_scale,w_" + breakpoint));
            generatedUrl = current.generate(publicId);
            srcsetItems.add(generatedUrl + " " + breakpoint + "w");
        }

        // last generated url is the largest
        return new SrcsetResult(StringUtils.join(srcsetItems, ", "), generatedUrl);
    }

    String generateSizes() {
        String format = "(max-width: %dpx) %dpx";
        List<String> sizes = new ArrayList<>(breakpoints.length);

        for (int breakpoint : breakpoints) {
            sizes.add(String.format(format, breakpoint, breakpoint));
        }

        return StringUtils.join(sizes, ", ");
    }

    boolean hasSizes() {
        return sizes;
    }

    /**
     * Hold the results of the srcset generation
     */
    static final class SrcsetResult {
        final String srcset;
        final String largestBreakpoint;

        SrcsetResult(String srcset, String largestBreakpointUrl) {
            this.srcset = srcset;
            this.largestBreakpoint = largestBreakpointUrl;
        }
    }
}
