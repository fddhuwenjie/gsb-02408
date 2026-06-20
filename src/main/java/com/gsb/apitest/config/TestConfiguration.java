package com.gsb.apitest.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class TestConfiguration {
    private String baseUrl;
    private Map<String, String> defaultHeaders;
    private Map<String, Object> globalVariables;
    private int connectTimeoutMs;
    private int readTimeoutMs;
    private String reportDirectory;
    private String templateDirectory;

    private static TestConfiguration instance;

    private TestConfiguration() {
        this.defaultHeaders = new HashMap<>();
        this.globalVariables = new HashMap<>();
        this.connectTimeoutMs = 10000;
        this.readTimeoutMs = 30000;
        this.reportDirectory = "target/test-reports";
        this.templateDirectory = "src/test/resources/templates";
    }

    public static TestConfiguration getInstance() {
        if (instance == null) {
            synchronized (TestConfiguration.class) {
                if (instance == null) {
                    instance = new TestConfiguration();
                }
            }
        }
        return instance;
    }

    public static void reset() {
        instance = null;
    }

    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
    public Map<String, String> getDefaultHeaders() { return defaultHeaders; }
    public void setDefaultHeaders(Map<String, String> defaultHeaders) { this.defaultHeaders = defaultHeaders; }
    public Map<String, Object> getGlobalVariables() { return globalVariables; }
    public void setGlobalVariables(Map<String, Object> globalVariables) { this.globalVariables = globalVariables; }
    public int getConnectTimeoutMs() { return connectTimeoutMs; }
    public void setConnectTimeoutMs(int connectTimeoutMs) { this.connectTimeoutMs = connectTimeoutMs; }
    public int getReadTimeoutMs() { return readTimeoutMs; }
    public void setReadTimeoutMs(int readTimeoutMs) { this.readTimeoutMs = readTimeoutMs; }
    public String getReportDirectory() { return reportDirectory; }
    public void setReportDirectory(String reportDirectory) { this.reportDirectory = reportDirectory; }
    public String getTemplateDirectory() { return templateDirectory; }
    public void setTemplateDirectory(String templateDirectory) { this.templateDirectory = templateDirectory; }

    public void addDefaultHeader(String key, String value) {
        this.defaultHeaders.put(key, value);
    }

    public void addGlobalVariable(String key, Object value) {
        this.globalVariables.put(key, value);
    }

    public void loadFromProperties(Properties props) {
        if (props.containsKey("api.baseUrl")) {
            this.baseUrl = props.getProperty("api.baseUrl");
        }
        if (props.containsKey("api.connectTimeout")) {
            this.connectTimeoutMs = Integer.parseInt(props.getProperty("api.connectTimeout"));
        }
        if (props.containsKey("api.readTimeout")) {
            this.readTimeoutMs = Integer.parseInt(props.getProperty("api.readTimeout"));
        }
        if (props.containsKey("report.directory")) {
            this.reportDirectory = props.getProperty("report.directory");
        }
        if (props.containsKey("template.directory")) {
            this.templateDirectory = props.getProperty("template.directory");
        }
        for (String name : props.stringPropertyNames()) {
            if (name.startsWith("header.")) {
                String headerName = name.substring("header.".length());
                addDefaultHeader(headerName, props.getProperty(name));
            }
            if (name.startsWith("global.")) {
                String varName = name.substring("global.".length());
                addGlobalVariable(varName, props.getProperty(name));
            }
        }
    }

    public String getFullUrl(String path) {
        if (path.startsWith("http://") || path.startsWith("https://")) {
            return path;
        }
        if (baseUrl == null) {
            return path;
        }
        return baseUrl + (path.startsWith("/") ? path : "/" + path);
    }
}
