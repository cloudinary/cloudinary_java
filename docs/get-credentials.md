# Get credentials

You need three values: a cloud name, an API key, and an API secret. Together they are usually
expressed as one connection URL:

```
cloudinary://<api_key>:<api_secret>@<cloud_name>
```

## From the console

1. Sign up at [cloudinary.com/users/register_free](https://cloudinary.com/users/register_free).
2. Open the [console dashboard](https://console.cloudinary.com/). The cloud name, API key, and the
   full `CLOUDINARY_URL` are shown there; the API secret is behind a reveal control.
3. Put the connection URL in your environment, not in your source tree:

```bash
export CLOUDINARY_URL="cloudinary://123456789012345:abcdeghijklmnopqrstuvwxyz12@my-cloud"
```

## Throwaway environments

A Claimable Cloud is a real, temporary product environment created without a signup flow. It is
useful for running an example end to end before moving to a permanent account:

```bash
npx @cloudinary/cloud
```

It prints live credentials. Treat them as secret: keep them out of commits, issues, PR
descriptions, and chat logs, and redact them before sharing anything.

If that command fails with
`delivery_ips must contain at least one public IP address [delivery_ips_not_public]`, the request
reached Cloudinary from a private network address, typically a corporate VPN or secure gateway. This
is a network-routing condition rather than an authentication failure; whether to change VPN or
network settings is a decision for the account owner.

This repository also has its own test-cloud helper, used by the test suite:

```bash
./gradlew createTestSubAccount -PmoduleName=http5
# writes tools/cloudinary_url.txt (already git-ignored)
```

## Which value goes where

| Value | Secret? | Used for |
|---|---|---|
| Cloud name | No | Delivery URLs. Safe in client code. |
| API key | No, but treat it carefully | Identifies your environment on API calls. |
| API secret | Yes | Signing. Server-side only. |

The API secret grants full control of the product environment, including deletion. Keep it out of
browsers, mobile apps, and public repositories. If a client needs to upload, sign the request
server-side — see [sign-browser-upload.md](sign-browser-upload.md).

Delivery URLs are built from the cloud name alone, so URL-building code needs no secret:

```java
Cloudinary cloudinary = new Cloudinary(ObjectUtils.asMap("cloud_name", "my-cloud"));
cloudinary.url().generate("sample.jpg");   // works with no key or secret
```

Signed delivery URLs (`signed(true)`) need the API secret. Check that it is loaded before
generating them:

```java
if (cloudinary.config.apiSecret == null) {
    throw new IllegalStateException("API secret not configured");
}
```

## Verifying credentials

```java
Map result = cloudinary.api().ping(ObjectUtils.emptyMap());
System.out.println(result.get("status"));   // "ok"
```

A `ping` response contains exactly one key, `status`. Failures:

| Result | Meaning |
|---|---|
| `IllegalArgumentException: Must supply api_key` | Credentials never reached the SDK. |
| `AuthorizationRequired: disabled customer` | The environment is suspended or expired — common with throwaway clouds. |
| `AuthorizationRequired` with another message | Key or secret is wrong. |

If credentials are correct but the SDK reports them as `null`, check whether the cloud name contains
an underscore. A connection URL whose cloud name contains an underscore parses with cloud name, key,
and secret all `null` — see [configure-cloudinary.md](configure-cloudinary.md).

## Rotating a secret

If a secret reaches a public place, rotate it in the console immediately — under Settings, then
Access Keys. Removing it from git history does not revoke it; treat anything pushed publicly as
compromised. Generate a new key, deploy it, then disable the old one.

## Related

- [configure-cloudinary.md](configure-cloudinary.md)
- [sign-browser-upload.md](sign-browser-upload.md)
