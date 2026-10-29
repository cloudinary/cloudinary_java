# Transform and deliver an image

URL building is entirely local. It needs a cloud name but no API key, makes no network request, and
does not depend on credentials.

## The basic URL

```java
String url = cloudinary.url().generate("docs_sample.jpg");
// https://res.cloudinary.com/my-cloud/image/upload/docs_sample.jpg?_a=DAGCg+ALZAA0
```

Two properties of that output:

- It is `https`. `secure` defaults to `true` when configuration comes from a `Map` or
  `CLOUDINARY_URL`; with `new Configuration()` or `Configuration.Builder`, set it explicitly.
- The `?_a=` parameter is SDK analytics. See
  [configure-cloudinary.md](configure-cloudinary.md) to disable it.

## Applying a transformation

```java
String url = cloudinary.url()
        .transformation(new Transformation().width(100).height(150).crop("fill"))
        .generate("docs_sample.jpg");
// .../image/upload/c_fill,h_150,w_100/docs_sample.jpg
```

`Transformation` is a chainable builder. Component order in the emitted URL is normalised by the
SDK, so `width(100).height(150)` and `height(150).width(100)` produce the same string.

### Automatic format and quality

```java
String url = cloudinary.url()
        .transformation(new Transformation().fetchFormat("auto").quality("auto"))
        .generate("docs_sample.jpg");
// .../image/upload/f_auto,q_auto/docs_sample.jpg
```

`f_auto` selects the best format the requesting browser supports (AVIF, WebP, or the original);
`q_auto` selects a quality level by analysing the image. Together they typically reduce bytes
substantially with no visible change, and they are the highest-value transformation for most
applications.

### Chained transformations

Each `chain()` starts a new component, applied in sequence:

```java
String url = cloudinary.url()
        .transformation(new Transformation()
                .width(400).height(400).crop("fill").gravity("auto")
                .chain()
                .effect("sharpen")
                .chain()
                .fetchFormat("auto").quality("auto"))
        .generate("docs_sample.jpg");
// .../c_fill,g_auto,h_400,w_400/e_sharpen/f_auto,q_auto/docs_sample.jpg
```

`gravity("auto")` lets Cloudinary choose the crop focus by content analysis, which suits
user-supplied images where a centre crop would cut off the subject.

## Crop modes

| Mode | Behaviour |
|---|---|
| `fill` | Fills the requested box exactly, cropping the overflow. Needs `gravity` to choose what to keep. |
| `fit` | Scales down to fit inside the box; no cropping, aspect ratio preserved. |
| `scale` | Forces exact dimensions, distorting if the aspect ratio differs. |
| `limit` | Like `fit`, but never scales *up*. |
| `thumb` | Crops to the box around a detected subject; pair with `gravity("face")` or `gravity("auto")`. |
| `pad` | Fits inside the box and pads the remainder; combine with `background`. |

The full list is in the
[transformation reference](https://cloudinary.com/documentation/transformation_reference.md).

## HTML tags

`imageTag` emits a complete `<img>`:

```java
String tag = cloudinary.url().imageTag("docs_sample.jpg");
// <img src='https://res.cloudinary.com/my-cloud/image/upload/docs_sample.jpg?_a=...'/>
```

With a transformation and extra attributes:

```java
String tag = cloudinary.url()
        .transformation(new Transformation().width(300).crop("fill"))
        .imageTag("docs_sample.jpg", ObjectUtils.asMap("alt", "Sample image", "class", "hero"));
```

### Attributes from user input

`imageTag` is for attribute values written by the developer, such as a fixed `class` or `alt`
text. When an attribute value comes from user input, generate the URL with `generate()` and let
your template engine build the tag and escape the value:

```java
String src = cloudinary.url()
        .transformation(new Transformation().width(300).crop("fill"))
        .generate("docs_sample.jpg");
// Thymeleaf: <img th:src="${src}" th:alt="${userText}"/>
// JSP:       <img src="<c:out value='${src}'/>" alt="<c:out value='${userText}'/>"/>
```

The same applies to `videoTag` and to the JSP tag library.

### Building a `srcset`

`imageTag` attributes are `String` values, concatenated into the tag. Passing a `Map` as the
`srcset` value throws:

```
java.lang.ClassCastException: class java.util.HashMap cannot be cast to class java.lang.String
    at com.cloudinary.Url.imageTag(Url.java:603)
```

(`ResponsiveBreakpoint` is an *upload* option that generates derived sizes; it does not produce a
`srcset` attribute.)

Generate each URL with `generate()` and assemble the attribute:

```java
StringBuilder srcset = new StringBuilder();
for (int width : new int[]{400, 800, 1200}) {
    if (srcset.length() > 0) {
        srcset.append(", ");
    }
    srcset.append(cloudinary.url()
                    .transformation(new Transformation().width(width).crop("scale")
                            .fetchFormat("auto").quality("auto"))
                    .generate("docs_sample.jpg"))
          .append(" ").append(width).append("w");
}
```

Then let your template engine emit the attribute.
See the [responsive images guide](https://cloudinary.com/documentation/responsive_images.md).

## Versions in URLs

If the public ID contains a slash and no version is set, the SDK inserts a `v1` placeholder:

```java
cloudinary.url().generate("folder/docs_sample.jpg");
// .../image/upload/v1/folder/docs_sample.jpg
```

The placeholder keeps the URL unambiguous when a public ID looks like a path. To emit the actual
version, pass the `version` from the upload response:

```java
cloudinary.url().version("1787824394").generate("folder/docs_sample.jpg");
// .../image/upload/v1787824394/folder/docs_sample.jpg
```

Including the real version means a re-upload to the same public ID produces a different URL, which
gives cache-safe updates. Set `force_version` to `false` in configuration to omit the placeholder.

## Delivering a remote image without uploading it

```java
String url = cloudinary.url().type("fetch")
        .transformation(new Transformation().width(300).crop("fill"))
        .generate("https://upload.wikimedia.org/wikipedia/commons/3/3f/JPEG_example_flower.jpg");
```

`type("fetch")` proxies, transforms, and caches a remote image on delivery. Fetch must be permitted
for your product environment.

## A named transformation

Define it once in the console or via the Admin API, then reference it:

```java
cloudinary.api().createTransformation("thumb_square",
        new Transformation().width(200).height(200).crop("fill").gravity("auto").generate(),
        ObjectUtils.emptyMap());

String url = cloudinary.url()
        .transformation(new Transformation().named("thumb_square"))
        .generate("docs_sample.jpg");
// .../image/upload/t_thumb_square/docs_sample.jpg
```

This keeps presentation decisions out of your application code.

## Signed URLs and access control

For assets that should not be publicly guessable, upload with `type` set to `authenticated` and
sign the delivery URL:

```java
String url = cloudinary.url().signed(true).type("authenticated")
        .transformation(new Transformation().width(300).crop("fill"))
        .generate("private_asset.jpg");
```

Signing requires the API secret, so it happens server-side. `Url` is a mutable builder and is not
thread-safe; call `cloudinary.url()` for each URL rather than sharing one instance.

## Related

- [transform-and-deliver-video.md](transform-and-deliver-video.md)
- [upload-image.md](upload-image.md)
- [Image transformations](https://cloudinary.com/documentation/image_transformations.md)
- [Transformation reference](https://cloudinary.com/documentation/transformation_reference.md)
