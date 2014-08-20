package com.cloudinary.api;

import java.util.Map;

import org.apache.http.HttpResponse;

@SuppressWarnings("rawtypes")
public interface ApiResponse extends Map {
	HttpResponse getRawHttpResponse();
	Map<String, RateLimit> rateLimits() throws java.text.ParseException;
	RateLimit apiRateLimit() throws java.text.ParseException;
}
