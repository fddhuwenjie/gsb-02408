package com.gsb.apitest.http;

import com.gsb.apitest.util.JsonUtils;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class HttpResponse {

    private int statusCode;
    private String statusMessage;
    private Map<String, List<String>> headers = new LinkedHashMap<>();
    private String body;
    private long responseTimeMs;
    private String requestUrl;
    private String requestMethod;
    private String requestBody;

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, List<String>> headers) {
        this.headers = headers;
    }

    public String getHeader(String name) {
        if (headers == null) return null;
        for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(name) && entry.getValue() != null && !entry.getValue().isEmpty()) {
                return entry.getValue().get(0);
            }
        }
        return null;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public long getResponseTimeMs() {
        return responseTimeMs;
    }

    public void setResponseTimeMs(long responseTimeMs) {
        this.responseTimeMs = responseTimeMs;
    }

    public String getRequestUrl() {
        return requestUrl;
    }

    public void setRequestUrl(String requestUrl) {
        this.requestUrl = requestUrl;
    }

    public String getRequestMethod() {
        return requestMethod;
    }

    public void setRequestMethod(String requestMethod) {
        this.requestMethod = requestMethod;
    }

    public String getRequestBody() {
        return requestBody;
    }

    public void setRequestBody(String requestBody) {
        this.requestBody = requestBody;
    }

    @SuppressWarnings("unchecked")
    public <T> T jsonPath(String path) {
        if (body == null || body.isEmpty()) {
            return null;
        }
        try {
            return (T) com.jayway.jsonpath.JsonPath.using(JsonUtils.jsonPathConfig()).parse(body).read(path);
        } catch (Exception e) {
            return null;
        }
    }

    public String jsonPathAsString(String path) {
        Object value = jsonPath(path);
        if (value == null) {
            return null;
        }
        if (value instanceof String) {
            return (String) value;
        }
        return JsonUtils.toJson(value);
    }

    public boolean is2xxSuccessful() {
        return statusCode >= 200 && statusCode < 300;
    }

    public Map<String, Object> bodyAsMap() {
        if (body == null || body.isEmpty()) {
            return Collections.emptyMap();
        }
        return JsonUtils.toMap(body);
    }
}
