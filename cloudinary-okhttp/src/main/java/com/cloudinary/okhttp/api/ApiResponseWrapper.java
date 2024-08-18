package com.cloudinary.okhttp.api;

import com.cloudinary.api.ApiResponse;
import com.cloudinary.api.RateLimit;
import com.cloudinary.utils.StringUtils;
import okhttp3.Headers;
import okhttp3.Response;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("rawtypes")
public class ApiResponseWrapper extends HashMap implements ApiResponse {
    private static final long serialVersionUID = -5458609797599845837L;
    private Response response = null;

    // Regex pattern to match rate limit headers
    private static final Pattern RATE_LIMIT_REGEX = Pattern
            .compile("X-FEATURE(\\w*)RATELIMIT(-LIMIT|-RESET|-REMAINING)", Pattern.CASE_INSENSITIVE);

    // Date format for parsing reset time
    private static final String RFC1123_PATTERN = "EEE, dd MMM yyyyy HH:mm:ss z";
    private static final DateFormat RFC1123 = new SimpleDateFormat(RFC1123_PATTERN, Locale.ENGLISH);

    @SuppressWarnings("unchecked")
    public ApiResponseWrapper(Response response, Map result) {
        super(result);
        this.response = response;
    }

    public Response getRawHttpResponse() {
        return this.response;
    }

    public Map<String, RateLimit> rateLimits() throws java.text.ParseException {
        Headers headers = this.response.headers();
        Map<String, RateLimit> limits = new HashMap<>();

        for (String name : headers.names()) {
            Matcher m = RATE_LIMIT_REGEX.matcher(name);
            if (m.matches()) {
                String limitName = "Api";
                RateLimit limit = limits.get(limitName);
                if (limit == null) {
                    limit = new RateLimit();
                }
                if (!StringUtils.isEmpty(m.group(1))) {
                    limitName = m.group(1);
                }

                switch (m.group(2).toLowerCase()) {
                    case "-limit":
                        limit.setLimit(Long.parseLong(headers.get(name)));
                        break;
                    case "-remaining":
                        limit.setRemaining(Long.parseLong(headers.get(name)));
                        break;
                    case "-reset":
                        limit.setReset(RFC1123.parse(headers.get(name)));
                        break;
                }
                limits.put(limitName, limit);
            }
        }
        return limits;
    }

    public RateLimit apiRateLimit() throws java.text.ParseException {
        return rateLimits().get("Api");
    }
}
