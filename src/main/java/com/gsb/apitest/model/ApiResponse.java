package com.gsb.apitest.model;

import java.util.Map;

public class ApiResponse {
    private int statusCode;
    private Map<String, String> headers;
    private String body;
    private long responseTimeMs;
    private Object parsedBody;

    public ApiResponse() {}

    private ApiResponse(Builder b) {
        this.statusCode = b.statusCode;
        this.headers = b.headers;
        this.body = b.body;
        this.responseTimeMs = b.responseTimeMs;
        this.parsedBody = b.parsedBody;
    }

    public static Builder builder() {
        return new Builder();
    }

    public int getStatusCode() { return statusCode; }
    public void setStatusCode(int statusCode) { this.statusCode = statusCode; }
    public Map<String, String> getHeaders() { return headers; }
    public void setHeaders(Map<String, String> headers) { this.headers = headers; }
    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
    public long getResponseTimeMs() { return responseTimeMs; }
    public void setResponseTimeMs(long responseTimeMs) { this.responseTimeMs = responseTimeMs; }
    public Object getParsedBody() { return parsedBody; }
    public void setParsedBody(Object parsedBody) { this.parsedBody = parsedBody; }

    @SuppressWarnings("unchecked")
    public <T> T jsonPath(String path) {
        try {
            return com.jayway.jsonpath.JsonPath.read(body, path);
        } catch (Exception e) {
            throw new RuntimeException("JSON path [" + path + "] 解析失败: " + e.getMessage(), e);
        }
    }

    public static class Builder {
        private int statusCode;
        private Map<String, String> headers;
        private String body;
        private long responseTimeMs;
        private Object parsedBody;

        public Builder statusCode(int v) { this.statusCode = v; return this; }
        public Builder headers(Map<String, String> v) { this.headers = v; return this; }
        public Builder body(String v) { this.body = v; return this; }
        public Builder responseTimeMs(long v) { this.responseTimeMs = v; return this; }
        public Builder parsedBody(Object v) { this.parsedBody = v; return this; }
        public ApiResponse build() { return new ApiResponse(this); }
    }
}
