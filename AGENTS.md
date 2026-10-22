# Contributor guide for coding agents

This file is for agents contributing to this repository. If you are *using* the SDK in another
project, read [`docs/`](docs/README.md) instead — task-oriented pages for agents using the SDK.

## Commands

**Use JDK 11.** The Gradle 6.0.1 wrapper cannot run on a modern JVM: under JDK 17+ it fails
before any task with `Could not initialize class org.codehaus.groovy.reflection.ReflectionCache`.

```bash
export JAVA_HOME=/path/to/jdk-11
./gradlew :cloudinary-core:jar :cloudinary-http5:jar        # build the two artifacts users depend on
./gradlew :cloudinary-core:test                             # unit tests, no cloud needed
./gradlew :cloudinary-http5:test -DCLOUDINARY_URL="cloudinary://key:secret@cloud"   # live integration tests
./gradlew createTestSubAccount -PmoduleName=http5           # throwaway cloud -> tools/cloudinary_url.txt
```

`CLOUDINARY_URL` is read as a **system property** by the tests (see `java_shared.gradle`), not
from the environment. `tools/cloudinary_url.txt` is git-ignored and **contains live
credentials — never commit it or paste its contents anywhere.**

## Testing

- `cloudinary-core` tests are offline. Everything else needs a live product environment.
- Do not share one product environment between doc verification and the integration suite.
  Doc probes leave mandatory metadata fields and pending moderated assets behind, and
  `ApiTest.testGetResourcesWithMetadata` then fails for reasons unrelated to the branch. Use a
  fresh sub-account for each.
- For docs-only changes, run the suite against the base branch and compare failure counts
  rather than asserting "tests pass". The integration suite takes about 3.5 minutes plus a
  throwaway cloud.
- Before committing a `docs/` change, two cheap checks: every `java` snippet compiles against
  the built jars (wrap fragments in a harness with the ambient names — `cloudinary`, `file`,
  `publicId` — declared as fields, then `javac`; this catches invented methods, which is the
  whole point), and every internal link, cross-page anchor, and external URL resolves. GitHub
  **preserves underscores** in heading slugs, so a naive slugifier reports false positives.

## Project structure

| Path | Published | Purpose |
|---|---|---|
| `cloudinary-core/` | Yes | URL/transformation building, parameter serialisation. No HTTP client, no runtime dependencies. |
| `cloudinary-http5/` | Yes | Apache HttpClient 5 transport. **This is what users depend on.** |
| `cloudinary-taglib/` | Yes | JSP tags. Targets `javax.*`, not Jakarta EE 9+. |
| `cloudinary-test-common/` | Yes | Shared test helpers. Not for application code. |
| `docs/` | No | Task pages for agents *using* the SDK. Every claim produced by running something. |
| `examples/` | No | One runnable program per doc page where the task is genuinely runnable. |
| `samples/` | No | Legacy sample applications. Not in the build. |
| `tools/` | No | Release and test-cloud scripts. |

`settings.gradle` includes only the four modules. The `cloudinary-http42/43/44` directories
present in a working copy are **untracked leftovers** from the 1.x line — not in git, not in the
build. Do not add them back and do not "tidy" them as part of unrelated work.

`org.cloudinary.json` inside `cloudinary-core` is a vendored JSON implementation. It exists to
keep the core dependency-free. Do not replace it with Jackson or Gson.

**`docs/` is deliberately not packaged into any jar.** A survey of 15 widely-used Java libraries
on Maven Central — Guava, OkHttp, Jackson, Gson, commons-lang3, SLF4J, Retrofit, AWS SDK v2,
google-cloud-storage, Stripe, Twilio, SendGrid, java-jwt, Spring Core, jsoup — found that only
jsoup ships any Markdown: its 3 KB project README in `META-INF/`. Everything else carries
manifests, licences, and machine-consumed config only. Embedding these pages would grow
`cloudinary-core` by 27%, from 175 KB to 223 KB, for files no runtime reads, and retrieval
through `unzip -p` is slower than reading the repository or Cloudinary's `llms.txt`. Java's
convention for documentation attached to an artifact is the `-javadoc.jar`, which this SDK
already publishes. Reversing this needs a reason better than "other Cloudinary SDKs do it".

`examples/` is likewise not shipped: loose `.java` files inside a library artifact are dead
weight, and each doc page carries its full flow inline. Keep examples uniform — one entry point
taking no arguments, no CLI parsing, constants at the top, one error-handling block mapping every
failure to a non-zero exit with a readable message and no stack trace.

## Code style

- The build targets **Java 8** (`sourceCompatibility = 1.8` in `java_shared.gradle`). Do not use
  newer language features in `src/main`.
- There is **no linter, formatter, or static-analysis tool**, deliberately. Match the surrounding
  code and do not introduce one as a side effect.
