/*
 * Upload an image and print its delivery URL.
 *
 * Prerequisites
 *   - CLOUDINARY_URL set in the environment:
 *       export CLOUDINARY_URL="cloudinary://<api_key>:<api_secret>@<cloud_name>"
 *   - cloudinary-http5 (and its transitive cloudinary-core) on the classpath.
 *
 * Run
 *   javac -cp "<classpath>" -d out examples/UploadImage.java
 *   java  -cp "<classpath>:out" UploadImage
 *
 * How this differs in a real project
 *   A real application would take the file from a request rather than a constant, would not
 *   print credentials-adjacent data, and would persist ASSET_ID in its own datastore instead
 *   of relying on the public ID.
 *
 * Docs: docs/upload-image.md
 */

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;

import java.io.IOException;
import java.util.Map;

public class UploadImage {

    private static final String SOURCE_URL =
            "https://res.cloudinary.com/demo/image/upload/sample.jpg";
    private static final String PUBLIC_ID = "examples/upload_image";

    public static void main(String[] args) {
        try {
            Cloudinary cloudinary = new Cloudinary();
            if (cloudinary.config.cloudName == null) {
                System.err.println("No Cloudinary configuration found. Set CLOUDINARY_URL.");
                System.exit(1);
            }

            Map result = cloudinary.uploader().upload(SOURCE_URL, ObjectUtils.asMap(
                    "public_id", PUBLIC_ID,
                    "overwrite", true));

            System.out.println("public_id:  " + result.get("public_id"));
            System.out.println("asset_id:   " + result.get("asset_id"));
            System.out.println("format:     " + result.get("format"));
            System.out.println("dimensions: " + result.get("width") + "x" + result.get("height"));
            System.out.println("secure_url: " + result.get("secure_url"));

            // Append the format the server reported, so the URL names a real file. A bare
            // public ID would produce ".../examples/upload_image" with no extension.
            String optimized = cloudinary.url()
                    .transformation(new Transformation()
                            .width(400).crop("fill").gravity("auto")
                            .fetchFormat("auto").quality("auto"))
                    .generate(PUBLIC_ID + "." + result.get("format"));
            System.out.println("optimized:  " + optimized);

        } catch (IllegalArgumentException e) {
            System.err.println("Configuration error: " + e.getMessage());
            System.exit(1);
        } catch (IOException e) {
            System.err.println("I/O error: " + e.getMessage());
            System.exit(1);
        } catch (RuntimeException e) {
            System.err.println("Upload failed: " + e.getMessage());
            System.exit(1);
        }
    }
}
