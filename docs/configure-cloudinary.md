# Configure the SDK

Credentials can be supplied in four ways. Pick one and use it consistently.

## 1. `CLOUDINARY_URL` environment variable

```bash
export CLOUDINARY_URL="cloudinary://123456789012345:abcdeghijklmnopqrstuvwxyz12@my-cloud"
```

```java
Cloudinary cloudinary = new Cloudinary();
```

The no-argument constructor reads the `CLOUDINARY_URL` system property first, then falls back to
the environment variable of the same name:

```java
// Equivalent to what the no-arg constructor does:
System.getProperty("CLOUDINARY_URL", System.getenv("CLOUDINARY_URL"))
```

`-DCLOUDINARY_URL=...` on the JVM command line therefore overrides the environment variable. If
neither is set, the constructor returns a `Cloudinary` with no credentials; an
`IllegalArgumentException` is thrown on the first call that needs them.

## 2. Connection URL passed explicitly

```java
Cloudinary cloudinary = new Cloudinary(
        "cloudinary://123456789012345:abcdeghijklmnopqrstuvwxyz12@my-cloud");
```

This form suits tests, where a dependency on ambient environment state is undesirable.

## 3. A `Map` of individual settings

```java
Cloudinary cloudinary = new Cloudinary(ObjectUtils.asMap(
        "cloud_name", "my-cloud",
        "api_key", "123456789012345",
        "api_secret", "abcdeghijklmnopqrstuvwxyz12",
        "secure", true));
```

Use this when credentials arrive from a secret manager or a framework's configuration binding
rather than from a URL. See [use-with-spring-boot.md](use-with-spring-boot.md).

## 4. A `Configuration` object

```java
Configuration configuration = new Configuration();
configuration.cloudName = "my-cloud";
configuration.apiKey = "123456789012345";
configuration.apiSecret = "abcdeghijklmnopqrstuvwxyz12";
configuration.secure = true;
Cloudinary cloudinary = new Cloudinary(configuration);
```

`Configuration` fields are public and mutable. A `Configuration.Builder` is also available. Both
start with `secure` set to `false` and analytics off, so set `secure` explicitly; without it,
`url().generate(...)` returns `http://` URLs.

## Cloud names containing an underscore

`Configuration.from()` parses the connection URL with `java.net.URI` and reads the cloud name from
`URI.getHost()`. Underscores are not permitted in DNS host names, so for such a URL `getHost()`
returns `null`, and so does `getUserInfo()`. Cloud name, API key, and API secret are all `null`
after parsing, and no exception is thrown at that point:

```java
// A cloud name with a hyphen — parses correctly.
Configuration.from("cloudinary://key:secret@my-cloud").cloudName;  // "my-cloud"

// A cloud name with an underscore — yields nulls.
Configuration.from("cloudinary://key:secret@my_cloud").cloudName;  // null
Configuration.from("cloudinary://key:secret@my_cloud").apiKey;     // null
```

The exception appears on the first call that needs configuration. Signed uploads and Admin API
calls check credentials first:

```
java.lang.IllegalArgumentException: Must supply api_key
```

URL generation and unsigned uploads check the cloud name:

```
java.lang.IllegalArgumentException: Must supply cloud_name in tag or in configuration
```

For a cloud name containing an underscore, use the `Map` or `Configuration` form (options 3 and 4),
which do not go through URI parsing. This behaviour is also recorded in
[troubleshoot-errors.md](troubleshoot-errors.md).

## Verifying your configuration works

`api().ping()` is the cheapest round trip that confirms credentials are valid:

```java
Map result = cloudinary.api().ping(ObjectUtils.emptyMap());
System.out.println(result.get("status"));   // "ok"
```

Reading configuration back confirms that it parsed, not that it is valid:

```java
System.out.println(cloudinary.config.cloudName);   // null means parsing failed
```

A `null` check here at startup distinguishes "credentials absent or unparseable" from
"credentials rejected by the server".

## Configuration options

| Field / key | Default | Meaning |
|---|---|---|
| `secure` | `true` from a `Map` or `CLOUDINARY_URL`; `false` from `new Configuration()` or `Configuration.Builder` | Generate `https://` delivery URLs. |
| `cname` | unset | Custom delivery domain. |
| `private_cdn` | `false` | Use a private CDN distribution. |
| `upload_prefix` | `https://api.cloudinary.com` | API base. Change only for testing against a different endpoint. |
| `timeout` | `0` (the SDK sets none; HttpClient's default 3-minute read timeout applies) | Request timeout in seconds. |
| `force_version` | `true` | Include the version segment in delivery URLs. |
| `analytics` | enabled from a `Map` or `CLOUDINARY_URL`; off from `new Configuration()` or `Configuration.Builder` | Appends a `_a=` query parameter to generated URLs. See below. |

With configuration from a `Map` or `CLOUDINARY_URL`, `secure` defaults to `true`, so
`url().generate(...)` returns an `https://` URL unless configured otherwise. The upload response contains both `url` (http) and `secure_url` (https); read
`secure_url`.

### The `_a=` query parameter

Generated delivery URLs carry an analytics parameter:

```
https://res.cloudinary.com/my-cloud/image/upload/sample.jpg?_a=DAGCg+ALZAA0
```

This parameter encodes the SDK and language version; it is not a cache-buster. It appears on URLs
built by `url().generate()` and is not present in the `secure_url` field of an upload response. It
is not added to a URL that already has a query string, such as one carrying an auth token.

`cloudinary.setAnalytics(...)` changes the value of `_a=`. To turn it off, disable it in
configuration:

```java
Cloudinary cloudinary = new Cloudinary(ObjectUtils.asMap(
        "cloud_name", "my-cloud",
        "analytics", false));
```

## Protecting credentials

The API secret grants full control over the product environment. Keep it server-side, out of
version control, and out of anything shipped to a browser or mobile client. For uploads from a
browser, sign the request server-side; see [sign-browser-upload.md](sign-browser-upload.md).
