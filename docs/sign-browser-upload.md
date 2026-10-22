# Sign a browser upload

When a browser or mobile client uploads straight to Cloudinary, the bytes never pass through your
server. The API secret stays on your server: the server signs a short parameter set, and the client
sends that signature along with the file.

Two approaches:

| Approach | Secret needed | Client can change | Use when |
|---|---|---|---|
| **Signed** | Yes, server-side | Only what you signed | Default choice |
| **Unsigned** | No | Anything the preset allows | Public uploads where the preset's constraints are sufficient |

## Signed upload

### Server side

```java
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import java.util.HashMap;
import java.util.Map;

public Map<String, Object> uploadSignature() {
    Cloudinary cloudinary = new Cloudinary();     // reads CLOUDINARY_URL

    Map<String, Object> params = new HashMap<String, Object>();
    params.put("timestamp", System.currentTimeMillis() / 1000L);
    params.put("folder", "user_uploads");
    params.put("source", "uw");

    // Mutates params in place: adds "signature" and "api_key".
    cloudinary.uploader().signRequestParams(params, ObjectUtils.emptyMap());

    return params;   // -> {api_key=..., signature=..., timestamp=..., folder=..., source=...}
}
```

`signRequestParams` returns `void`. It mutates the map you pass in, adding `signature` and
`api_key`; assigning its result, as in `Map signed = cloudinary.uploader().signRequestParams(...)`,
does not compile.

Return that map to the client as JSON. It contains the API key and a signature but not the API
secret.

### What you sign is what you constrain

The signature covers exactly the parameters you put in the map. Parameters you do not sign are set
by the client.

Sign every parameter you want to fix:

```java
params.put("timestamp", System.currentTimeMillis() / 1000L);
params.put("folder", "user_uploads");           // confine to a folder
params.put("public_id", "user_" + userId);      // pin the name
params.put("eager", "c_limit,w_2000");          // cap the derived size
```

`timestamp` is mandatory and the signature is short-lived: Cloudinary rejects a signature whose
timestamp is more than one hour old. Generate it per upload rather than caching it.

### Client side

```html
<input type="file" id="file"/>
<script>
const CLOUD_NAME = 'my-cloud';

document.getElementById('file').addEventListener('change', async (event) => {
  // 1. Ask your server to sign.
  const signed = await fetch('/api/cloudinary-signature').then(r => r.json());

  // 2. POST the file plus the signed parameters.
  const form = new FormData();
  form.append('file', event.target.files[0]);
  for (const [key, value] of Object.entries(signed)) {
    form.append(key, value);
  }

  const response = await fetch(
      `https://api.cloudinary.com/v1_1/${CLOUD_NAME}/image/upload`,
      { method: 'POST', body: form });

  const result = await response.json();
  console.log(result.public_id, result.secure_url, result.asset_id);
});
</script>
```

Store `asset_id` from the response as the durable handle — see
[search-and-manage-assets.md](search-and-manage-assets.md).

## Unsigned upload

Unsigned uploads need an upload preset with `unsigned` enabled. No secret and no signature are
involved, so anyone with access to your page can upload to your product environment. The preset's
settings are the only constraint on what is uploaded.

Create one server-side:

```java
cloudinary.api().createUploadPreset(ObjectUtils.asMap(
        "name",             "browser_unsigned",
        "unsigned",         true,
        "folder",           "public_uploads",
        "allowed_formats",  "jpg,png,webp",
        "moderation",       "manual"));
```

Then from the client, post `upload_preset` instead of a signature:

```javascript
form.append('upload_preset', 'browser_unsigned');
```

If you enable unsigned uploads, constrain the preset: pin a folder, restrict `allowed_formats`, cap
dimensions, and consider `moderation`. Moderation does not block delivery of pending assets by
default — see [moderate-upload.md](moderate-upload.md).

The SDK also has `unsignedUpload` for server-side use with a preset. A server that already holds
the secret can use the normal signed `upload()` instead.

## Verifying the result

A client's report of what it uploaded is not authoritative. Confirm a reported upload against the
API:

```java
Map asset = cloudinary.api().resourceByAssetID(reportedAssetId, ObjectUtils.emptyMap());
```

Alternatively, pass a `notification_url` when signing and treat Cloudinary's callback as the source
of truth. Verify its signature:

```java
boolean valid = cloudinary.verifyNotificationSignature(
        requestBody,
        request.getHeader("X-Cld-Timestamp"),
        request.getHeader("X-Cld-Signature"),
        7200L);                                   // validity window in seconds
```

There is also `verifyApiResponseSignature(String publicId, String version, String signature)` for
checking a signature returned in an upload response.

## Related

- [get-credentials.md](get-credentials.md)
- [upload-image.md](upload-image.md)
- [moderate-upload.md](moderate-upload.md)
- [Upload widget](https://cloudinary.com/documentation/upload_widget.md)
