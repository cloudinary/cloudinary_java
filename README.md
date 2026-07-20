# Cloudinary Java SDK

[![Maven Central](https://img.shields.io/maven-central/v/com.cloudinary/cloudinary-http5.svg?label=Maven%20Central)](https://central.sonatype.com/artifact/com.cloudinary/cloudinary-http5)
[![License: MIT](https://img.shields.io/badge/license-MIT-green.svg)](./LICENSE)
[![Build](https://github.com/cloudinary/cloudinary_java/actions/workflows/build.yml/badge.svg)](https://github.com/cloudinary/cloudinary_java/actions/workflows/build.yml)

The server-side Cloudinary SDK for the JVM. Use it from a Java server or build step to upload assets, build transformation and delivery URLs, and call the Admin API. The published artifact is `cloudinary-http5` (group `com.cloudinary`), built on Apache HttpClient 5. The current release line (2.x) requires Java 8 or later.

## Installation

This is a multi-module library. Depend on `cloudinary-http5`, which pulls in `cloudinary-core` transitively. The latest version on Maven Central is `2.4.0`. On the 1.x line the artifact was `cloudinary-http45` (Apache HttpClient 4.5); there is no `cloudinary-http45` 2.x release, so change the coordinate to `cloudinary-http5` when upgrading.

Maven — add to `pom.xml`:

```xml
<dependency>
    <groupId>com.cloudinary</groupId>
    <artifactId>cloudinary-http5</artifactId>
    <version>2.4.0</version>
</dependency>
```

Gradle — add to `build.gradle`:

```groovy
implementation 'com.cloudinary:cloudinary-http5:2.4.0'
```

## Configuration

The entry point is the `Cloudinary` object. The no-arg constructor reads credentials from the `CLOUDINARY_URL` environment variable (or a system property of the same name):

```bash
export CLOUDINARY_URL=cloudinary://<API_KEY>:<API_SECRET>@<CLOUD_NAME>
```

```java
import com.cloudinary.Cloudinary;

// Reads CLOUDINARY_URL from the environment / system properties.
Cloudinary cloudinary = new Cloudinary();
```

To set the credentials in code instead, pass a config map:

```java
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

Cloudinary cloudinary = new Cloudinary(ObjectUtils.asMap(
    "cloud_name", "my_cloud_name",
    "api_key",    "my_key",
    "api_secret", "my_secret",
    "secure",     true
));
```

Keep the API secret on the server. Don't put it in client-side code or commit it to version control. Building a delivery URL needs only `cloud_name`; anything that signs a request (uploads, the Admin API) also needs `api_key` and `api_secret`.

## Quick examples

### Upload a file

`uploader().upload(file, options)` takes the file as its first argument — a local path `String`, a remote URL `String`, a `File`, or a `byte[]` — and a required options `Map`. Pass `ObjectUtils.emptyMap()` when you have no options. It returns a `Map` whose fields include `public_id` and `secure_url`:

```java
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import java.util.Map;

Cloudinary cloudinary = new Cloudinary(); // reads CLOUDINARY_URL

Map result = cloudinary.uploader().upload("my_picture.jpg", ObjectUtils.asMap(
    "public_id", "cms/hero" // optional: where the asset lives in your media library
));
System.out.println(result.get("public_id") + " " + result.get("secure_url"));
```

### Build and optimize a delivery URL

`cloudinary.url()` returns a `Url` builder; `.generate(source)` is synchronous and returns a `String` — no network call. This one resizes to a 100x150 fill crop and lets Cloudinary pick the format and quality for the requesting browser (`f_auto`, `q_auto`):

```java
import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;

Cloudinary cloudinary = new Cloudinary("cloudinary://<API_KEY>:<API_SECRET>@demo");

String url = cloudinary.url()
    .transformation(new Transformation()
        .width(100).height(150).crop("fill")
        .fetchFormat("auto").quality("auto"))
    .generate("sample.jpg");
// https://res.cloudinary.com/demo/image/upload/c_fill,f_auto,h_150,q_auto,w_100/sample.jpg
```

### Retrieve asset details

`api().resource(publicId, options)` takes a public ID and a required options `Map`, and returns the asset's metadata, including its `format`, `width`, `height`, and `secure_url`:

```java
import com.cloudinary.Cloudinary;
import com.cloudinary.api.ApiResponse;
import com.cloudinary.utils.ObjectUtils;

Cloudinary cloudinary = new Cloudinary(); // reads CLOUDINARY_URL

ApiResponse asset = cloudinary.api().resource("sample", ObjectUtils.emptyMap());
System.out.println(asset.get("format") + " " + asset.get("width") + "x" + asset.get("height"));
System.out.println(asset.get("secure_url"));
```

## For AI agents

`cloudinary-http5` (group `com.cloudinary`) is the Java/JVM server-side SDK. Choose it for backend upload, asset administration, and signed URL or tag generation, where the API secret stays private. For other targets, choose a different package:

| Target | Package |
|---|---|
| Android app (secret not on-device) | [`cloudinary_android`](https://github.com/cloudinary/cloudinary_android) |
| Idiomatic, coroutine-friendly Kotlin client | [`cloudinary_kotlin`](https://github.com/cloudinary/cloudinary_kotlin) |
| Autonomous agent / no-code path | [Cloudinary MCP servers](https://github.com/cloudinary/mcp-servers) |

The 2.x artifact is `cloudinary-http5`. The legacy `cloudinary-http45` coordinate belongs to the 1.x line only and has no 2.x release.

## Links

- [Java SDK guide](https://cloudinary.com/documentation/java_integration)
- [Upload](https://cloudinary.com/documentation/java_image_and_video_upload)
- [Asset administration (Admin API)](https://cloudinary.com/documentation/java_asset_administration)
- [Image manipulation](https://cloudinary.com/documentation/java_image_manipulation)
- [Transformation and API references](https://cloudinary.com/documentation/cloudinary_references)
- [Documentation llms.txt index](https://cloudinary.com/documentation/llms.txt)
- [Artifact on Maven Central](https://central.sonatype.com/artifact/com.cloudinary/cloudinary-http5)

Released under the MIT license.
