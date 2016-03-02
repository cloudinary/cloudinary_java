package com.cloudinary.api;

import java.util.Date;

public class RateLimit {
    private long limit = 0L;
    private long remaining = 0L;
    private Date reset = null;

    public RateLimit() {
        super();
    }

    public long getLimit() {
        return limit;
    }

    public void setLimit(long limit) {
        this.limit = limit;
    }

    public long getRemaining() {
        return remaining;
    }

    public void setRemaining(long remaining) {
        this.remaining = remaining;
    }

    public Date getReset() {
        return reset;
    }

    public void setReset(Date reset) {
        this.reset = reset;
    }
}
