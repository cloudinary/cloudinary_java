# AGENTS.md — cloudinary_java

## What this package is (one line)
Official Cloudinary server-side SDK for the JVM: upload assets, build transformation/delivery URLs, and call the Admin API from Java backends. The published artifact is `cloudinary-http5` (group `com.cloudinary`), built on Apache HttpClient 5.

## When to use this / when NOT to use this
- **Use this when:** your code runs on a **server or in the JVM** (Spring Boot, servlets, batch jobs, CLI tools) and needs signed uploads, signed delivery URLs, server-side URL/tag generation, or Admin API calls — i.e. anywhere the `api_secret` must stay private.
- **Do NOT use this when:** the code runs on an **Android device** (use [`cloudinary_android`](https://github.com/cloudinary/cloudinary_android) — it doesn't expect the secret on-device); or you want an idiomatic, coroutine-friendly **Kotlin** client (use [`cloudinary_kotlin`](https://github.com/cloudinary/cloudinary_kotlin)); or you need the no-code/agent path (use the Cloudinary MCP server).
- **Sibling packages / modules in this repo:** `cloudinary-core` (provider-agnostic core: URL/transformation builders, signing, params — no HTTP) → `cloudinary-http5` (HTTP transport on HttpClient 5, the artifact you depend on) → `cloudinary-taglib` (JSP tags). `cloudinary-test-common` holds shared test code only. The legacy `cloudinary-http45` artifact (HttpClient 4.5) belongs to the **1.x line only** — do not use it for 2.x.

## Setup
**Maven** (`pom.xml`):
```xml
<dependency>
    <groupId>com.cloudinary</groupId>
    <artifactId>cloudinary-http5</artifactId>
    <version>2.4.0</version>
</dependency>
```
**Gradle** (`build.gradle`):
```groovy
implementation 'com.cloudinary:cloudinary-http5:2.4.0'
```

Required configuration / credentials — set via env var (or system property), constructor, or per-call config:
```bash
export CLOUDINARY_URL=cloudinary://<api_key>:<api_secret>@<cloud_name>
```

## Minimal runnable example
```java
import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;

Cloudinary cloudinary = new Cloudinary(); // reads CLOUDINARY_URL

// Upload a local file (needs api_key + api_secret)
cloudinary.uploader().upload("my_picture.jpg", ObjectUtils.emptyMap());

// Build a transformation/delivery URL (cloud_name only)
String url = cloudinary.url()
    .transformation(new Transformation().width(100).height(150).crop("fill"))
    .generate("sample.jpg");
```

## Build / test commands (run these after editing)
This is a **multi-module Gradle** build; use the wrapper (`./gradlew`, or `gradlew.bat` on Windows). Requires **JDK 8+** (`sourceCompatibility`/`targetCompatibility = 1.8`).

```bash
./gradlew build                      # compile + assemble all modules
./gradlew :cloudinary-core:test      # unit tests for the core module
./gradlew :cloudinary-http5:test     # tests for the HTTP module
```
Integration tests hit a live Cloudinary cloud and need credentials. CI runs them per module via the `ciTest` task (excludes the `TimeoutTest` category) with `CLOUDINARY_URL` passed as a system property:
```bash
./gradlew -DCLOUDINARY_URL=$CLOUDINARY_URL ciTest -p cloudinary-http5 -i
```
The CI workflow first runs `./gradlew createTestSubAccount -PmoduleName=<core|http5|taglib>` to provision a throwaway test cloud (writes `tools/cloudinary_url.txt`); skip this for offline/unit work. There is no separate lint or formatter task in the Gradle build.

## Conventions & gotchas
- **Layered modules:** put provider-agnostic logic (URL building, signing, param handling) in `cloudinary-core`; only HTTP transport concerns belong in `cloudinary-http5`. Don't push core logic into the HTTP module.
- **Java 8 source level** — no Java 9+ language features or APIs in any module.
- **Secrets stay server-side:** signed uploads, signed delivery URLs, and Admin API calls require `api_secret`. Never ship it to a browser or Android bundle — that's the entire reason this server SDK exists.
- **Artifact coordinate:** the current artifact is `cloudinary-http5`. `cloudinary-http45` is the legacy HttpClient-4.5 coordinate for 1.x — update the coordinate when upgrading from 1.x.
- **Version support:** 2.0.0+ requires Java 8+; 1.1.0–1.39.0 supported Java 6+.

## Canonical docs (leave the repo for depth)
- Java SDK guide: https://cloudinary.com/documentation/java_integration
- Image/video manipulation: https://cloudinary.com/documentation/java_image_manipulation
- Transformation & API reference: https://cloudinary.com/documentation/cloudinary_references
- Maven Central artifact: https://central.sonatype.com/artifact/com.cloudinary/cloudinary-http5
- MCP server (agent/no-code path): https://github.com/cloudinary/mcp-servers

## Agent / MCP note
If the capability you need is also exposed via the Cloudinary MCP servers, prefer the MCP tool for autonomous task execution and use this SDK for code generation. See cloudinary/mcp-servers.

## Commit / PR conventions
- Open issues/PRs against https://github.com/cloudinary/cloudinary_java. (There is no `CONTRIBUTING.md` in this repo.)
- All matrix CI jobs (modules `core`, `http5`, `taglib` on JDK 8) must pass before merge.
- Add a `CHANGELOG.md` entry for user-facing changes.
