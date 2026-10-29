# Troubleshoot errors

## Error types by entry point

The exception type depends on which part of the SDK was called:

| Entry point | On failure throws |
|---|---|
| `cloudinary.api()` — Admin API | A typed `ApiException` subclass, chosen by HTTP status |
| `cloudinary.uploader()` — Upload API | `java.lang.RuntimeException`; `getMessage()` carries the server's error text |
| `cloudinary.search()` | A typed `ApiException` subclass (it goes through the Admin path) |
| Missing credentials, any entry point | `java.lang.IllegalArgumentException` |
| Missing HTTP adapter on the classpath | `java.lang.UnknownError`, which extends `Error` |

There is no common exception type across entry points: `catch (ApiException e)` does not catch an
upload failure.

### The `ApiException` hierarchy

All typed exceptions descend from `ApiException`, which extends `Exception`:

```
com.cloudinary.api.exceptions.ApiException      -> java.lang.Exception -> Throwable
com.cloudinary.api.exceptions.BadRequest        -> ApiException
com.cloudinary.api.exceptions.NotFound          -> ApiException
com.cloudinary.api.exceptions.AlreadyExists     -> ApiException
com.cloudinary.api.exceptions.RateLimited       -> ApiException
com.cloudinary.api.exceptions.NotAllowed        -> ApiException
com.cloudinary.api.exceptions.GeneralError      -> ApiException
com.cloudinary.api.AuthorizationRequired        -> ApiException
```

- `catch (ApiException e)` catches every Admin API error, including `AuthorizationRequired`.
- `AuthorizationRequired` is in `com.cloudinary.api`; every other typed exception is in the
  `com.cloudinary.api.exceptions` subpackage. An import of
  `com.cloudinary.api.exceptions.AuthorizationRequired` does not compile.

Each class corresponds to an HTTP status code:

| Exception | HTTP |
|---|---|
| `BadRequest` | 400 |
| `AuthorizationRequired` | 401 |
| `NotAllowed` | 403 |
| `NotFound` | 404 |
| `AlreadyExists` | 409 |
| `RateLimited` | 420 |
| `GeneralError` | 500, and every unmapped status |

The mapping covers these seven codes (`Api.CLOUDINARY_API_ERROR_CLASSES`). Any other status,
including HTTP 429, is thrown as `GeneralError` with a message of the form
`Server returned unexpected status code - <code> - <body>`. To handle a 429, catch `GeneralError`
or `ApiException` and inspect the message.

`ApiException` carries a message and has no separate error-code accessor. For Admin API calls the
class name identifies the error category; for upload calls the message is the information the
exception carries. To receive the HTTP status as data instead, pass `return_error` (see below).

## Catching errors

The catch list depends on the entry point.

**For an upload**, `Uploader.upload` does not declare `ApiException`, which is a checked exception,
so a `catch (ApiException e)` clause on an upload call does not compile:

```
error: exception ApiException is never thrown in body of corresponding try statement
```

An upload block catches `IllegalArgumentException`, `IOException`, and `RuntimeException`:

```java
import java.io.IOException;

try {
    cloudinary.uploader().upload(file, ObjectUtils.asMap("public_id", "example"));
} catch (IllegalArgumentException e) {
    // Configuration is absent or unparseable.
    System.err.println("Configuration error: " + e.getMessage());
    System.exit(1);
} catch (IOException e) {
    // Local file unreadable, or transport failure.
    System.err.println("I/O error: " + e.getMessage());
    System.exit(1);
} catch (RuntimeException e) {
    // Rejected by the server; getMessage() carries the server's error text.
    System.err.println("Upload failed: " + e.getMessage());
    System.exit(1);
}
```

`IllegalArgumentException` is a subclass of `RuntimeException`, so it must come first; with the
order reversed the second branch is unreachable and the block does not compile.

**For an Admin API or Search call**, catch `ApiException`. `Api` methods declare `throws Exception`,
so a broader catch is also required:

