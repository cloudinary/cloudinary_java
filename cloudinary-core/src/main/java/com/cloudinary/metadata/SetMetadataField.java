package com.cloudinary.metadata;

import java.util.List;

public class SetMetadataField extends AbstractMetadataField<List<String>> {
    public SetMetadataField() {
        super(MetadataFieldType.SET);
    }
}
