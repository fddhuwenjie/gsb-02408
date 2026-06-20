package com.gsb.apitest.http;

import java.util.LinkedHashMap;
import java.util.Map;

public class HttpRequest {

    private HttpMethod method = HttpMethod.POST;
    private String url;
    private Map<String, String> headers = new LinkedHashMap<>();
    private Map<String, String> queryParams = new LinkedHashMap<>();
    private String body;
    private long connectTimeoutMs;
    private long readTimeoutMs;
    private long writeTimeoutMs;

    public HttpMethod getMethod() {
        return method;
    }

    public void setMethod(HttpMethod method) {
        this.method = method;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public void addHeader(String key, String value) {
        this.headers.put(key, value);
    }

    public Map<String, String> getQueryParams() {
        return queryParams;
    }

    public void setQueryParams(Map<String, String> queryParams) {
        this.queryParams = queryParams;
    }

    public void addQueryParam(String key, String value) {
        this.queryParams.put(key, value);
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public long getConnectTimeoutMs() {
        return connectTimeoutMs;
    }

    public void setConnectTimeoutMs(long connectTimeoutMs) {
        this.connectTimeoutMs = connectTimeoutMs;
    }

    public long getReadTimeoutMs() {
        return readTimeoutMs;
    }

    public void setReadTimeoutMs(long readTimeoutMs) {
        this.readTimeoutMs = readTimeoutMs;
    }

    public long getWriteTimeoutMs() {
        return writeTimeoutMs;
    }

    public void setWriteTimeoutMs(long writeTimeoutMs) {
        this.writeTimeoutMs = writeTimeoutMs;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final HttpRequest request = new HttpRequest();

        public Builder method(HttpMethod method) {
            request.method = method;
            return this;
        }

        public Builder post() {
            request.method = HttpMethod.POST;
            return this;
        }

        public Builder get() {
            request.method = HttpMethod.GET;
            return this;
        }

        public Builder url(String url) {
            request.url = url;
            return this;
        }

        public Builder header(String key, String value) {
            request.headers.put(key, value);
            return this;
        }

        public Builder headers(Map<String, String> headers) {
            request.headers.putAll(headers);
            return this;
        }

        public Builder queryParam(String key, String value) {
            request.queryParams.put(key, value);
            return this;
        }

        public Builder body(String body) {
            request.body = body;
            return this;
        }

        public Builder jsonBody(String json) {
            request.body = json;
            request.headers.put("Content-Type", "application/json;charset=UTF-8");
            return this;
        }

        public Builder connectTimeout(long ms) {
            request.connectTimeoutMs = ms;
            return this;
        }

        public Builder readTimeout(long ms) {
            request.readTimeoutMs = ms;
            return this;
        }

        public Builder writeTimeout(long ms) {
            request.writeTimeoutMs = ms;
            return this;
        }

        public HttpRequest build() {
            return request;
        }
    }
}
