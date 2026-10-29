# Upload a large video or other big file

## Upload with `uploadLarge`

Use `uploadLarge` for files above roughly 100 MB. It splits the file into chunks and uploads them
sequentially, so a single request never has to carry the whole payload.

```java
Map result = cloudinary.uploader().uploadLarge(new File("clip.mp4"),
        ObjectUtils.asMap("public_id", "clips/intro", "resource_type", "video"));

// -> resource_type=video  format=mp4  duration=13.4134  bytes=9094354
```

`resource_type` defaults to `image`. Pass `video` for video files, or `auto` when the file type is
not known ahead of time. If `resource_type` is omitted for a video, the upload throws
`java.lang.RuntimeException: Invalid image file`.

```java
cloudinary.uploader().uploadLarge(userFile, ObjectUtils.asMap("resource_type", "auto"));
```

### Chunk size

```java
Map result = cloudinary.uploader().uploadLarge(
        new File("/path/to/feature.mp4"),
        ObjectUtils.asMap(
                "public_id",     "clips/feature",
                "resource_type", "video",
                "chunk_size",    20_000_000));      // bytes; default is 20 MB
```

`chunk_size` is in bytes. Larger chunks mean fewer requests but more to re-send if one fails.

For non-media files there is `uploadLargeRaw` — see below.

### Chunking and the asset size limit

Chunking works around per-request limits. It does not raise the maximum asset size for your product
environment. Read the current limits with `usage`:

```java
Map usage = cloudinary.api().usage(ObjectUtils.emptyMap());
System.out.println(usage.get("media_limits"));
// {video_max_size_bytes=21474836480, image_max_size_bytes=21474836480,
//  image_max_px=50000000, raw_max_size_bytes=21474836480,
//  asset_max_total_px=300000000}
```

A "file size too large" error means one of these limits was reached, not a fixed API ceiling. See
[troubleshoot-errors.md](troubleshoot-errors.md).

## What comes back

Same shape as a regular upload, plus video-specific fields:

| Field | Example |
|---|---|
| `resource_type` | `video` |
| `format` | `mp4` |
| `duration` | `13.4134` (seconds) |
| `width`, `height` | `1920`, `1080` |
| `bytes` | `9094354` |
| `playback_url` | HLS URL, when adaptive streaming applies |
| `asset_id` | `abcdef0123456789abcdef0123456789` |

## Progress callbacks

`Uploader` has `ProgressCallback` overloads — `uploadLarge(file, options, callback)` and others.
Progress reporting is implemented by the Android adapter (`cloudinary-android`). On
`cloudinary-http5`, `UploaderStrategy.callApi` rejects any non-null callback:

```java
ProgressCallback callback = (bytesUploaded, totalBytes) -> { /* ... */ };

cloudinary.uploader().uploadLarge(new File("feature.mp4"),
        ObjectUtils.asMap("resource_type", "video"), callback);

// -> java.lang.IllegalArgumentException: Progress callback is not supported
```

On the server, report progress per file. The
`uploadLarge(file, options, bufferSize, offset, uniqueUploadId, callback)` overload uploads every
chunk from `offset` to the end of the file in one call, so it offers no per-chunk hook either.

## `uploadLargeRaw` for non-media files

`uploadLargeRaw` sets `resource_type` to `raw`, so you do not pass it:

```java
Map result = cloudinary.uploader().uploadLargeRaw(
        new File("archive.zip"), ObjectUtils.asMap("public_id", "backups/archive"));

// -> resource_type=raw
```

For raw assets the file extension is appended to the `public_id`: uploading `archive.zip` with
`"public_id", "backups/archive"` stores the asset as `backups/archive.zip`. Read `public_id` from
the response.

## Long uploads and processing

Video processing continues after the upload response returns. Two consequences:

- A delivery URL may return 423 while processing is still in progress. Retry with backoff; see
  [troubleshoot-errors.md](troubleshoot-errors.md).
- For anything large, prefer asynchronous notification over polling. Pass `notification_url` and
  let Cloudinary call you back:

```java
cloudinary.uploader().uploadLarge(new File("feature.mp4"), ObjectUtils.asMap(
        "resource_type",    "video",
        "notification_url", "https://example.com/hooks/cloudinary"));
```

Verify the callback before acting on it:

```java
boolean valid = cloudinary.verifyNotificationSignature(
        requestBody,
        request.getHeader("X-Cld-Timestamp"),
        request.getHeader("X-Cld-Signature"),
        7200L);                                   // validity window in seconds
```

Consider raising `timeout` in configuration for large synchronous uploads. It defaults to `0`,
meaning the SDK sets no timeout and HttpClient's default 3-minute read timeout applies; an upload
whose response takes longer fails with `java.net.SocketTimeoutException: Read timed out`. A
positive value replaces that limit.

## Related

- [upload-image.md](upload-image.md) — accepted input types and how unknown parameters are handled
- [transform-and-deliver-video.md](transform-and-deliver-video.md)
- [Upload API reference](https://cloudinary.com/documentation/image_upload_api_reference.md)
