# Use the SDK with Spring Boot

Cloudinary does not publish a Spring Boot starter, auto-configuration, or `@EnableCloudinary`
annotation for this SDK. Third-party starters on Maven Central are not maintained by Cloudinary.
Integration is a single `@Bean` method that exposes a `Cloudinary` instance.

## Dependency

```xml
<dependency>
    <groupId>com.cloudinary</groupId>
    <artifactId>cloudinary-http5</artifactId>
    <version>X.Y.Z</version>
</dependency>
```

## Configuration

`application.yml`:

```yaml
cloudinary:
  url: ${CLOUDINARY_URL}
```

Keep the value in the environment, not in the file. `CLOUDINARY_URL` is also what the SDK reads by
default, so this binding keeps one source of truth.

```java
package com.example.config;

import com.cloudinary.Cloudinary;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CloudinaryConfig {

    @Bean
    public Cloudinary cloudinary(@Value("${cloudinary.url}") String cloudinaryUrl) {
        return new Cloudinary(cloudinaryUrl);
    }
}
```

`Cloudinary` is safe to share as a singleton bean, which is the default scope; request scope is
unnecessary.

To bind individual properties instead of a connection URL (required when the cloud name contains an
underscore; see [configure-cloudinary.md](configure-cloudinary.md)), pass them as a map:

```java
@Bean
public Cloudinary cloudinary(
        @Value("${cloudinary.cloud-name}") String cloudName,
        @Value("${cloudinary.api-key}")    String apiKey,
        @Value("${cloudinary.api-secret}") String apiSecret) {
    return new Cloudinary(ObjectUtils.asMap(
            "cloud_name", cloudName,
            "api_key",    apiKey,
            "api_secret", apiSecret,
            "secure",     true));
}
```

### Validating configuration at startup

The `Cloudinary` constructor does not validate credentials; an incorrect value surfaces on the first
request. To detect it at startup, check the parsed configuration in the bean method:

```java
@Bean
public Cloudinary cloudinary(@Value("${cloudinary.url}") String cloudinaryUrl) {
    Cloudinary cloudinary = new Cloudinary(cloudinaryUrl);
    if (cloudinary.config.cloudName == null) {
        throw new IllegalStateException(
                "CLOUDINARY_URL did not parse. Does the cloud name contain an underscore?");
    }
    return cloudinary;
}
```

## Uploading a `MultipartFile`

`Uploader.upload` accepts `File`, `String`, `byte[]`, or `null`. An `InputStream` is not an accepted
type; `upload(multipartFile.getInputStream(), ...)` throws
`IOException: Unrecognized file parameter`. `uploadLarge` does accept an `InputStream`; see
[Large files without buffering in memory](#large-files-without-buffering-in-memory).

Pass the bytes:

```java
package com.example.web;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/images")
public class ImageUploadController {

    private final Cloudinary cloudinary;

    public ImageUploadController(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    @PostMapping
    public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(ObjectUtils.asMap("error", "No file supplied"));
        }
        try {
            Map result = cloudinary.uploader().upload(
                    file.getBytes(),                     // byte[], not getInputStream()
                    ObjectUtils.asMap(
                            "folder",          "uploads",
                            "filename",        file.getOriginalFilename(),
                            "resource_type",   "auto",
                            "allowed_formats", "jpg,png,webp,mp4"));

            return ResponseEntity.ok(ObjectUtils.asMap(
                    "assetId",   result.get("asset_id"),
                    "publicId",  result.get("public_id"),
                    "secureUrl", result.get("secure_url")));

        } catch (IOException e) {
            return ResponseEntity.status(500).body(ObjectUtils.asMap("error", "Could not read upload"));
        } catch (RuntimeException e) {
            // Upload failures are reported as RuntimeException carrying a message; log it server-side.
            return ResponseEntity.status(502).body(ObjectUtils.asMap("error", "Upload failed"));
        }
    }
}
```

Points to note:

- `file.getBytes()` supplies the payload. For very large files this buffers the whole payload in
  memory; see the section on large files below.
- `filename` gives the asset a meaningful name, since `byte[]` uploads otherwise have none.
- `resource_type: "auto"` accepts both images and videos; `allowed_formats` limits uploads to the
  listed formats, and any other format is rejected.
- The error response carries a fixed message. Log `e.getMessage()` on the server.
- Catch `RuntimeException`: the upload path reports failures as `RuntimeException` rather than a
  typed exception. See [troubleshoot-errors.md](troubleshoot-errors.md).

Spring's multipart limits apply before the request reaches your controller; raise them for large
uploads:

```yaml
spring:
  servlet:
    multipart:
      max-file-size: 100MB
      max-request-size: 100MB
```

### Large files without buffering in memory

For big uploads, pass the multipart stream to `uploadLarge`, which reads it in chunks, so the
application holds neither the whole payload in memory nor a temp copy of it:

```java
try (java.io.InputStream in = file.getInputStream()) {
    Map result = cloudinary.uploader().uploadLarge(in, ObjectUtils.asMap(
            "resource_type", "video",       // required for video; defaults to image
            "folder",        "videos",
            "filename",      file.getOriginalFilename()));
}
```

`uploadLarge` requires `resource_type` for anything other than an image — see
[upload-large-video.md](upload-large-video.md).

## Delivering URLs from a controller or template

`Url` is a mutable builder and is not thread-safe. Call `cloudinary.url()` for each URL rather than
storing an instance as a field on a singleton bean:

```java
@GetMapping("/{publicId}")
public String url(@PathVariable String publicId) {
    return cloudinary.url()
            .transformation(new Transformation().width(600).crop("fill")
                    .fetchFormat("auto").quality("auto"))
            .generate(publicId);
}
```

In Thymeleaf, expose the URL string and let the template build the tag; `th:src` and `th:alt`
escape attribute values. See [transform-and-deliver-image.md](transform-and-deliver-image.md#attributes-from-user-input).

## Async uploads

Uploads are blocking HTTP calls. Run them off the request thread where possible:

```java
@Async
public CompletableFuture<Map> uploadAsync(byte[] bytes) throws IOException {
    return CompletableFuture.completedFuture(
            cloudinary.uploader().upload(bytes, ObjectUtils.asMap("resource_type", "auto")));
}
```

For webhook-driven flows, pass `notification_url` and verify the callback with
`cloudinary.verifyNotificationSignature(...)` — see
[upload-large-video.md](upload-large-video.md).

## Testing

Unit tests can run without the live API. Inject a mock `Cloudinary`, or point a test configuration
at a throwaway product environment — see [get-credentials.md](get-credentials.md).

```java
@TestConfiguration
public class CloudinaryTestConfig {
    @Bean
    @Primary
    public Cloudinary cloudinary() {
        return new Cloudinary(ObjectUtils.asMap("cloud_name", "test-cloud"));
    }
}
```

A `Cloudinary` built with only a cloud name can generate URLs without credentials, which covers
URL-building tests without any network access.

## Related

- [configure-cloudinary.md](configure-cloudinary.md)
- [upload-image.md](upload-image.md)
- [use-with-jsp-taglib.md](use-with-jsp-taglib.md)
