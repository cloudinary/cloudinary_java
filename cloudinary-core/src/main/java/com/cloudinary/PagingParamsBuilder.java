package com.cloudinary;

/**
 * Convenient builder class for producing {@link PagingParams} instances.
 */
public class PagingParamsBuilder {
    private Integer maxResults;
    private String nextCursor;

    private PagingParamsBuilder() {
    }

    public static PagingParamsBuilder newInstance() {
        return new PagingParamsBuilder();
    }

    /**
     * Set "max_results" parameter value for {@link PagingParams} instance being built.
     *
     * @param maxResults value of "max_results" parameter
     * @return instance of this builder (for chaining calls)
     */
    public PagingParamsBuilder maxResults(int maxResults) {
        this.maxResults = maxResults;
        return this;
    }

    /**
     * Set "next_cursor" parameter value for {@link PagingParams} instance being built.
     *
     * @param nextCursor value of "next_cursor" parameter
     * @return instance of this builder (for chaining calls)
     */
    public PagingParamsBuilder nextCursor(String nextCursor) {
        this.nextCursor = nextCursor;
        return this;
    }

    /**
     * Constructs the {@link PagingParams} instance being built and returns to consumer.
     *
     * @return built instance of {@link PagingParams}
     */
    public PagingParams build() {
        return new MyPagingParams(maxResults, nextCursor);
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
