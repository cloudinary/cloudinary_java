@AGENTS.md

# CLAUDE.md — cloudinary_java

## Claude Code-specific notes

**Primary reference:** `AGENTS.md` (imported above) covers setup, build commands, conventions, and gotchas. Read it before touching any file.

## What this repo is

`cloudinary_java` is the official server-side Cloudinary SDK for the JVM. The published artifact is `cloudinary-http5` (group `com.cloudinary`), built on Apache HttpClient 5. Use it from Java backends — Spring Boot, servlets, batch jobs — where the `api_secret` must stay private.

## Key constraints / gotchas

- **Multi-module Gradle build.** Depend on `cloudinary-http5`; it pulls in `cloudinary-core` transitively. Do not depend on `cloudinary-core` directly for HTTP work.
- **Artifact coordinate changed at 2.x.** The legacy `cloudinary-http45` coordinate belongs to the 1.x line only — there is no `cloudinary-http45:2.x`. Change the `artifactId` to `cloudinary-http5` when upgrading from 1.x.
- **Java 8 source level.** No Java 9+ language features or APIs anywhere in the codebase (`sourceCompatibility = targetCompatibility = 1.8`).
- **`api_secret` is server-only.** Never ship it to a browser or Android bundle. Use the signed-upload pattern (server signs, browser posts directly to Cloudinary) to keep the secret off the client.
- **Not for Android.** Use [`cloudinary_android`](https://github.com/cloudinary/cloudinary_android). Not for idiomatic Kotlin: use [`cloudinary_kotlin`](https://github.com/cloudinary/cloudinary_kotlin).
- **Integration tests need a live cloud.** CI provisions a throwaway sub-account via `./gradlew createTestSubAccount`. For offline/unit work, skip that step — unit tests in `cloudinary-core` and `cloudinary-http5` run without credentials.

## Verified build commands

```bash
./gradlew build                                                       # compile + assemble all modules
./gradlew :cloudinary-core:test                                       # unit tests, no credentials needed
./gradlew :cloudinary-http5:test                                      # HTTP module unit tests

# Full integration suite (needs CLOUDINARY_URL):
./gradlew -DCLOUDINARY_URL=$CLOUDINARY_URL ciTest -p cloudinary-http5 -i
```

Use `gradlew.bat` instead of `./gradlew` on Windows. There is no separate lint or formatter task in the Gradle build.
