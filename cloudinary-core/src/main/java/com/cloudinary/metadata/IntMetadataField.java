package com.cloudinary.metadata;

/**
 * Represents a metadata field with 'Int' type.
 */
public class IntMetadataField extends AbstractMetadataField<Integer> {
    public IntMetadataField() {
        super(MetadataFieldType.INTEGER);
    }
}