```java
import com.cloudinary.api.exceptions.ApiException;

try {
    cloudinary.api().resource("example", ObjectUtils.emptyMap());
} catch (ApiException e) {
    // Branch on the class, not the message: NotFound, BadRequest, RateLimited, ...
    System.err.println("API error (" + e.getClass().getSimpleName() + "): " + e.getMessage());
} catch (Exception e) {
    System.err.println("Call failed: " + e.getMessage());
}
```

Besides `ApiException` subclasses, an Admin API call throws `RuntimeException: Invalid JSON response
from server ...` when the response body is not JSON, such as an HTML 502 page from a proxy, and
`IllegalArgumentException: Must supply api_key` when credentials are missing. The `Exception`
branch catches both. Catch `ApiException` first, then `Exception`.

Neither block catches `UnknownError`; see the next section.

## `UnknownError: Can't find Cloudinary platform adapter`

```
java.lang.UnknownError: Can't find Cloudinary platform adapter
    [com.cloudinary.android.UploaderStrategy,com.cloudinary.http5.UploaderStrategy]
```

**Cause:** `cloudinary-core` is on the classpath and no HTTP adapter is. `cloudinary-core` has no
HTTP client of its own.

**Fix:** add `cloudinary-http5` to the classpath. This is a build-configuration error and is not
meant to be caught; it is a `java.lang.Error`, so `catch (Exception e)` does not catch it. It is
thrown from the `Cloudinary` constructor, before any API call.

See [import-and-call.md](import-and-call.md).

## `IllegalArgumentException: Must supply cloud_name` / `Must supply api_key`

**Cause:** no credentials reached the SDK. The `Cloudinary` constructor does not validate
configuration, so this exception appears on the first call, not at construction.

Check in this order:

```java
System.out.println(cloudinary.config.cloudName);   // null -> nothing was parsed
System.out.println(cloudinary.config.apiKey != null);   // false -> no API key
```

1. Is `CLOUDINARY_URL` set in the environment the JVM sees? A system property of the same name
   takes precedence over the environment variable.
2. Does the cloud name contain an underscore? If so, the URL was not parsed; see the next section.
3. URL building needs only `cloud_name`; upload and Admin API calls also need `api_key` and
   `api_secret`.

### Cloud names containing an underscore

`Configuration.from()` parses the connection URL with `java.net.URI` and reads the cloud name from
`getHost()`. Underscores are not permitted in DNS host names, so for such a URL `getHost()` returns
`null`, and so does `getUserInfo()`, which supplies the key and secret. No exception is thrown at
parse time.

```java
Configuration.from("cloudinary://key:secret@my-cloud").cloudName;   // "my-cloud"  (fine)
Configuration.from("cloudinary://key:secret@my_cloud").cloudName;   // null
Configuration.from("cloudinary://key:secret@my_cloud").apiKey;      // null
```

Configure such environments with a `Map` or a `Configuration` object rather than a connection URL.
See [configure-cloudinary.md](configure-cloudinary.md).

## `RuntimeException: Invalid image file`

**Cause:** in most cases, `resource_type` does not match the file. `upload()` and `uploadLarge()`
both default to `image`, so uploading a video without `resource_type` produces this message.

```java
// Throws RuntimeException: Invalid image file
cloudinary.uploader().uploadLarge(new File("clip.mp4"), ObjectUtils.emptyMap());

// Correct
cloudinary.uploader().uploadLarge(new File("clip.mp4"),
        ObjectUtils.asMap("resource_type", "video"));
```

Use `"resource_type", "auto"` when the file type is not known ahead of time. See
[upload-large-video.md](upload-large-video.md).

## `RuntimeException: You don't have an active subscription for ...`

**Cause:** the requested add-on is not enabled for this product environment. Observed message:
`You don't have an active subscription for Cloudinary AI Content Analysis`.

This arrives as a `RuntimeException` from the upload path, not as `AuthorizationRequired` or a
rate-limit error. Enable the add-on in the Cloudinary console under Add-ons, then retry.

## `RuntimeException: Metadata External IDs do not exist: [...]`

**Cause:** the upload passed a structured-metadata key that is not defined in the product
environment. Undefined keys are rejected rather than ignored, and the whole upload fails. Define
the field first. See [use-structured-metadata.md](use-structured-metadata.md).

