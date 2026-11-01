/*
 * Build delivery URLs. This example makes no API calls and needs no credentials beyond a
 * cloud name, because URL building is entirely local.
 *
 * Prerequisites
 *   - CLOUDINARY_URL set in the environment, or just a cloud name. Only cloud_name is used.
 *   - cloudinary-http5 on the classpath. Nothing is sent, but the Cloudinary constructor
 *     requires an HTTP adapter and throws java.lang.UnknownError without one.
 *
 * Run
 *   javac -cp "<classpath>" -d out examples/TransformAndDeliver.java
 *   java  -cp "<classpath>:out" TransformAndDeliver
 *
 * How this differs in a real project
 *   URLs would be generated per request from IDs held in your datastore, and rendered by a
 *   template engine rather than printed.
 *
 * Docs: docs/transform-and-deliver-image.md
 *       docs/transform-and-deliver-video.md
 */

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;

public class TransformAndDeliver {

    private static final String IMAGE_PUBLIC_ID = "sample.jpg";
    private static final String VIDEO_PUBLIC_ID = "dog.mp4";

    public static void main(String[] args) {
        try {
            Cloudinary cloudinary = new Cloudinary();
            if (cloudinary.config.cloudName == null) {
                System.err.println("No Cloudinary configuration found. Set CLOUDINARY_URL.");
                System.exit(1);
            }

            System.out.println("plain:");
            System.out.println("  " + cloudinary.url().generate(IMAGE_PUBLIC_ID));

            System.out.println("optimized (f_auto,q_auto - apply this by default):");
            System.out.println("  " + cloudinary.url()
                    .transformation(new Transformation().fetchFormat("auto").quality("auto"))
                    .generate(IMAGE_PUBLIC_ID));

            System.out.println("square thumbnail, content-aware crop:");
            System.out.println("  " + cloudinary.url()
                    .transformation(new Transformation()
                            .width(300).height(300).crop("fill").gravity("auto")
                            .fetchFormat("auto").quality("auto"))
                    .generate(IMAGE_PUBLIC_ID));

            System.out.println("chained - crop, then sharpen, then optimize:");
            System.out.println("  " + cloudinary.url()
                    .transformation(new Transformation()
                            .width(400).height(400).crop("fill").gravity("auto")
                            .chain()
                            .effect("sharpen")
                            .chain()
                            .fetchFormat("auto").quality("auto"))
                    .generate(IMAGE_PUBLIC_ID));

            System.out.println("responsive srcset (assembled by hand - the SDK has no builder):");
            StringBuilder srcset = new StringBuilder();
            for (int width : new int[]{400, 800, 1200}) {
                if (srcset.length() > 0) {
                    srcset.append(", ");
                }
                srcset.append(cloudinary.url()
                                .transformation(new Transformation()
                                        .width(width).crop("scale")
                                        .fetchFormat("auto").quality("auto"))
                                .generate(IMAGE_PUBLIC_ID))
                      .append(" ").append(width).append("w");
            }
            System.out.println("  " + srcset);

            System.out.println("video, scaled:");
            System.out.println("  " + cloudinary.url().resourceType("video")
                    .transformation(new Transformation()
                            .width(640).crop("scale").quality("auto"))
                    .generate(VIDEO_PUBLIC_ID));

            System.out.println("video poster frame at 2s:");
            System.out.println("  " + cloudinary.url().resourceType("video").format("jpg")
                    .transformation(new Transformation()
                            .width(640).crop("fill").startOffset("2"))
                    .generate("dog"));

        } catch (IllegalArgumentException e) {
            System.err.println("Configuration error: " + e.getMessage());
            System.exit(1);
        } catch (RuntimeException e) {
            System.err.println("Failed: " + e.getMessage());
            System.exit(1);
        }
    }
}
