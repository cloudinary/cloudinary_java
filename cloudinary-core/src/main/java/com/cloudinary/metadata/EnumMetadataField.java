package com.cloudinary.metadata;

/**
 * Represents a metadata field with 'Enum' type.
 */
public class EnumMetadataField extends MetadataField<String> {
    EnumMetadataField() {
        super(MetadataFieldType.ENUM);
    }
}
