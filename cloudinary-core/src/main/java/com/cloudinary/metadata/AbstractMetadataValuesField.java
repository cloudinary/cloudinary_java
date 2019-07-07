package com.cloudinary.metadata;

/**
 * This class is the abstract base for field classes based on a list of valid values (at the moment set+enum).
 * @param <T> The type of the value.
 */
public abstract class AbstractMetadataValuesField<T> extends AbstractMetadataField<T> {
    AbstractMetadataValuesField(MetadataFieldType type) {
        super(type);
    }

    /**
     * Get the data source definition of this field.
     * @return The data source.
     */
    public MetadataDataSource getDataSource() {
        return (MetadataDataSource) optJSONObject("datasource");
    }

    /**
     * Set the datasource for the field.
     * @param dataSource The datasource to set.
     */
    public void setDataSource(MetadataDataSource dataSource) {
        put("datasource", dataSource);
    }
}