## `BadRequest: Query Error (at position N)`

**Cause:** an invalid search expression. Leading wildcards are rejected, including `*` on its own:

```
search("*sample")           -> BadRequest: Query Error (at position 1) ' ➥➥➥*sample'
search("*")                 -> BadRequest: Query Error (at position 1) ' ➥➥➥*'
search("public_id:*sample") -> BadRequest: Query Error (at position 11)
search("folder:*")          -> BadRequest: Query Error (at position 8)
```

The `➥➥➥` marker points at the offending character. To match everything, pass an empty
expression rather than `*`. See [search-and-manage-assets.md](search-and-manage-assets.md).

## `RateLimited`, and `423 Processing`

`RateLimited` maps from HTTP 420 only; a 429 arrives as `GeneralError`, as described above.

Read the current limits from the response. `ApiResponse` exposes one accessor for this,
`rateLimits()`, returning `Map<String, RateLimit>` and declaring `ParseException`:

```java
import com.cloudinary.api.ApiResponse;
import com.cloudinary.api.RateLimit;
import java.text.ParseException;

ApiResponse response = cloudinary.api().ping(ObjectUtils.emptyMap());
try {
    for (Map.Entry<String, RateLimit> entry : response.rateLimits().entrySet()) {
        RateLimit limit = entry.getValue();
        System.out.println(entry.getKey()
                + " limit="     + limit.getLimit()
                + " remaining=" + limit.getRemaining()
                + " reset="     + limit.getReset());     // java.util.Date
    }
} catch (ParseException e) {
    // Headers absent or unparseable.
}
```

`RateLimit` has `getLimit()`, `getRemaining()`, and `getReset()`. `ApiResponse` has no
`rateLimitAllowed()` or `rateLimitRemaining()` methods.

**HTTP 423 Processing** indicates that the asset exists but is still being processed, typically a
large video shortly after upload. Retry with backoff rather than treating it as a failure. If
requests fail in ways not covered here, check
[status.cloudinary.com](https://status.cloudinary.com) before debugging further.

## "File size too large"

In almost all cases this message refers to the product-environment limit, not a fixed API ceiling.
Read the current values from the usage report:

```java
Map usage = cloudinary.api().usage(ObjectUtils.emptyMap());
System.out.println(usage.get("media_limits"));
// e.g. {video_max_size_bytes=21474836480, image_max_size_bytes=21474836480,
//       image_max_px=50000000, raw_max_size_bytes=21474836480,
//       asset_max_total_px=300000000}
```

`image_max_px` and `asset_max_total_px` are pixel limits, not byte limits; a file of modest byte
size can exceed them.

Chunking does not raise the environment maximum. `uploadLarge` splits a file into chunks to stay
within per-request limits; it cannot upload a file larger than the environment allows. At the
limit, reduce the source or contact Cloudinary about a plan that permits more.

## Getting an upload error as data instead of an exception

Upload exceptions carry no status code. To branch on the kind of failure, pass `return_error`:

```java
Map result = cloudinary.uploader().upload(file,
        ObjectUtils.asMap("public_id", "x", "return_error", true));

Map error = (Map) result.get("error");
if (error != null) {
    System.err.println(error.get("http_code") + ": " + error.get("message"));
    // e.g. 400: Metadata External IDs do not exist: ["nope"]
}
```

With `return_error`, on failure the response map contains an `error` key and no asset fields, so
check for `error` before reading anything else.

## Inspecting the request the SDK builds

For upload parameter problems, print the serialised parameters. `Util.buildUploadParams` forwards
a defined set of parameters and unrecognised keys are not sent, so this is the fastest way to find
a misspelled name:

```java
System.out.println(Util.buildUploadParams(options));
```

For URL problems, print the generated URL. URL building is local and needs no credentials beyond
a cloud name:

```java
System.out.println(cloudinary.url().generate("sample.jpg"));
```

## Related

- [configure-cloudinary.md](configure-cloudinary.md)
- [import-and-call.md](import-and-call.md)
- [platform-capabilities.md](platform-capabilities.md)
