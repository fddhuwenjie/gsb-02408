package com.gsb.apitest.report;

import java.util.LinkedHashMap;
import java.util.Map;

public class StepRecord {

    private String name;
    private String method;
    private String url;
    private int statusCode;
    private long responseTimeMs;
    private long totalTimeMs;
    private boolean success;
    private String errorMessage;
    private String requestBody;
    private String responseBody;
    private int assertionTotal;
    private int assertionPassed;
    private int assertionFailed;
    private Map<String, String> extractions = new LinkedHashMap<>();

    public StepRecord() {
    }

    public StepRecord(String name, String method, String url) {
        this.name = name;
        this.method = method;
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public long getResponseTimeMs() {
        return responseTimeMs;
    }

    public void setResponseTimeMs(long responseTimeMs) {
        this.responseTimeMs = responseTimeMs;
    }

    public long getTotalTimeMs() {
        return totalTimeMs;
    }

    public void setTotalTimeMs(long totalTimeMs) {
        this.totalTimeMs = totalTimeMs;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getRequestBody() {
        return requestBody;
    }

    public void setRequestBody(String requestBody) {
        this.requestBody = requestBody;
    }

    public String getResponseBody() {
        return responseBody;
    }

    public void setResponseBody(String responseBody) {
        this.responseBody = responseBody;
    }

    public int getAssertionTotal() {
        return assertionTotal;
    }

    public void setAssertionTotal(int assertionTotal) {
        this.assertionTotal = assertionTotal;
    }

    public int getAssertionPassed() {
        return assertionPassed;
    }

    public void setAssertionPassed(int assertionPassed) {
        this.assertionPassed = assertionPassed;
    }

    public int getAssertionFailed() {
        return assertionFailed;
    }

    public void setAssertionFailed(int assertionFailed) {
        this.assertionFailed = assertionFailed;
    }

    public Map<String, String> getExtractions() {
        return extractions;
    }

    public void setExtractions(Map<String, String> extractions) {
        this.extractions = extractions;
    }
}