- Options are untyped `Map`s built with `ObjectUtils.asMap(k, v, ...)`; responses are `Map`s keyed
  by the raw API field names (`public_id`, `secure_url`, `asset_id`). There are no typed response
  objects.
- `Cloudinary` is safe to share as a singleton. `Url` is a **mutable** builder and is not
  thread-safe; call `cloudinary.url()` per URL.
- In docs, prefer `asset_id` (immutable) over `public_id` (changes on rename) when showing how to
  store a reference. Only six `Api` methods accept an asset ID; nothing in `Uploader` does.
- Docs describe **this SDK**, not how it differs from other Cloudinary SDKs. Link Cloudinary docs
  in `.md` form (`https://cloudinary.com/documentation/<page>.md`); the repository `README.md`
  may link HTML, since people read it. Do not recommend third-party packages — community
  Java/Spring integrations exist and are not maintained by Cloudinary.
- Docs are reference documentation: state what the API does and how to use it. Do not frame
  documented behaviour as a bug or an oversight, and do not tell the reader what they might
  wrongly assume.
- Docs and README carry **no version literal**. Install snippets use `X.Y.Z` and point at the
  Maven Central badge and a `maven-metadata.xml` one-liner. The literal once drifted to a
  never-published `2.3.1` while Central was at `2.4.0`, and nothing rewrites `docs/` at release.

## Git workflow

- Branch from `master`; keep changes focused; one topic per pull request.
- Do not edit `CHANGELOG.md` or bump the version. Releases are a separate process:
  `tools/update_version.sh` rewrites `Cloudinary.VERSION` in `Cloudinary.java` and `version=`
  in `gradle.properties`. **It greps the `VERSION` line with a regex; do not reformat it.** Its
  `sed` over `README.md` is now a harmless no-op. `Cloudinary.VERSION` feeds the `User-Agent` and
  the `_a=` analytics parameter, so drift misreports SDK usage rather than breaking visibly.
- Keep secrets out of everything — code, tests, fixtures, docs, commit messages. Grep the diff
  for `cloudinary://` with real values before pushing.
- Never commit `tools/cloudinary_url.txt`, `/out/`, or media left behind by running examples.

## Boundaries

**Always**
- Produce every claim in `docs/` by running something — not by reading source, and not by
  copying another Cloudinary SDK's docs. Behaviour differs between the SDKs.
- Keep `docs/` and `examples/` consistent with the code they document.
- Keep API secrets out of examples, docs, tests, and fixtures.

**Ask first**
- Changing the published artifact list, packaging, or what ships inside a jar — including
  moving `docs/` into `src/main/resources`.
- Adding a dependency. `cloudinary-core` has no runtime dependencies at all, which is a feature
  for consumers.
- Changing public API signatures or behaviour. This is a widely deployed library; a behaviour
  change can break callers. Document current behaviour and raise an issue before changing it.
- Changing any of these documented behaviours. Each is part of the public contract as observed
  by execution and described in `docs/`; changing one is a behaviour change for existing
  callers:
  1. `Configuration.from()` parses with `java.net.URI`, so a cloud name containing an
     underscore yields `null` cloud name, key, and secret (`getHost()` and `getUserInfo()`
     return `null` for a hostname with an underscore). Such environments are configured with a
     `Map`. An alternative implementation would parse `getAuthority()` directly.
  2. Error types differ by entry point: `java.lang.RuntimeException` carrying a message for
     uploads (`AbstractUploaderStrategy.processResponse`), typed `ApiException` subclasses for
     the Admin API, and `java.lang.UnknownError` (an `Error`, not an `Exception`) when no HTTP
     adapter is present.
  3. `Api.CLOUDINARY_API_ERROR_CLASSES` maps HTTP 400/401/403/404/409/420/500 to typed
     exceptions; other status codes become `GeneralError`. This includes 429, which is not
     routed to `RateLimited`.
  4. `EnumMetadataField` has a package-private constructor and no factory, unlike
     `SetMetadataField`. Application code uses `new MetadataField<String>(MetadataFieldType.ENUM)`.
  5. `ProgressCallback` overloads compile on `cloudinary-http5` and throw
     `IllegalArgumentException: Progress callback is not supported` when called. Progress
     reporting is available in the Android SDK.
  6. `Url.resourcType(String)` is retained alongside `resourceType(String)` for compatibility.
     Both work.
  7. `Cloudinary.VERSION` and `version=` in `gradle.properties` are maintained separately and
     can drift; during 2.3.x they read `2.3.1` and `2.3.2`. Both read `2.5.0` at the time of
     writing.
- Touching `publish.gradle`, `gradle.properties`, `MAVEN_CENTRAL_PUBLISHING_GUIDE.md`, or the
  release script.
- Adding a typed response object, a linter, or a formatter.
- Modifying the legacy sample apps in `samples/`.

**Never**
- Commit credentials or real account identifiers.
- Document a Cloudinary platform capability as an SDK method unless this library implements it
  (see `docs/platform-capabilities.md`).
