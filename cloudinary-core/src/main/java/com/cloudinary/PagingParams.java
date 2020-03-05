package com.cloudinary;

/**
 * Defines interface for providers of paging parameters like "max_results" and "next_cursor".
 */
public interface PagingParams {
    /**
     * @return value of "max_results" parameter
     */
    Integer getMaxResults();

    /**
     * @return value of "next_cursor" parameter
     */
    String getNextCursor();
}
