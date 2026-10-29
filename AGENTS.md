# Contributor guide for coding agents

This file is for agents contributing to this repository. If you are *using* the SDK in another
project, read [`docs/`](docs/README.md) instead — task-oriented pages for agents using the SDK.

## Commands

Use JDK 8 or 11; CI runs on JDK 8. The Gradle 6.0.1 wrapper fails on JDK 17 and later before any
task runs.

```bash
./gradlew :cloudinary-core:jar :cloudinary-http5:jar :cloudinary-taglib:jar   # build the published modules
./gradlew :cloudinary-core:test                          # unit tests, no cloud needed
./gradlew createTestSubAccount -PmoduleName=http5        # throwaway cloud -> tools/cloudinary_url.txt
source tools/cloudinary_url.txt                          # sets CLOUDINARY_URL
./gradlew ciTest -p cloudinary-http5 -DCLOUDINARY_URL="$CLOUDINARY_URL"   # integration tests, as CI runs them
```

Gradle passes `-DCLOUDINARY_URL` to the test JVM as the `CLOUDINARY_URL` environment variable
(`java_shared.gradle`). `tools/cloudinary_url.txt` is git-ignored and contains live credentials;
never commit it or paste its contents anywhere.

## Testing

- `cloudinary-core` tests run offline. `cloudinary-http5` tests need a live product environment.
  `cloudinary-taglib` has no tests.
- Use a fresh sub-account for each integration run. Assets and metadata fields left by earlier
  runs can fail unrelated tests.
- `java` snippets in `docs/` must compile against the built jars, and links in `docs/` must
  resolve.

## Project structure

| Path | Published | Purpose |
|---|---|---|
| `cloudinary-core/` | Yes | URL/transformation building, parameter serialisation. No HTTP client, no runtime dependencies. |
| `cloudinary-http5/` | Yes | Apache HttpClient 5 transport. **This is what users depend on.** |
| `cloudinary-taglib/` | Yes | JSP tags. Targets `javax.*`, not Jakarta EE 9+. |
| `cloudinary-test-common/` | Yes | Shared test helpers. Not for application code. |
| `docs/` | No | Task pages for agents *using* the SDK. |
| `examples/` | No | One runnable program per doc page where the task is genuinely runnable. |
| `samples/` | No | Legacy sample applications. Not in the build. |
| `tools/` | No | Release and test-cloud scripts. |

`settings.gradle` includes the four modules above.

`org.cloudinary.json` inside `cloudinary-core` is a vendored JSON implementation. It exists to
keep the core dependency-free. Do not replace it with Jackson or Gson.

`docs/` and `examples/` are not packaged into any jar; the published `-javadoc.jar` is the
documentation attached to each artifact.

Keep examples uniform: one entry point taking no arguments, no CLI parsing, constants at the top,
and one error-handling block mapping every failure to a non-zero exit with a readable message.

## Code style

- The build targets **Java 8** (`sourceCompatibility = 1.8` in `java_shared.gradle`). Do not use
  newer language features in `src/main`.
- There is no linter, formatter, or static-analysis tool. Match the surrounding code.
- Options are untyped `Map`s built with `ObjectUtils.asMap(k, v, ...)`; responses are `Map`s keyed
  by the raw API field names (`public_id`, `secure_url`, `asset_id`). There are no typed response
  objects.
- `Cloudinary` is safe to share as a singleton. `Url` is a **mutable** builder and is not
  thread-safe; call `cloudinary.url()` per URL.

## Docs

- Docs describe this SDK. Link Cloudinary documentation in `.md` form
  (`https://cloudinary.com/documentation/<page>.md`); `README.md` may link HTML. Do not recommend
  third-party packages.
- Prefer `asset_id` (immutable) over `public_id` (changes on rename) when showing how to store a
  reference. Five `Api` methods accept an asset ID; nothing in `Uploader` does.
- `README.md` shows the released version, which the release script updates. Pages in `docs/` use
  `X.Y.Z`, because the release script does not touch them.

## Git workflow

- Branch from `master`; keep changes focused; one topic per pull request.
- Do not edit `CHANGELOG.md` or bump the version. Releases run `tools/update_version.sh <version>`,
  which rewrites `Cloudinary.VERSION` in `Cloudinary.java`, `version=` in `gradle.properties`, and
  the version in `README.md`. It reads the `VERSION` line with a regex; do not reformat it.
- Keep secrets out of everything — code, tests, fixtures, docs, commit messages. Grep the diff
  for `cloudinary://` with real values before pushing.
- Never commit `tools/cloudinary_url.txt`, `/out/`, or media left behind by running examples.

## Boundaries

**Always**
- Verify claims in `docs/` against the source and, where possible, by running them. Do not copy
  another Cloudinary SDK's docs; behaviour differs between the SDKs.
- Keep `docs/` and `examples/` consistent with the code they document.
- Keep API secrets out of examples, docs, tests, and fixtures.

**Ask first**
- Changing the published artifact list, packaging, or what ships inside a jar.
- Adding a dependency. `cloudinary-core` has no runtime dependencies.
- Changing public API signatures or behaviour. Document current behaviour and raise an issue
  before changing it. This includes these behaviours, which `docs/` describes and callers may
  depend on:
  1. Error types differ by entry point: `java.lang.RuntimeException` carrying a message for
     uploads (`AbstractUploaderStrategy.processResponse`), typed `ApiException` subclasses for
     the Admin API, and `java.lang.UnknownError` (an `Error`, not an `Exception`) when no HTTP
     adapter is present.
  2. `EnumMetadataField` has a package-private constructor and no factory, unlike
     `SetMetadataField`. Application code uses `new MetadataField<String>(MetadataFieldType.ENUM)`.
  3. `ProgressCallback` overloads compile on `cloudinary-http5` and throw
     `IllegalArgumentException: Progress callback is not supported` when called.
  4. `Url.resourcType(String)` is retained alongside `resourceType(String)` for compatibility.
- Touching `publish.gradle`, `gradle.properties`, `MAVEN_CENTRAL_PUBLISHING_GUIDE.md`, or the
  release script.
- Adding a typed response object, a linter, or a formatter.
- Modifying the legacy sample apps in `samples/`.

**Never**
- Commit credentials or real account identifiers.
- Document a Cloudinary platform capability as an SDK method unless this library implements it
  (see `docs/platform-capabilities.md`).
