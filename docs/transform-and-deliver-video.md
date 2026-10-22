# Transform and deliver a video

Video URLs are built with the same `Url` builder as images, with `resourceType("video")` set. The
builder defaults to `image`; without this call the URL is emitted under `/image/upload/` and does
not resolve.

```java
String url = cloudinary.url().resourceType("video").generate("docs_video.mp4");
// https://res.cloudinary.com/my-cloud/video/upload/docs_video.mp4?_a=...
```

`resourceType(String)` sets the resource type. The older spelling `resourcType` remains for
backwards compatibility and behaves identically; use `resourceType`.

## With a transformation

```java
String url = cloudinary.url().resourceType("video")
        .transformation(new Transformation().width(640).crop("scale"))
        .generate("docs_video.mp4");
// .../video/upload/c_scale,w_640/docs_video.mp4
```

Useful video transformation parameters:

| Builder call | URL | Purpose |
|---|---|---|
| `.quality("auto")` | `q_auto` | Automatic quality. Apply this by default. |
| `.videoCodec("auto")` | `vc_auto` | Best codec for the requesting client. |
| `.width(640).crop("scale")` | `c_scale,w_640` | Resize. |
| `.duration("10")` | `du_10` | Limit output length to 10 seconds. |
| `.startOffset("5")` | `so_5` | Start 5 seconds in. |
| `.effect("fade:2000")` | `e_fade:2000` | Fade in over 2 seconds. |
| `.audioCodec("none")` | `ac_none` | Strip audio. |
| `.flags("streaming_attachment")` | `fl_streaming_attachment` | Progressive download. |

## The `<video>` tag

`videoTag` emits a full element with several source formats and a poster image:

```java
String tag = cloudinary.url().resourceType("video").videoTag("docs_video", ObjectUtils.emptyMap());
```

```html
<video poster='https://res.cloudinary.com/my-cloud/video/upload/docs_video.jpg?_a=...'>
  <source src='.../docs_video.webm' type='video/webm'>
  <source src='.../docs_video.mp4'  type='video/mp4'>
  <source src='.../docs_video.ogv'  type='video/ogg'>
</video>
```

Note:

- Pass the public ID without an extension. `videoTag` appends `.webm`, `.mp4`, and `.ogv`
  itself, and derives the `.jpg` poster from the same ID.
- The poster is a generated thumbnail of the first frame.
- Default source formats are WebM, MP4, and OGV.

For attribute values that come from user input, use `generate()` and let your template engine
build the tag; see [transform-and-deliver-image.md](transform-and-deliver-image.md#attributes-from-user-input).

## Thumbnails and animated previews

A still frame as an image:

```java
String thumb = cloudinary.url().resourceType("video").format("jpg")
        .transformation(new Transformation().width(400).crop("fill").startOffset("2"))
        .generate("docs_video");
// .../video/upload/c_fill,so_2,w_400/docs_video.jpg
```

An animated GIF preview:

```java
String preview = cloudinary.url().resourceType("video").format("gif")
        .transformation(new Transformation().width(300).crop("scale")
                .startOffset("0").duration("3"))
        .generate("docs_video");
```

## Adaptive bitrate streaming

For long videos, deliver HLS or DASH rather than a single MP4. Request eager transformations at
upload time using a streaming profile, and read the resulting `playback_url` from the response:

```java
Map result = cloudinary.uploader().uploadLarge(new File("feature.mp4"), ObjectUtils.asMap(
        "resource_type",       "video",
        "public_id",           "clips/feature",
        "eager",               Arrays.asList(
                new EagerTransformation().streamingProfile("full_hd").format("m3u8")),
        "eager_async",         true));
```

Because `eager_async` is set, the derived renditions are not yet available when the upload response
returns. Use `eager_notification_url` to be notified when they are, rather than polling. Streaming
profiles are managed with `api().listStreamingProfiles()` and the related methods.

## Related

- [upload-large-video.md](upload-large-video.md)
- [transform-and-deliver-image.md](transform-and-deliver-image.md)
- [Video transformations](https://cloudinary.com/documentation/video_manipulation_and_delivery.md)
- [Transformation reference](https://cloudinary.com/documentation/transformation_reference.md)
