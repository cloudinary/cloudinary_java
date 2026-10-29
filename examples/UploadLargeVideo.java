/*
 * Upload a video with the chunked uploader and print its streaming-ready URLs.
 *
 * Prerequisites
 *   - CLOUDINARY_URL set in the environment.
 *   - cloudinary-http5 on the classpath.
 *   - A local video file. This example downloads one from the Cloudinary demo cloud if
 *     SOURCE_FILE does not exist, so it is runnable with no setup.
 *
 * Run
 *   javac -cp "<classpath>" -d out examples/UploadLargeVideo.java
 *   java  -cp "<classpath>:out" UploadLargeVideo
 *
 * How this differs in a real project
 *   A real application would stream a user upload to a temporary file, pass
 *   notification_url instead of blocking on the response, and delete the temp file in a
 *   finally block.
 *
 * Docs: docs/upload-large-video.md
 */

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Map;

public class UploadLargeVideo {

    private static final String SOURCE_URL =
            "https://res.cloudinary.com/demo/video/upload/dog.mp4";
    private static final String SOURCE_FILE = "example-video.mp4";
    private static final String PUBLIC_ID = "examples/upload_large_video";

    public static void main(String[] args) {
        try {
            Cloudinary cloudinary = new Cloudinary();
            if (cloudinary.config.cloudName == null) {
                System.err.println("No Cloudinary configuration found. Set CLOUDINARY_URL.");
                System.exit(1);
            }

            File video = new File(SOURCE_FILE);
            if (!video.exists()) {
                System.out.println("Downloading a sample video to " + SOURCE_FILE + " ...");
                try (InputStream in = new URL(SOURCE_URL).openStream()) {
                    Files.copy(in, video.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
            }

            // resource_type is REQUIRED here. uploadLarge defaults to "image" and fails
            // with "Invalid image file" on a video without it.
            Map result = cloudinary.uploader().uploadLarge(video, ObjectUtils.asMap(
                    "public_id",     PUBLIC_ID,
                    "resource_type", "video",
                    "overwrite",     true,
                    "chunk_size",    20_000_000));

            System.out.println("public_id:     " + result.get("public_id"));
            System.out.println("asset_id:      " + result.get("asset_id"));
            System.out.println("resource_type: " + result.get("resource_type"));
            System.out.println("format:        " + result.get("format"));
            System.out.println("duration:      " + result.get("duration") + "s");
            System.out.println("bytes:         " + result.get("bytes"));
            System.out.println("secure_url:    " + result.get("secure_url"));

            String scaled = cloudinary.url().resourceType("video")
                    .transformation(new Transformation()
                            .width(640).crop("scale").quality("auto"))
                    .generate(PUBLIC_ID + "." + result.get("format"));
            System.out.println("scaled:        " + scaled);

            String poster = cloudinary.url().resourceType("video").format("jpg")
                    .transformation(new Transformation()
                            .width(640).crop("fill").startOffset("2"))
                    .generate(PUBLIC_ID);
            System.out.println("poster:        " + poster);

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
