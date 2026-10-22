# Add the dependency and make your first call

## Add the dependency

The SDK is split across two artifacts. `cloudinary-core` contains the URL builder, the
transformation builder, and the parameter serialisation; it has no HTTP client, so on its own it
cannot upload or call the Admin API. The HTTP transport lives in a separate artifact that
`cloudinary-core` locates at runtime by class name.

For server-side Java, depend on `cloudinary-http5`. It pulls in `cloudinary-core` transitively, so
declaring both is unnecessary.

Maven:

```xml
<dependency>
    <groupId>com.cloudinary</groupId>
    <artifactId>cloudinary-http5</artifactId>
    <version>X.Y.Z</version>
</dependency>
```

Gradle:

```groovy
implementation 'com.cloudinary:cloudinary-http5:X.Y.Z'
```

Replace `X.Y.Z` with the current release. These pages carry no version literal because they are
not tied to a release; resolve it from Maven Central:

```bash
curl -s https://repo1.maven.org/maven2/com/cloudinary/cloudinary-http5/maven-metadata.xml \
    | grep -o '<latest>[^<]*</latest>'
```

The current version is also shown on
[Maven Central](https://central.sonatype.com/artifact/com.cloudinary/cloudinary-http5).

### Published artifacts

| Artifact | Status |
|---|---|
| `cloudinary-http5` | Current. Apache HttpClient 5. Use this. |
| `cloudinary-core` | Current. Not usable alone; it has no HTTP client. |
| `cloudinary-taglib` | Current. JSP tags only; add alongside `cloudinary-http5`. See [use-with-jsp-taglib.md](use-with-jsp-taglib.md). |
| `cloudinary-http45`, `-http44`, `-http43`, `-http42` | Legacy, 1.x line. Last published 1.39.0; no 2.x version of these artifacts exists. |
| `cloudinary-test-common` | Internal test helper. Not for application code. |

A dependency on `cloudinary-http45` with a 2.x version number does not resolve, because that
combination has never been published.

## Missing HTTP adapter

If `cloudinary-core` is on the classpath without an HTTP adapter, the `Cloudinary` constructor
throws:

```
java.lang.UnknownError: Can't find Cloudinary platform adapter
    [com.cloudinary.android.UploaderStrategy,com.cloudinary.http5.UploaderStrategy]
```

- It is thrown from the `Cloudinary` constructor, before any API call.
- `java.lang.UnknownError` extends `Error`, not `Exception`. A `catch (Exception e)` block does
  not catch it; `catch (Throwable t)` or `catch (Error e)` does.

The fix is to add `cloudinary-http5` to the classpath rather than to catch the error.

## Your first call

```java
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import java.util.Map;

public class FirstCall {
    public static void main(String[] args) throws Exception {
        // Reads the CLOUDINARY_URL environment variable.
        Cloudinary cloudinary = new Cloudinary();

        Map result = cloudinary.uploader().upload(
                "https://res.cloudinary.com/demo/image/upload/sample.jpg",
                ObjectUtils.asMap("public_id", "first_upload"));

        System.out.println(result.get("secure_url"));
        System.out.println(cloudinary.url().generate("first_upload.jpg"));
    }
}
```

See [configure-cloudinary.md](configure-cloudinary.md) for the ways to supply credentials, and
[get-credentials.md](get-credentials.md) if you do not have any yet.

## The shape of the API

All entry points are methods on a `Cloudinary` instance:

| Call | Returns | Use for |
|---|---|---|
| `cloudinary.uploader()` | `Uploader` | Uploading, destroying, renaming, tagging, archives |
| `cloudinary.api()` | `Api` | Admin API: listing, resource details, folders, metadata fields, usage |
| `cloudinary.url()` | `Url` | Building delivery URLs and HTML tags |
| `cloudinary.search()` | `Search` | Search API over assets |
| `cloudinary.searchFolders()` | `SearchFolders` | Search API over folders |

`Uploader` and `Api` methods almost all take a trailing `Map` of options. Use
`ObjectUtils.asMap(k1, v1, k2, v2, ...)` to build one, or `ObjectUtils.emptyMap()` when there are
no options to pass.

Responses are `Map` (Admin API calls return `ApiResponse`, which implements `Map`). There are no
typed response objects, so fields are read by string key and cast. Field names are the raw API
field names: `public_id`, `secure_url`, `asset_id`, and so on.

`Url` is a mutable builder and is not safe to share across threads. `Cloudinary` is cheap to
construct and safe to hold as a singleton; call `cloudinary.url()` for each URL you build.

## Java version

The 2.x line targets Java 8 and above. The 1.x line supported Java 6.
