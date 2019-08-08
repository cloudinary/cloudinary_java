package com.cloudinary.metadata;

import java.util.List;

/**
 * Represents a metadata field with 'Set' type.
 */
public class SetMetadataField extends MetadataField<List<String>> {
    public SetMetadataField() {
        super(MetadataFieldType.SET);
    }
}
