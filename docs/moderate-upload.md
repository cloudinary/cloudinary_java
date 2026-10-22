# Moderate uploaded content

## Requesting moderation

```java
Map result = cloudinary.uploader().upload(file, ObjectUtils.asMap(
        "public_id",  "ugc/submission_1",
        "moderation", "manual"));
```

Values are the moderation kind: `manual` for human review, or an add-on identifier such as
`aws_rek` / `google_video_moderation` / `perception_point` for automatic moderation. Add-on kinds
require an active subscription; without one the upload is rejected — see
[troubleshoot-errors.md](troubleshoot-errors.md).

## Moderation and delivery

By default, moderation status does not gate delivery. A `pending` moderated asset is still
delivered: its delivery URL returns HTTP 200 with the full image body. Uploading with
`moderation=manual` and then fetching the returned `secure_url` gives:

```
HTTP status for pending asset = 200   content-type=image/jpeg   len=109669
```

Moderation status is metadata that your own application code gates on.

Blocking delivery of non-approved assets is configurable per product environment by Cloudinary
support. It is not an upload parameter and cannot be enabled from this SDK. For a hard gate, either
request that configuration or withhold the URL until you have checked the status yourself, as
described under "Gating delivery in your application" below.

## Where the moderation status appears

The status is returned in different fields depending on the call:

| Call | `moderation` | `moderation_status` |
|---|---|---|
| `uploader().upload(...)` | `[{kind=manual, status=pending}]` | absent (`null`) |
| `api().resource(id, ...)` | `[{kind=manual, status=pending}]` | `pending` |
| `api().update(id, ...)` | `[{updated_at=..., kind=manual, status=approved}]` | `approved` |
| `search().execute()` resources | not present | `pending` (flat, plus `moderation_kind`) |

The upload response has no flat `moderation_status` key; `result.get("moderation_status")` returns
`null` there. Read the status from the `moderation` list:

```java
Map result = cloudinary.uploader().upload(file, ObjectUtils.asMap(
        "public_id", "ugc/submission_1", "moderation", "manual"));

List<Map> moderation = (List<Map>) result.get("moderation");
String status = moderation == null || moderation.isEmpty()
        ? null                                   // asset was not submitted for moderation
        : (String) moderation.get(0).get("status");
// status -> "pending"
```

A helper that reads every response shape:

```java
static String moderationStatus(Map response) {
    Object flat = response.get("moderation_status");
    if (flat instanceof String) {
        return (String) flat;                    // resource(), update(), search() results
    }
    Object list = response.get("moderation");
    if (list instanceof List && !((List) list).isEmpty()) {
        return (String) ((Map) ((List) list).get(0)).get("status");   // upload()
    }
    return null;                                 // not moderated
}
```

When several moderation kinds are applied to one asset, `moderation` holds one entry per kind, each
with its own status. In that case check every entry, and decide whether your gate requires *all*
kinds to be approved or *any* one of them.

## Statuses

`pending`, `approved`, `rejected`. An asset that was not submitted for moderation has neither field
populated; this is distinct from `pending`.

## Approving and rejecting

```java
Map updated = cloudinary.api().update("ugc/submission_1",
        ObjectUtils.asMap("moderation_status", "approved"));

// -> moderation = [{updated_at=2026-08-27T09:54:34Z, kind=manual, status=approved}]
// -> moderation_status = approved
```

`api().update()` takes a `public_id`, not an `asset_id`; there is no asset-ID variant of `update`.
See [search-and-manage-assets.md](search-and-manage-assets.md).

## Listing what is waiting for review

```java
Map pending = cloudinary.api().resourcesByModeration(
        "manual", "pending", ObjectUtils.asMap("max_results", 100));

for (Map asset : (List<Map>) pending.get("resources")) {
    System.out.println(asset.get("public_id") + " " + asset.get("moderation_status"));
}
```

Paginate with `next_cursor` when there are more results than `max_results`.

## Gating delivery in your application

Because delivery is not gated by default, gate it in your own application:

1. Upload with `moderation`, and store the returned `asset_id` (the `public_id` changes on rename).
2. Record the asset as unpublished in your own datastore and withhold its Cloudinary URL.
3. On approval — via your review UI or a moderation webhook — update your own flag, then start
   serving the URL.

The Cloudinary URL of an unapproved asset returns 200, not 404, so it cannot serve as the gate.

If you use webhooks, verify the callback before acting on it:

```java
boolean valid = cloudinary.verifyNotificationSignature(
        requestBody,                                  // raw body string
        request.getHeader("X-Cld-Timestamp"),
        request.getHeader("X-Cld-Signature"),
        7200L);                                       // validity window, seconds
```

## Related

- [upload-image.md](upload-image.md)
- [use-structured-metadata.md](use-structured-metadata.md) — for review notes on an asset
- [Moderation documentation](https://cloudinary.com/documentation/moderate_assets.md)
