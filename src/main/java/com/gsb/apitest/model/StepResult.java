package com.gsb.apitest.model;

import java.time.LocalDateTime;
import java.util.Map;

public class StepResult {
    private final String stepName;
    private final int stepIndex;
    private final boolean success;
    private final int statusCode;
    private final String requestBody;
    private final String responseBody;
    private final Map<String, String> requestHeaders;
    private final Map<String, String> responseHeaders;
    private final Throwable error;
    private final LocalDateTime timestamp;

    private StepResult(String stepName, int stepIndex, boolean success, int statusCode,
                       String requestBody, String responseBody,
                       Map<String, String> requestHeaders, Map<String, String> responseHeaders,
                       Throwable error) {
        this.stepName = stepName;
        this.stepIndex = stepIndex;
        this.success = success;
        this.statusCode = statusCode;
        this.requestBody = requestBody;
        this.responseBody = responseBody;
        this.requestHeaders = requestHeaders;
        this.responseHeaders = responseHeaders;
        this.error = error;
        this.timestamp = LocalDateTime.now();
    }

    public static StepResult success(String stepName, int stepIndex, int statusCode,
                                     String requestBody, String responseBody,
                                     Map<String, String> requestHeaders, Map<String, String> responseHeaders) {
        return new StepResult(stepName, stepIndex, true, statusCode, requestBody, responseBody,
                requestHeaders, responseHeaders, null);
    }

    public static StepResult failure(String stepName, int stepIndex, Throwable error, String requestBody) {
        return new StepResult(stepName, stepIndex, false, -1, requestBody, null,
                null, null, error);
    }

    public String getStepName() {
        return stepName;
    }

    public int getStepIndex() {
        return stepIndex;
    }

    public boolean isSuccess() {
        return success;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getRequestBody() {
        return requestBody;
    }

    public String getResponseBody() {
        return responseBody;
    }

    public Map<String, String> getRequestHeaders() {
        return requestHeaders;
    }

    public Map<String, String> getResponseHeaders() {
        return responseHeaders;
    }

    public Throwable getError() {
        return error;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}
