package com.gsb.apitest.config;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class TestConfig {

    private String baseUrl = "";
    private long connectTimeoutMs = 10_000;
    private long readTimeoutMs = 30_000;
    private long writeTimeoutMs = 30_000;
    private Map<String, String> defaultHeaders = new HashMap<>();
    private boolean reportEnabled = true;
    private String reportDir = "target/test-reports";
    private boolean logRequest = true;
    private boolean logResponse = true;

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl == null ? "" : baseUrl;
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

    public Map<String, String> getDefaultHeaders() {
        return defaultHeaders;
    }

    public void addDefaultHeader(String key, String value) {
        this.defaultHeaders.put(key, value);
    }

    public void setDefaultHeaders(Map<String, String> headers) {
        this.defaultHeaders = new HashMap<>(headers);
    }

    public boolean isReportEnabled() {
        return reportEnabled;
    }

    public void setReportEnabled(boolean reportEnabled) {
        this.reportEnabled = reportEnabled;
    }

    public String getReportDir() {
        return reportDir;
    }

    public void setReportDir(String reportDir) {
        this.reportDir = reportDir;
    }

    public boolean isLogRequest() {
        return logRequest;
    }

    public void setLogRequest(boolean logRequest) {
        this.logRequest = logRequest;
    }

    public boolean isLogResponse() {
        return logResponse;
    }

    public void setLogResponse(boolean logResponse) {
        this.logResponse = logResponse;
    }

    public String resolveUrl(String path) {
        if (path == null || path.isEmpty()) {
            return baseUrl;
        }
        if (path.startsWith("http://") || path.startsWith("https://")) {
            return path;
        }
        String base = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        String p = path.startsWith("/") ? path : "/" + path;
        return base + p;
    }

    public Map<String, String> mergedHeaders(Map<String, String> additional) {
        Map<String, String> merged = new HashMap<>(defaultHeaders);
        if (additional != null) {
            merged.putAll(additional);
        }
        return Collections.unmodifiableMap(merged);
    }

    public TestConfig copy() {
        TestConfig copy = new TestConfig();
        copy.baseUrl = this.baseUrl;
        copy.connectTimeoutMs = this.connectTimeoutMs;
        copy.readTimeoutMs = this.readTimeoutMs;
        copy.writeTimeoutMs = this.writeTimeoutMs;
        copy.defaultHeaders = new HashMap<>(this.defaultHeaders);
        copy.reportEnabled = this.reportEnabled;
        copy.reportDir = this.reportDir;
        copy.logRequest = this.logRequest;
        copy.logResponse = this.logResponse;
        return copy;
    }
}
