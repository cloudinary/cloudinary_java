package com.cloudinary.metadata;

/**
 * Represents a metadata field with 'Enum' type.
 */
public class EnumMetadataField extends AbstractMetadataValuesField<String>{
    EnumMetadataField() {
        super(MetadataFieldType.ENUM);
    }
}
