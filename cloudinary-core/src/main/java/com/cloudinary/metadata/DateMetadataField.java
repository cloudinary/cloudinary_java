package com.cloudinary.metadata;

import java.util.Date;

public class DateMetadataField extends AbstractMetadataField<Date> {
    public DateMetadataField() {
        super(MetadataFieldType.DATE);
    }
}
