package com.cloudinary.http5.api;

import com.cloudinary.api.ApiResponse;
import com.cloudinary.api.RateLimit;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpResponse;
import java.text.ParseException;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Response extends HashMap implements ApiResponse {
    private static final long serialVersionUID = -5458609797599845837L;
    private final HttpResponse response;

    @SuppressWarnings("unchecked")
    public Response(HttpResponse response, Map<String, Object> result) {
        super(result);
        this.response = response;
    }

    public HttpResponse getRawHttpResponse() {
        return this.response;
    }

    private static final Pattern RATE_LIMIT_REGEX = Pattern
            .compile("X-FEATURE(\\w*)RATELIMIT(-LIMIT|-RESET|-REMAINING)", Pattern.CASE_INSENSITIVE);
    private static final String RFC1123_PATTERN = "EEE, dd MMM yyyy HH:mm:ss z";
    private static final DateFormat RFC1123 = new SimpleDateFormat(RFC1123_PATTERN, Locale.ENGLISH);

    public Map<String, RateLimit> rateLimits() throws ParseException {
        Header[] headers = this.response.getHeaders();
        Map<String, RateLimit> limits = new HashMap<>();
        for (Header header : headers) {
            Matcher m = RATE_LIMIT_REGEX.matcher(header.getName());
            if (m.matches()) {
                String limitName = "Api";
                RateLimit limit = limits.getOrDefault(limitName, new RateLimit());
                if (!m.group(1).isEmpty()) {
                    limitName = m.group(1);
                }
                if (m.group(2).equalsIgnoreCase("-limit")) {
                    limit.setLimit(Long.parseLong(header.getValue()));
                } else if (m.group(2).equalsIgnoreCase("-remaining")) {
                    limit.setRemaining(Long.parseLong(header.getValue()));
                } else if (m.group(2).equalsIgnoreCase("-reset")) {
                    limit.setReset(RFC1123.parse(header.getValue()));
                }
                limits.put(limitName, limit);
            }
        }
        return limits;
    }

    public RateLimit apiRateLimit() throws ParseException {
        return rateLimits().get("Api");
    }
}
