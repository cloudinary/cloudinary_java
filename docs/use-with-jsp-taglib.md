# Use the JSP tag library

`cloudinary-taglib` provides JSP tags for building delivery URLs and upload fields in server-rendered
views. It is a separate artifact from the SDK core, relevant when your views are JSP.

## Dependencies

You need the taglib and an HTTP adapter. The taglib depends on `cloudinary-core`, which has no HTTP
transport of its own:

```xml
<dependency>
    <groupId>com.cloudinary</groupId>
    <artifactId>cloudinary-taglib</artifactId>
    <version>X.Y.Z</version>
</dependency>
<dependency>
    <groupId>com.cloudinary</groupId>
    <artifactId>cloudinary-http5</artifactId>
    <version>X.Y.Z</version>
</dependency>
```

`cloudinary-taglib` declares `javax.servlet:jsp-api` as a compile dependency. On a container that
provides its own JSP API, exclude or override it. The taglib targets the `javax.*` namespace; on a
Jakarta EE 9+ container, which uses `jakarta.*`, it requires modification.

## Configuration: the tags use a static singleton

The tags take no credentials as attributes. They call `com.cloudinary.Singleton.getCloudinary()`,
and the application registers the instance:

```java
Singleton.registerCloudinary(cloudinary);
```

If nothing registers one, `Singleton` falls back to `new Cloudinary()`, the no-argument constructor,
which reads the `CLOUDINARY_URL` system property or environment variable. With that variable set,
the tags work without further wiring; without it, they throw
`IllegalArgumentException: Must supply cloud_name in tag or in configuration`.

Register explicitly when your configuration comes from somewhere else. With Spring, do it where the
`Cloudinary` bean is created:

```java
import com.cloudinary.Cloudinary;
import com.cloudinary.Singleton;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Configuration
public class CloudinaryConfig {

    private final Cloudinary cloudinary = new Cloudinary();   // reads CLOUDINARY_URL

    @Bean
    public Cloudinary cloudinary() {
        return cloudinary;
    }

    @PostConstruct
    public void register() {
        Singleton.registerCloudinary(cloudinary);
    }

    @PreDestroy
    public void deregister() {
        Singleton.deregisterCloudinary();
    }
}
```

Call `deregisterCloudinary()` on shutdown. The singleton is a static field, so in a container that
redeploys applications without restarting the JVM, an instance left registered outlives its
deployment, and the redeployed application would read the previous deployment's configuration.

`SingletonManager` wraps the same two calls in a bean: `setCloudinary(...)` supplies the instance,
`init()` registers it, and `destroy()` deregisters it. Nothing calls `init()` or `destroy()` for
you; declare them as the bean's init and destroy methods.

## Declaring the taglib

```jsp
<%@ taglib prefix="cl" uri="http://cloudinary.com/jsp/taglib" %>
```

The URI must be exactly that string.

## The tags

| Tag | Purpose |
|---|---|
| `<cl:image>` | Emit an `<img>` for a Cloudinary asset |
| `<cl:video>` | Emit a `<video>` element |
| `<cl:url>` | Emit just the URL string |
| `<cl:transformation>` | Emit a transformation string such as `c_fill,w_300` |
| `<cl:upload>` | Signed upload form field |
| `<cl:unsignedUpload>` | Unsigned upload form field, needs a preset |
| `<cl:jsconfig>` | Emit JS configuration for Cloudinary's browser libraries |
| `<cl:jsinclude>` | Emit `<script>` tags for those libraries |

### Attributes: `src` and dynamic transformation attributes

- The asset is named by `src`. The TLD also declares a deprecated `publicId` attribute on
  `<cl:image>` and `<cl:video>`, but the tag classes have no setter for it, so a page that uses it
  fails with `JasperException: Unable to find setter method for attribute: [publicId]`.
- Transformation parameters are *dynamic* attributes on `<cl:image>`, `<cl:video>`, and
  `<cl:url>`: they are not declared in the TLD and are passed to the transformation as given.
  Use the SDK's snake_case parameter names (`width`, `crop`, `fetch_format`). An unrecognised
  name, such as `fetchFormat`, is dropped without an error.
- The body of `<cl:image>`, `<cl:video>`, and `<cl:url>` is not evaluated, so a nested
  `<cl:transformation>` has no effect on the parent tag.

### Images

```jsp
<cl:image src="docs_sample" format="jpg" width="300" height="200" crop="fill" gravity="auto"/>
```

For chained transformations, use the `transformation` attribute with a raw transformation string.
Whitespace separates the chained components:

```jsp
<cl:image src="docs_sample" transformation="c_fill,g_auto,h_400,w_400 e_sharpen f_auto,q_auto"/>
```

### URL only

Emitting the URL alone lets the JSP escape the surrounding attributes itself. `<cl:url>` has no
`var` attribute; it writes the URL directly to the page output, so the tag alone cannot capture it
into a variable:

```jsp
<img src="<cl:url src='docs_sample' width='600' crop='fill' fetch_format='auto' quality='auto'/>"
     alt="<c:out value='${product.name}'/>"/>
```

To use the URL as a variable, build it in the controller with `cloudinary.url().generate(...)` and
expose it as a model attribute. This is the more flexible path for anything non-trivial — see
[use-with-spring-boot.md](use-with-spring-boot.md).

### Video

```jsp
<cl:video src="docs_video" width="640" crop="scale" quality="auto"/>
```

`<cl:video>` also accepts `sourceTypes`, `poster`, `autoplay`, `controls`, `loop`, `muted`, and
`preload` as declared attributes.

`<cl:video>` emits `<source>` elements for WebM, MP4, and OGV, plus a generated poster image. Pass
the public ID without an extension.

## Attributes from user input

The tags are for attribute values written by the developer. For text that comes from users, emit
the URL with `<cl:url>` and write the surrounding tag yourself, escaping user text with `<c:out>`,
as shown under [URL only](#url-only).

## Upload fields

`<cl:upload>` renders a file input whose parameters are signed server-side using the registered
singleton's API secret:

```jsp
<form action="/products" method="post">
    <cl:upload fieldName="image" resourceType="image" folder="products" tags="product"/>
    <button type="submit">Save</button>
</form>
```

The tag signs every upload parameter it emits. Cloudinary rejects an upload whose parameters
differ from the signed set, so the client cannot add or change parameters. See
[sign-browser-upload.md](sign-browser-upload.md).

`<cl:unsignedUpload>` requires an upload preset and involves no secret, so anyone who can read the
page can upload. Constrain the preset accordingly.

## Choosing the taglib

The taglib targets `javax.*` servlet APIs, so it requires shims on Jakarta EE 9+. For a new Spring
Boot application, generating URL strings in a controller
or service and rendering them with Thymeleaf is the more common route — see
[use-with-spring-boot.md](use-with-spring-boot.md).

The taglib fits an existing JSP codebase where the transformation syntax is wanted inline.

## Related

- [use-with-spring-boot.md](use-with-spring-boot.md)
- [transform-and-deliver-image.md](transform-and-deliver-image.md)
- [sign-browser-upload.md](sign-browser-upload.md)
