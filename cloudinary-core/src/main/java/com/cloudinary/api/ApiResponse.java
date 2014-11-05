package com.cloudinary.api;

import java.util.Map;

@SuppressWarnings("rawtypes")
public interface ApiResponse extends Map {
	Map<String, RateLimit> rateLimits() throws java.text.ParseException;
	RateLimit apiRateLimit() throws java.text.ParseException;
}
