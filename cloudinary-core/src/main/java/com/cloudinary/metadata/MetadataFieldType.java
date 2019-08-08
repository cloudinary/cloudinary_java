package com.cloudinary.metadata;

/**
 * Enum represneting all the valid field types.
 */
public enum MetadataFieldType  {
    STRING,
    INTEGER,
    DATE,
    ENUM,
    SET;

    @Override
    public String toString() {
        return super.toString().toLowerCase();
    }
}
