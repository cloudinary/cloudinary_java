package com.cloudinary.metadata;

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
