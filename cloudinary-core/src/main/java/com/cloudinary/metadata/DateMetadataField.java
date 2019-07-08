package com.cloudinary.metadata;

import com.cloudinary.utils.ObjectUtils;

import java.text.ParseException;
import java.util.Date;

/**
 * Represents a metadata field with type 'date'
 */
public class DateMetadataField extends AbstractMetadataField<Date> {

    public DateMetadataField() {
        super(MetadataFieldType.DATE);
    }

    /**
     * Sets the default date used for this field.
     * @param defaultValue The date to set. Date only without a time component, UTC assumed.
     */
    @Override
    public void setDefaultValue(Date defaultValue) {
        put(DEFAULT_VALUE, ObjectUtils.toISO8601DateOnly(defaultValue));
    }

    /**
     * Get the default value of this date field.
     * @return The date only without a time component, UTC.
     * @throws ParseException When the underlying value is malformed.
     */
    @Override
    public Date getDefaultValue() throws ParseException {
        Object value = get(DEFAULT_VALUE);
        if (value == null) {
            return null;
        }

        return ObjectUtils.fromISO8601DateOnly(value.toString());
    }
}
