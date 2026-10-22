# Use structured metadata

Structured metadata is a schema defined on the product environment: named, typed fields that assets
can carry. It is distinct from `context`, which is free-form key/value pairs with no schema.

| Feature | Schema | Validation | Use when |
|---|---|---|---|
| Structured metadata (`metadata`) | Yes — fields declared up front | Yes, server-side | You need typed, validated, queryable fields |
| Context (`context`) | No | No | You need ad-hoc labels with no ceremony |

## Defining fields

Each field type has its own class in `com.cloudinary.metadata`. The `externalId` is the key you use
when uploading, and it is immutable once created.

```java
import com.cloudinary.metadata.*;

StringMetadataField field = new StringMetadataField();
field.setExternalId("photographer");
field.setLabel("Photographer");
field.setMandatory(false);

cloudinary.api().addMetadataField(field);
```

Available field types:

| Class | Stores |
|---|---|
| `StringMetadataField` | Free text |
| `IntMetadataField` | Integer |
| `DateMetadataField` | Date |
| `EnumMetadataField` | One value from a fixed list |
| `SetMetadataField` | Several values from a fixed list |

`EnumMetadataField` and `SetMetadataField` need a datasource of permitted values.

`EnumMetadataField` has a package-private constructor and no factory method, so
`new EnumMetadataField()` does not compile outside the SDK's own package:

```
EnumMetadataField() is not public in EnumMetadataField;
cannot be accessed from outside package
```

To define an enum field, use the generic base class with an explicit type:

```java
import com.cloudinary.metadata.*;

MetadataDataSource datasource = new MetadataDataSource(Arrays.asList(
        new MetadataDataSource.Entry("editorial", "Editorial"),
        new MetadataDataSource.Entry("commercial", "Commercial")));

MetadataField<String> usage = new MetadataField<String>(MetadataFieldType.ENUM);
usage.setExternalId("usage_rights");
usage.setLabel("Usage rights");
usage.setDataSource(datasource);

cloudinary.api().addMetadataField(usage);
```

`SetMetadataField` has a public constructor, so for multi-value fields `new SetMetadataField()`
works directly.

An `Entry` is `(externalId, value)` — the first is what you store, the second what a human sees.
There is also a single-argument `Entry(String)` form.

### Mandatory fields apply to uploads

Setting `setMandatory(true)` means uploads that omit the field are rejected. Adding a mandatory
field therefore affects every existing upload path in your application. Set a default value if
uploads that omit the field should continue to succeed:

```java
field.setMandatory(true);
field.setDefaultValue("unknown");
```

## Setting values at upload time

```java
Map result = cloudinary.uploader().upload(file, ObjectUtils.asMap(
        "public_id", "shoots/2026/hero",
        "metadata",  ObjectUtils.asMap(
                "photographer",  "A. Photographer",
                "usage_rights",  "editorial")));

System.out.println(result.get("metadata"));
```

Values are validated against the field definition, so an enum value outside the datasource or a
non-integer for an `IntMetadataField` rejects the upload.

Metadata keys must reference fields that exist in the environment. An unknown external ID rejects
the whole upload rather than being dropped:

```java
cloudinary.uploader().upload(file, ObjectUtils.asMap(
        "public_id", "x",
        "metadata",  ObjectUtils.asMap("no_such_field", "value")));
```

```
java.lang.RuntimeException: Metadata External IDs do not exist: ["no_such_field"]
```

This is a `java.lang.RuntimeException` from the upload path rather than a typed exception — see
[troubleshoot-errors.md](troubleshoot-errors.md). With `"return_error", true` the same message is
returned as data, with `http_code` 400. Unknown upload *parameters*, by contrast, are ignored and
the upload proceeds ([upload-image.md](upload-image.md)); unknown metadata keys reject it. Create
fields with `addMetadataField` before referencing them.

## Updating values on existing assets

Two ways, and they differ in scope:

```java
// One asset, via the Admin API.
cloudinary.api().update("shoots/2026/hero",
        ObjectUtils.asMap("metadata", ObjectUtils.asMap("photographer", "B. Photographer")));

// Many assets at once, via the Uploader.
Map result = cloudinary.uploader().updateMetadata(
        ObjectUtils.asMap("photographer", "B. Photographer"),
        new String[]{"shoots/2026/hero", "shoots/2026/alt"},
        ObjectUtils.emptyMap());

// -> {public_ids=[shoots/2026/hero, shoots/2026/alt]}
```

`updateMetadata` returns the list of public IDs it updated — no asset fields and no per-asset
success flags. An ID that does not exist is absent from that list, so compare the list against
what you sent to detect misses.

Both take `public_id`, not `asset_id`. If you hold an asset ID, resolve it first — see
[search-and-manage-assets.md](search-and-manage-assets.md).

## Reading values back

```java
Map asset = cloudinary.api().resource("shoots/2026/hero", ObjectUtils.emptyMap());
Map metadata = (Map) asset.get("metadata");
```

Fields that have not been set are absent from the map rather than present with a `null` value, so
null-check each key you read.

## Searching on metadata

Metadata fields are queryable by external ID:

```java
Map result = cloudinary.search()
        .expression("metadata.usage_rights=editorial")
        .execute();
```

Leading wildcards are rejected in search expressions — see
[search-and-manage-assets.md](search-and-manage-assets.md).

## Managing the schema

Unlike most Admin API methods, these take no trailing options `Map`:

```java
cloudinary.api().listMetadataFields();                          // no arguments
cloudinary.api().metadataFieldByFieldId("photographer");
cloudinary.api().updateMetadataField("photographer", field);
cloudinary.api().deleteMetadataField("photographer");
cloudinary.api().addMetadataField(field);                       // one argument
```

`addMetadataField` returns the stored definition, including defaults the server filled in:

```
{default_disabled=false, restrictions={readonly_ui=false, hidden_ui=false,
 excluded_from_search=false}, external_id=photographer, default_value=null,
 label=Photographer, type=string, mandatory=false, validation=null, group=default}
```

Deleting a field removes it from the schema; values already stored on assets become inaccessible
through it. There are also conditional-rule methods (`addMetadataRule`, `listMetadataRules`,
`updateMetadataRule`, `deleteMetadataRule`) for making one field's validity depend on another.

## Related

- [upload-image.md](upload-image.md)
- [search-and-manage-assets.md](search-and-manage-assets.md)
- [Structured metadata documentation](https://cloudinary.com/documentation/structured_metadata.md)
