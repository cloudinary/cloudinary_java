# Upload an image

## The minimal call

```java
Map result = cloudinary.uploader().upload(
        new File("/path/to/local.jpg"),
        ObjectUtils.emptyMap());

System.out.println(result.get("public_id"));
System.out.println(result.get("secure_url"));
System.out.println(result.get("asset_id"));
```

`upload()` accepts several types as its first argument:

| Pass | Behaviour |
|---|---|
| `java.io.File` | Uploads the local file. |
| `String` that is a remote URL | Cloudinary fetches the URL server-side. |
| `String` that is a local path | Resolved to a `File`; throws `IOException` if missing or unreadable. |
| `String` that is a base64 data URI | Sent as a text body; Cloudinary decodes it. |
| `byte[]` | Uploads the bytes directly. |
| `null` | No file part is sent. |

`InputStream` is not supported; passing one throws
`java.io.IOException: Unrecognized file parameter java.io.ByteArrayInputStream@...`. Read the
stream into a `byte[]` first:

```java
ByteArrayOutputStream buffer = new ByteArrayOutputStream();
byte[] chunk = new byte[8192];
for (int n; (n = inputStream.read(chunk)) != -1; ) {
    buffer.write(chunk, 0, n);
}
byte[] bytes = buffer.toByteArray();
cloudinary.uploader().upload(bytes, ObjectUtils.asMap("filename", "upload.jpg"));
```

Pass `filename` alongside `byte[]`; without it the upload is named `file` with no extension.

Uploading from a remote URL means Cloudinary fetches the file, so the bytes never pass through your
application:

```java
Map result = cloudinary.uploader().upload(
        "https://res.cloudinary.com/demo/image/upload/sample.jpg",
        ObjectUtils.asMap("public_id", "product_hero"));
```

## What comes back

An upload response is a `Map` keyed by the raw API field names. The commonly used fields:

| Field | Example | Notes |
|---|---|---|
| `public_id` | `product_hero` | The delivery handle. Appears in URLs. Changes on rename. |
| `asset_id` | `abcdef0123456789abcdef0123456789` | Immutable handle, 32 lowercase hex chars. Survives rename. |
| `secure_url` | `https://res.cloudinary.com/...` | Prefer this over `url`. |
| `url` | `http://res.cloudinary.com/...` | Plain HTTP. Prefer `secure_url`. |
| `version` | `1787824394` | Used in versioned delivery URLs. |
| `width`, `height`, `format`, `bytes` | `864`, `576`, `jpg`, `120401` | |
| `etag` | | Content hash. |
| `resource_type` | `image` | |

Store `asset_id` for a durable reference; see
[search-and-manage-assets.md](search-and-manage-assets.md) for the reasons, and for the calls that
accept it.

## Naming: `public_id`, folders, and overwriting

Without a `public_id`, Cloudinary assigns a random one:

```java
cloudinary.uploader().upload(file, ObjectUtils.emptyMap());
// public_id -> e.g. "kqhrfxrmpqvtmvjbmgkr"
```

With one, you control the delivery URL:

```java
cloudinary.uploader().upload(file, ObjectUtils.asMap(
        "public_id", "products/hero",       // a slash makes it a folder path
        "overwrite", true));
```

### Re-uploading to the same `public_id`

The outcome depends on the `overwrite` option:

- **No `overwrite` key** — the asset is overwritten. The response carries a new `version` and
  describes the newly uploaded bytes. There is no `existing` key.
- **`overwrite` set to `false`** — the call returns normally with the existing asset's metadata
  plus an extra field `existing` set to `true`. Nothing is uploaded and no exception is thrown.

```java
Map result = cloudinary.uploader().upload(file, ObjectUtils.asMap(
        "public_id", "products/hero",
        "overwrite", false));

if (Boolean.TRUE.equals(result.get("existing"))) {
    // Nothing was uploaded; this is the asset that was already there.
}
```

The `existing` field is how the two cases are distinguished; a collision with `overwrite` set to
`false` does not throw.

## Common options

```java
Map result = cloudinary.uploader().upload(file, ObjectUtils.asMap(
        "public_id",   "products/hero",
        "folder",      "catalog",                       // prepended to public_id
        "tags",        "product,hero",                  // comma-separated, or a List
        "context",     ObjectUtils.asMap("alt", "Blue shirt"),
        "eager",       Arrays.asList(new Transformation().width(300).crop("fill")),
        "resource_type", "image"));
```

