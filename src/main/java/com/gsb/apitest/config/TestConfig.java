package com.gsb.apitest.config;

import java.util.HashMap;
import java.util.Map;

public class TestConfig {
    private String baseUrl;
    private Map<String, String> globalHeaders = new HashMap<>();
    private int connectTimeout = 10000;
    private int readTimeout = 30000;
    private Map<String, Object> globalVariables = new HashMap<>();

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public Map<String, String> getGlobalHeaders() {
        return globalHeaders;
    }

    public void setGlobalHeaders(Map<String, String> globalHeaders) {
        this.globalHeaders = globalHeaders;
    }

    public void addGlobalHeader(String key, String value) {
        this.globalHeaders.put(key, value);
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }

    public Map<String, Object> getGlobalVariables() {
        return globalVariables;
    }

    public void setGlobalVariables(Map<String, Object> globalVariables) {
        this.globalVariables = globalVariables;
    }

    public void setGlobalVariable(String key, Object value) {
        this.globalVariables.put(key, value);
    }
}
