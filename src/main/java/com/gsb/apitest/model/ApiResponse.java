package com.gsb.apitest.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ApiResponse {
    private final int statusCode;
    private final String body;
    private final Map<String, List<String>> headers;
    private final long responseTimeMs;

    public ApiResponse(int statusCode, String body, Map<String, List<String>> headers, long responseTimeMs) {
        this.statusCode = statusCode;
        this.body = body;
        this.headers = headers != null ? headers : new HashMap<>();
        this.responseTimeMs = responseTimeMs;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getBody() {
        return body;
    }

    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    public Map<String, String> getSingleValueHeaders() {
        Map<String, String> result = new HashMap<>();
        headers.forEach((k, v) -> {
            if (v != null && !v.isEmpty()) {
                result.put(k, v.get(0));
            }
        });
        return result;
    }

    public String getHeader(String name) {
        List<String> values = headers.get(name);
        if (values == null) {
            for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
                if (entry.getKey().equalsIgnoreCase(name)) {
                    values = entry.getValue();
                    break;
                }
            }
        }
        return values != null && !values.isEmpty() ? values.get(0) : null;
    }

    public long getResponseTimeMs() {
        return responseTimeMs;
    }

    public boolean isSuccessful() {
        return statusCode >= 200 && statusCode < 300;
    }
}