`resource_type` accepts `image`, `video`, `raw`, or `auto`, and defaults to `image` for `upload()`.
Use `auto` when the file type is not known in advance:

```java
cloudinary.uploader().upload(userFile, ObjectUtils.asMap("resource_type", "auto"));
```

For files over roughly 100 MB, use `uploadLarge` instead; see
[upload-large-video.md](upload-large-video.md).

## Forwarded parameters

`Util.buildUploadParams` serialises a defined set of parameter names into the upload request. A key
that is not in that set is not sent; no exception or warning is raised, and the upload proceeds:

```java
// The unrecognised key is not sent; the upload proceeds without it.
cloudinary.uploader().upload(file, ObjectUtils.asMap(
        "public_id", "x",
        "totally_bogus_param", "ignored"));
```

A misspelled parameter name therefore behaves like an omitted one: `"ovewrite", false` (note the
spelling) is not forwarded, and the default of overwriting the asset applies. Check parameter
names against the Upload API reference.

To confirm whether a parameter is serialised, inspect the map the SDK builds rather than whether
the request succeeded:

```java
Map<String, Object> built = Util.buildUploadParams(ObjectUtils.asMap(
        "public_id", "x",
        "totally_bogus_param", "v"));

built.containsKey("totally_bogus_param");   // false: not forwarded
built.containsKey("public_id");             // true
```

`upload()` sends the map `buildUploadParams` returns, plus `timestamp`, `signature`, and `api_key`,
which the SDK adds when signing. That map covers named fields such as `public_id`, `folder`, and
`upload_preset`; boolean options such as `overwrite`, `unique_filename`, and `invalidate`; `eager`
and `transformation`; and encoded fields such as `tags`, `context`, and `metadata`.

Two options are read by the SDK rather than sent as parameters, so they do not appear in the
built map: `resource_type` selects the endpoint in the request URL, and `return_error` controls
whether a failure is thrown or returned.

## Errors

Upload errors are thrown as `java.lang.RuntimeException`; `getMessage()` carries the server's error
text, and there is no machine-readable error code. Admin API calls, by contrast, throw typed
`ApiException` subclasses.

```java
try {
    cloudinary.uploader().upload(file, ObjectUtils.asMap("public_id", "x"));
} catch (IllegalArgumentException e) {
    // Missing configuration: "Must supply api_key" / "Must supply cloud_name ..."
    System.err.println("Configuration problem: " + e.getMessage());
} catch (RuntimeException e) {
    // Rejected by the server: invalid file, unknown metadata field,
    // unsubscribed add-on, file too large.
    System.err.println("Upload failed: " + e.getMessage());
} catch (IOException e) {
    // Local file unreadable, or a network failure.
    System.err.println("I/O problem: " + e.getMessage());
}
```

Distinguishing causes from the exception requires matching on the message text. Messages observed:

| Message | Cause |
|---|---|
| `Invalid image file` | Not an image, or `resource_type` does not match the file. |
| `Metadata External IDs do not exist: ["..."]` | Undefined structured-metadata field. See [use-structured-metadata.md](use-structured-metadata.md). |
| `You don't have an active subscription for ...` | Add-on not enabled for this product environment. |
| `File size too large` | Exceeds the product-environment limit, not a fixed API ceiling. See [troubleshoot-errors.md](troubleshoot-errors.md). |

### Getting the error as data instead of an exception

With `return_error`, the SDK places the error in the response map instead of throwing:

```java
Map result = cloudinary.uploader().upload(file, ObjectUtils.asMap("return_error", true));
Map error = (Map) result.get("error");
if (error != null) {
    System.err.println(error.get("http_code") + ": " + error.get("message"));
}
```

This is the path that provides the HTTP status code for an upload failure; use it when you need to
branch on the kind of failure rather than log it.

## Related

- [upload-large-video.md](upload-large-video.md) — chunked uploads and the `resource_type` parameter
- [sign-browser-upload.md](sign-browser-upload.md) — uploading straight from a browser
- [moderate-upload.md](moderate-upload.md) — user-generated content
- [Upload API reference](https://cloudinary.com/documentation/image_upload_api_reference.md)
