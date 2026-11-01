# Cloudinary Java SDK

Upload, transform, optimize, and manage images and videos with Cloudinary from Java — the
`com.cloudinary` artifacts on Maven Central.

[![Build](https://github.com/cloudinary/cloudinary_java/actions/workflows/build.yml/badge.svg)](https://github.com/cloudinary/cloudinary_java/actions/workflows/build.yml)
[![Maven Central](https://img.shields.io/maven-central/v/com.cloudinary/cloudinary-http5.svg)](https://central.sonatype.com/artifact/com.cloudinary/cloudinary-http5)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)

## Install

```xml
<dependency>
    <groupId>com.cloudinary</groupId>
    <artifactId>cloudinary-http5</artifactId>
    <version>2.6.0</version>
</dependency>
```

```groovy
implementation 'com.cloudinary:cloudinary-http5:2.6.0'
```

For JSP tags, also add `cloudinary-taglib`.

## Quick start

Set your API environment variable (Console > Settings > API Keys):

```bash
export CLOUDINARY_URL=cloudinary://<api_key>:<api_secret>@<cloud_name>
```

Upload an image and get an optimized delivery URL:

```java
import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;
import java.util.Map;

public class QuickStart {
    public static void main(String[] args) {
        try {
            // Reads CLOUDINARY_URL from the environment
            Cloudinary cloudinary = new Cloudinary();

            // Upload a remote image (a local File or path works the same way)
            Map result = cloudinary.uploader().upload(
                    "https://res.cloudinary.com/demo/image/upload/sample.jpg",
                    ObjectUtils.asMap("public_id", "quickstart-sample"));
            System.out.println("Uploaded: " + result.get("public_id"));

            // Build a 400x400 auto-cropped URL with automatic format and quality
            String url = cloudinary.url()
                    .transformation(new Transformation()
                            .width(400).height(400).crop("fill").gravity("auto")
                            .fetchFormat("auto").quality("auto"))
                    .generate((String) result.get("public_id"));
            System.out.println("Optimized URL: " + url);
        } catch (Exception e) {
            System.err.println("Quick start failed: " + e.getMessage());
            System.err.println("Check that CLOUDINARY_URL is set (Console > Settings > API Keys).");
            System.exit(1);
        }
    }
}
```

Save as `QuickStart.java`, compile against `cloudinary-http5` and its dependencies, and run.
[Create a free account](https://cloudinary.com/users/register_free) if you don't have one — see
[Get credentials](docs/get-credentials.md) for the options, including a throwaway environment for
testing.

## Common tasks

- [Get Cloudinary credentials](docs/get-credentials.md)
- [Add the dependency and make a first call](docs/import-and-call.md)
- [Configure credentials in code, environment, or a framework](docs/configure-cloudinary.md)
- [Upload an image](docs/upload-image.md)
- [Upload a large video](docs/upload-large-video.md)
- [Sign a browser upload](docs/sign-browser-upload.md)
- [Transform and deliver an image](docs/transform-and-deliver-image.md)
- [Transform and deliver a video](docs/transform-and-deliver-video.md)
- [Search and manage assets](docs/search-and-manage-assets.md)
- [Moderate an upload](docs/moderate-upload.md)
- [Use structured metadata](docs/use-structured-metadata.md)
- [Use with Spring Boot](docs/use-with-spring-boot.md)
- [Use the JSP tag library](docs/use-with-jsp-taglib.md)
- [Troubleshoot errors](docs/troubleshoot-errors.md)

Runnable versions live in [`examples/`](examples/) — each is a complete file you can run directly.

## When to use this SDK

Use this library in **server-side Java**: uploads, signed operations, asset administration,
search, moderation, and delivery URL generation.

For other jobs, better-fitting tools exist:

- Android apps: the [Android SDK](https://cloudinary.com/documentation/android_integration) ([md](https://cloudinary.com/documentation/android_integration.md)); `cloudinary-http5` targets server-side JVMs.
- Complete in-browser upload UI: [Upload Widget](https://cloudinary.com/documentation/upload_widget) ([md](https://cloudinary.com/documentation/upload_widget.md)).
- Text-to-image generation and image-to-video: [platform APIs](https://cloudinary.com/documentation/image_generation_addon) ([md](https://cloudinary.com/documentation/image_generation_addon.md)), not wrapped by this library.
- Multi-step media workflow automation: [MediaFlows](https://cloudinary.com/documentation/mediaflows_user_guide) ([md](https://cloudinary.com/documentation/mediaflows_user_guide.md)).
- Interactive agent-driven asset operations: [Cloudinary MCP servers and Skills](https://cloudinary.com/documentation/cloudinary_llm_mcp) ([md](https://cloudinary.com/documentation/cloudinary_llm_mcp.md)).

The full capability map — plus the Skills, MCP servers, and CLI worth setting up first — is in
[docs/platform-capabilities.md](docs/platform-capabilities.md).

## Status and compatibility

Stable, actively maintained. See [CHANGELOG.md](CHANGELOG.md).

| SDK version    | Java                          |
|----------------|-------------------------------|
| 2.x            | 8 and later                   |
| 1.1.0 – 1.39.0 | 6 and later (no longer maintained) |

`cloudinary-taglib` targets `javax.servlet` / `javax.servlet.jsp`, not Jakarta EE 9+.

## Documentation

- [Task docs for this repository](docs/README.md) — snippets compile against this SDK, and
  documented behaviour was observed against a live environment.
- [Java SDK guide](https://cloudinary.com/documentation/java_integration) — the full documentation
  ([md](https://cloudinary.com/documentation/java_integration.md)).

Documentation links in this README point at the browsable HTML page, with an `(md)` companion
link that returns the same page as raw Markdown. Inside `docs/` and `examples/` the links are
Markdown-only, since those files are written to be read by coding agents. Either form works for
any page: add `.md` for Markdown, drop it for HTML.

## For AI coding agents

- Contributing to this repo: read [AGENTS.md](AGENTS.md).
- Using the SDK in your own project: the pages in [`docs/`](docs/README.md) describe this SDK's
  behaviour; start with [platform-capabilities](docs/platform-capabilities.md) for what the
  library covers. They describe the 2.x API.

## Support

- SDK bugs and feature requests: [GitHub issues](https://github.com/cloudinary/cloudinary_java/issues)
- Account and platform questions: [Cloudinary support](https://support.cloudinary.com)

## Security

See [SECURITY.md](SECURITY.md) for private vulnerability reporting. Keep your `api_secret` in
server-side code; for client uploads, use the server-signed pattern in
[Sign a browser upload](docs/sign-browser-upload.md).

## License

Released under the MIT license — see [LICENSE](LICENSE). Copyright (c) Cloudinary Ltd.
