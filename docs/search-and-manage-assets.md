# Search and manage assets

## Two identifiers, and which calls accept which

| Identifier | Example | Property |
|---|---|---|
| `public_id` | `products/hero` | Appears in delivery URLs. Changes when you rename. |
| `asset_id` | `abcdef0123456789abcdef0123456789` | Immutable, 32 lowercase hex chars. Survives renames. |

Store `asset_id` as your durable reference. Store `public_id` only if you need to reconstruct a
URL, and re-read it when you need it.

Six `Api` methods accept an `asset_id`:

```
Api.resourceByAssetID(String, Map)
Api.resourcesByAssetIDs(Iterable, Map)
Api.resourcesByAssetFolder(String, Map)
Api.deleteResourcesByAssetIds(Iterable, Map)
Api.restoreByAssetIds(Iterable, Map)
Api.deleteBackedUpAssets(String, String[], Map)
```

There is no asset-ID variant of `api().update()`, of any `Uploader` method (`destroy`, `rename`,
`explicit`, `addTag`, …), or of URL building. To use those with an asset ID, look the asset up by
its asset ID, read the `public_id` from the response, then pass that to the call that requires it.
This costs one extra round trip.

```java
// 1. Resolve the immutable handle to a current public_id.
Map asset = cloudinary.api().resourceByAssetID(
        "abcdef0123456789abcdef0123456789", ObjectUtils.emptyMap());
String publicId = (String) asset.get("public_id");

// 2. Use the public_id for calls that only accept it.
cloudinary.api().update(publicId, ObjectUtils.asMap("tags", "reviewed"));
```

## Search

```java
Map result = cloudinary.search()
        .expression("resource_type:image AND tags=hero")
        .sortBy("created_at", "desc")
        .maxResults(30)
        .execute();

System.out.println(result.get("total_count"));
for (Map asset : (List<Map>) result.get("resources")) {
    System.out.println(asset.get("public_id") + " " + asset.get("asset_id"));
}
```

`Search` is a chainable builder; `execute()` performs the request. Search results include
`asset_id`, `public_id`, `format`, `bytes`, `created_at`, `secure_url`, and — when relevant —
`moderation_status` and `moderation_kind` as flat fields.

### Expression syntax notes

Leading wildcards are rejected, including `*` on its own. Each of these is a `BadRequest`:

```
"*sample"            -> Query Error (at position 1) ' ➥➥➥*sample'
"*"                  -> Query Error (at position 1) ' ➥➥➥*'
"public_id:*sample"  -> Query Error (at position 11)
"folder:*"           -> Query Error (at position 8)
```

To match everything, pass an empty expression:

```java
cloudinary.search().expression("").maxResults(500).execute();
```

Trailing wildcards are accepted: `public_id:products/*`.

A valid expression can return zero results while matching assets exist. On a dynamic-folder product
environment, `folder:` may match nothing even though assets exist, because the field has a different
meaning there. To tell an empty environment from a non-matching expression, compare against the
unfiltered total:

```java
long all = ((Number) cloudinary.search().expression("").execute().get("total_count")).longValue();
```

If `all` is non-zero while the folder query returns zero, the expression does not match the data.
Use `asset_folder:` on dynamic-folder environments. The full syntax is in the
[search expression reference](https://cloudinary.com/documentation/search_api.md#expressions).

### Pagination

```java
String cursor = null;
do {
    Search search = cloudinary.search().expression("resource_type:image").maxResults(500);
    if (cursor != null) {
        search = search.nextCursor(cursor);
    }
    Map page = search.execute();
    for (Map asset : (List<Map>) page.get("resources")) {
        // ...
    }
    cursor = (String) page.get("next_cursor");
} while (cursor != null);
```

Search is rate-limited more tightly than other Admin API calls. For bulk enumeration prefer
`api().resources(...)` with a cursor, and cache rather than re-querying.

## Listing without search

```java
Map result = cloudinary.api().resources(ObjectUtils.asMap(
        "type", "upload", "prefix", "products/", "max_results", 100));
```

`prefix` filters on `public_id`, which makes this the cheapest way to enumerate a folder-like
namespace.

Fetch details for one asset:

```java
Map asset = cloudinary.api().resource("products/hero", ObjectUtils.asMap(
        "colors", true, "faces", true, "image_metadata", true));
```

Those extra flags cost more to compute — omit them unless you need them.

## Rename, tag, delete

```java
// Rename. The public_id changes; the asset_id does not.
cloudinary.uploader().rename("products/hero", "products/hero_v2", ObjectUtils.emptyMap());

// Add and remove tags.
cloudinary.uploader().addTag("featured", new String[]{"products/hero_v2"}, ObjectUtils.emptyMap());
cloudinary.uploader().removeTag("featured", new String[]{"products/hero_v2"}, ObjectUtils.emptyMap());

// Delete one asset.
cloudinary.uploader().destroy("products/hero_v2", ObjectUtils.emptyMap());

// Delete several by public_id, or by asset_id.
cloudinary.api().deleteResources(Arrays.asList("a", "b"), ObjectUtils.emptyMap());
cloudinary.api().deleteResourcesByAssetIds(
        Arrays.asList("abcdef0123456789abcdef0123456789"), ObjectUtils.emptyMap());
```

`rename` is rejected if the destination exists unless you pass `"overwrite", true`.

By default, deleting an asset leaves CDN copies in place. Pass `"invalidate", true` to `destroy`
when the content must stop being served.

`resource_type` and `type` default to `image` and `upload` on these calls. Deleting a video or a
raw file requires passing `resource_type`:

```java
cloudinary.uploader().destroy("clips/intro",
        ObjectUtils.asMap("resource_type", "video", "invalidate", true));
```

Without it, a `destroy` for a video's public ID looks for an *image* with that ID, finds none, and
reports `not found` while the video remains.

## Folders

```java
Map created = cloudinary.api().createFolder("catalog/2026", ObjectUtils.emptyMap());
// -> keys: [external_id, name, path, success]

Map root = cloudinary.api().rootFolders(ObjectUtils.emptyMap());
Map subs = cloudinary.api().subFolders("catalog", ObjectUtils.emptyMap());
cloudinary.api().deleteFolder("catalog/2026", ObjectUtils.emptyMap());
```

`createFolder` returns `{name, path, external_id, success}`. The other folder calls return
different shapes, so read the specific field you need and null-check it.

`renameFolder` is disabled on many product environments, including a default new one. The refusal
is reported as an authorization error:

```
com.cloudinary.api.AuthorizationRequired: Folder Renaming is not allowed in this cloud
```

This error reflects the environment setting rather than the credentials: it is returned even when
the same key succeeds on every other Admin API call.

Deleting a folder requires it to be empty.

## Related

- [upload-image.md](upload-image.md)
- [use-structured-metadata.md](use-structured-metadata.md)
- [Admin API reference](https://cloudinary.com/documentation/admin_api.md)
- [Search API reference](https://cloudinary.com/documentation/search_api.md)
