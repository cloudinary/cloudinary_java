# Platform capabilities and SDK scope

This page maps Cloudinary platform capabilities to the part of this SDK that implements them, or to
the route that provides them when the SDK does not. For a capability listed as "not in this SDK",
use the route named in the table.

## Upload and ingest

| I want to | In this SDK? | How |
|---|---|---|
| Upload a file, URL, or bytes | Yes | `uploader().upload(...)` — [upload-image.md](upload-image.md) |
| Upload a large file in chunks | Yes | `uploader().uploadLarge(...)` — [upload-large-video.md](upload-large-video.md) |
| Upload a non-media file | Yes | `uploader().uploadLargeRaw(...)`, or `resource_type=raw` |
| Sign a browser/mobile upload | Yes | `uploader().signRequestParams(...)` — [sign-browser-upload.md](sign-browser-upload.md) |
| Tag assets at or after upload | Yes | `tags` option; `uploader().addTag(...)` |
| Attach typed, validated fields | Yes | `metadata` option — [use-structured-metadata.md](use-structured-metadata.md) |
| Request moderation | Yes | `moderation` option — [moderate-upload.md](moderate-upload.md) |
| Report upload progress | No, on the server | `ProgressCallback` overloads compile but throw `IllegalArgumentException` with `cloudinary-http5`. Progress reporting is available on Android only. |
| Show a drop-in upload UI | No | The [Upload Widget](https://cloudinary.com/documentation/upload_widget.md), a JavaScript component. Your server signs; see [sign-browser-upload.md](sign-browser-upload.md). |
| Ingest from S3/Dropbox/etc. | No | [Auto-upload mappings](https://cloudinary.com/documentation/fetch_remote_images.md#auto_upload_remote_files), configured once via `api().createUploadMapping(...)`, then delivery-time fetch. |

## Delivery and transformation

| I want to | In this SDK? | How |
|---|---|---|
| Build a delivery URL | Yes | `url().generate(...)` — [transform-and-deliver-image.md](transform-and-deliver-image.md) |
| Resize, crop, effects, overlays | Yes | `Transformation` builder |
| Automatic format and quality | Yes | `.fetchFormat("auto").quality("auto")` |
| Emit an `<img>` or `<video>` tag | Yes | `url().imageTag(...)` / `videoTag(...)` for developer-written attributes; for user input see [transform-and-deliver-image.md](transform-and-deliver-image.md#attributes-from-user-input) |
| Sign a delivery URL | Yes | `url().signed(true)` |
| Named/stored transformations | Yes | `api().createTransformation(...)`, then `.named(...)` |
| Adaptive bitrate streaming | Partly | Request eager HLS/DASH at upload; profiles via `api().*StreamingProfile*`. The player is a separate JS component. |
| Play video with a Cloudinary player | No | The [Video Player](https://cloudinary.com/documentation/cloudinary_video_player.md), a JavaScript library. |
| Generate a responsive `srcset` | No | `imageTag` attributes are `String` values; passing a `Map` throws `ClassCastException`. Generate the URLs in a loop and assemble `srcset` in your template, or use Cloudinary's front-end SDKs, which build it directly. |
| Transform on the client | No | Delivery URLs are built server-side in this SDK; Cloudinary's front-end SDKs build them in the browser. |

## Management

| I want to | In this SDK? | How |
|---|---|---|
| List, filter, paginate assets | Yes | `api().resources(...)`, `search()` — [search-and-manage-assets.md](search-and-manage-assets.md) |
| Asset details | Yes | `api().resource(...)`, `api().resourceByAssetID(...)` |
| Rename, delete, restore | Yes | `uploader().rename/destroy`, `api().deleteResources`, `api().restore` |
| Folders | Yes | `api().createFolder/subFolders/deleteFolder`. `renameFolder` is disabled on many environments. |
| Usage and environment limits | Yes | `api().usage(...)` — read `media_limits` |
| Upload presets and mappings | Yes | `api().*UploadPreset*`, `api().*UploadMapping*` |
| Metadata field schema | Yes | `api().addMetadataField(...)` and the related methods |
| Create/manage sub-accounts, users | Yes | `com.cloudinary.provisioning.Account`, a separate class with its own credentials, distinct from `api()` |
| Verify a webhook signature | Yes | `cloudinary.verifyNotificationSignature(...)` |
| Backups and versions | Partly | `api().deleteBackedUpAssets(...)`, `restore(...)`; full version history is in the console and REST API |

## Analysis and AI

| I want to | In this SDK? | How |
|---|---|---|
| Analyze an asset | Yes | `api().analyze(...)` |
| Visual/similarity search | Yes | `api().visualSearch(...)` |
| Auto-tagging, OCR, face detection | Via add-ons | Passed as upload/`explicit` options. Each add-on requires a subscription; without one the upload throws `RuntimeException` with a message stating there is no active subscription. |
| Generative fill, background removal | Via add-ons / transformations | Transformation parameters, not dedicated SDK methods. |

Add-on failures do not arrive as authorization or rate-limit errors. See
[troubleshoot-errors.md](troubleshoot-errors.md).

## Outside this SDK

- Storage integration. This SDK does not provide a framework storage backend; wire uploads into
  your own persistence layer. Community integrations exist for various Java frameworks and are not
  maintained by Cloudinary.
- Image processing on your own machine. All transformation happens on Cloudinary's side, at
  delivery time.
- A CLI. See below.

## Other Cloudinary tooling

This SDK is one of several routes to the platform:

| Tool | Good for |
|---|---|
| [Cloudinary CLI](https://cloudinary.com/documentation/cloudinary_cli.md) | Scripted admin work, bulk operations, quick experiments without writing Java |
| [MCP servers](https://cloudinary.com/documentation/cloudinary_llm_mcp.md) | Letting an agent call Cloudinary directly — asset management, structured metadata, environment config |
| Cloudinary Skills | Packaged agent workflows |
| [`llms.txt` indexes](https://cloudinary.com/documentation/llms.txt) | Fetching the whole documentation index as text; per-product indexes are linked from it |
| `.md` documentation URLs | Any docs page with `.md` appended returns raw Markdown |

For one-off administration rather than application code, the CLI or an MCP server is typically a
shorter path than writing and running a Java program.

## Version and platform notes

- The 2.x line requires Java 8+.
- Android applications use the `cloudinary-android` SDK.
- The legacy `cloudinary-http42/43/44/45` artifacts are 1.x only. See
  [import-and-call.md](import-and-call.md).
- These pages carry no version number. They describe the current `master` of the SDK; for an older
  release, read the pages at that release's tag.
