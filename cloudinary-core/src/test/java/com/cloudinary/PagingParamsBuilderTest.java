package com.cloudinary;

import org.junit.Test;

import static org.junit.Assert.*;

public class PagingParamsBuilderTest {
    @Test
    public void shouldBuildNotNull() {
        PagingParamsBuilder builder = PagingParamsBuilder.newInstance();

        PagingParams actual = builder.build();

        assertNotNull(actual);
    }

    @Test
    public void shouldBuildWithNoMaxResultsParamWhenNotProvided() {
        PagingParamsBuilder builder = PagingParamsBuilder.newInstance();

        PagingParams actual = builder.build();

        assertNull(actual.getMaxResults());
    }

    @Test
    public void shouldBuildWithNoNextCursorParamWhenNotProvided() {
        PagingParamsBuilder builder = PagingParamsBuilder.newInstance();

        PagingParams actual = builder.build();

        assertNull(actual.getNextCursor());
    }

    @Test
    public void shouldBuildWithMaxResultsWhenProvided() {
        PagingParamsBuilder builder = PagingParamsBuilder.newInstance();
        builder.maxResults(10);

        PagingParams actual = builder.build();

        assertEquals(Integer.valueOf(10), actual.getMaxResults());
    }

    @Test
    public void shouldBuildWithNextCursorWhenProvided() {
        PagingParamsBuilder builder = PagingParamsBuilder.newInstance();
        builder.nextCursor("123");

        PagingParams actual = builder.build();

        assertEquals("123", actual.getNextCursor());
    }

    @Test
    public void shouldBuildWithLatestProvidedValue() {
        PagingParamsBuilder builder = PagingParamsBuilder.newInstance();
        builder.nextCursor("123").nextCursor("234");

        PagingParams actual = builder.build();

        assertEquals("234", actual.getNextCursor());
    }
}