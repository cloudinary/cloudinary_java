package com.cloudinary.metadata;

/**
 * Represents a metadata field with 'String' type.
 */
public class StringMetadataField extends AbstractMetadataField<String> {
    public StringMetadataField() {
        super(MetadataFieldType.STRING);
    }
}