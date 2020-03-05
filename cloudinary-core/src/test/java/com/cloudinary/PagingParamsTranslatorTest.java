package com.cloudinary;

import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PagingParamsTranslatorTest {
    @Test
    public void shouldReturnEmptyMapWhenParamsNull() {
        Map<String, ?> actual = PagingParamsTranslator.toMap(null);

        assertTrue(actual.isEmpty());
    }

    @Test
    public void shouldReturnEmptyMapWhenParamsEmpty() {
        Map<String, ?> actual = PagingParamsTranslator.toMap(new MyPagingParams(null, null));

        assertTrue(actual.isEmpty());
    }

    @Test
    public void shouldContainMaxResultsWhenParamsMaxResultsPresent() {
        Map<String, ?> actual = PagingParamsTranslator.toMap(new MyPagingParams(10, null));

        assertEquals(10, actual.get("max_results"));
    }

    @Test
    public void shouldContainNextCursorWhenParamsNextCursorPresent() {
        Map<String, ?> actual = PagingParamsTranslator.toMap(new MyPagingParams(null, "asdasdasdasdasd"));

        assertEquals("asdasdasdasdasd", actual.get("next_cursor"));
    }

    private static class MyPagingParams implements PagingParams {
        private Integer maxResults;
        private String nextCursor;

        public MyPagingParams(Integer maxResults, String nextCursor) {
            this.maxResults = maxResults;
            this.nextCursor = nextCursor;
        }

        @Override
        public Integer getMaxResults() {
            return maxResults;
        }

        @Override
        public String getNextCursor() {
            return nextCursor;
        }
    }
}