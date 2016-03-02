package com.cloudinary.api;

import java.text.ParseException;
import java.util.Map;

@SuppressWarnings("rawtypes")
public interface ApiResponse extends Map {
    Map<String, RateLimit> rateLimits() throws ParseException;

    RateLimit apiRateLimit() throws ParseException;
}
