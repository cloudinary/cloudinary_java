# Security Policy

## Supported versions

| Version | Supported |
|---------|-----------|
| 2.x     | Yes       |
| 1.x     | No        |

## Reporting a vulnerability

Report vulnerabilities privately through [GitHub private vulnerability reporting](https://github.com/cloudinary/cloudinary_java/security/advisories/new) for this repository.

If you cannot use GitHub reporting, contact Cloudinary support at [support.cloudinary.com](https://support.cloudinary.com/hc/en-us/requests/new) and mark the ticket as a security issue.

Use these private channels for anything security-sensitive; public GitHub issues are for regular bugs and feature requests.

## What to include in a report

- The affected SDK version, which artifact (`cloudinary-core`, `cloudinary-http5`, `cloudinary-taglib`), and the Java version.
- A minimal reproduction or proof of concept.
- The impact you believe the issue has (for example: credential exposure, signature bypass, markup injection).
- Any suggested remediation, if you have one.

Never include real credentials in a report. A Cloudinary API secret grants full control of a product environment; redact cloud names, keys, and secrets, and rotate anything you may have exposed while investigating.

## Response and disclosure process

- We acknowledge reports and keep you informed while the issue is investigated.
- Fixes are released as patched artifact versions; the changelog notes security-relevant changes without disclosing exploit details before users can upgrade.
- Please give us reasonable time to release a fix before public disclosure.

## Security guidance for SDK users

- Your `api_secret` is a server-side credential. Keep it on your server; browsers, mobile binaries, and repositories should only ever hold delivery URLs or short-lived signatures.
- Provide credentials through the `CLOUDINARY_URL` environment variable rather than hardcoding them.
- For uploads initiated from a browser or mobile app, generate the signature on your server, and sign every parameter you intend to constrain — an unsigned parameter is one the client can choose. See [docs/sign-browser-upload.md](docs/sign-browser-upload.md).
- For unsigned uploads, use a deliberately restricted [unsigned upload preset](https://cloudinary.com/documentation/upload_presets) ([md](https://cloudinary.com/documentation/upload_presets.md)).
- For attribute values that come from user input, build the URL with `generate()` and render the tag in your template engine. See [docs/transform-and-deliver-image.md](docs/transform-and-deliver-image.md#attributes-from-user-input).
- Cloudinary platform security documentation: https://cloudinary.com/documentation/solution_overview#security
