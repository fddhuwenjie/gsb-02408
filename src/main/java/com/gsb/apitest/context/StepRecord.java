package com.gsb.apitest.context;

import com.gsb.apitest.model.ApiResponse;

import java.util.Map;

public class StepRecord {
    private int stepIndex;
    private String stepName;
    private String method;
    private String url;
    private Map<String, String> headers;
    private String requestBody;
    private ApiResponse response;
    private long durationMs;
    private boolean success;
    private String errorMessage;
    private Map<String, Object> extractedVariables;

    public StepRecord() {}

    private StepRecord(Builder b) {
        this.stepIndex = b.stepIndex;
        this.stepName = b.stepName;
        this.method = b.method;
        this.url = b.url;
        this.headers = b.headers;
        this.requestBody = b.requestBody;
        this.response = b.response;
        this.durationMs = b.durationMs;
        this.success = b.success;
        this.errorMessage = b.errorMessage;
        this.extractedVariables = b.extractedVariables;
    }

    public static Builder builder() { return new Builder(); }

    public int getStepIndex() { return stepIndex; }
    public void setStepIndex(int stepIndex) { this.stepIndex = stepIndex; }
    public String getStepName() { return stepName; }
    public void setStepName(String stepName) { this.stepName = stepName; }
    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public Map<String, String> getHeaders() { return headers; }
    public void setHeaders(Map<String, String> headers) { this.headers = headers; }
    public String getRequestBody() { return requestBody; }
    public void setRequestBody(String requestBody) { this.requestBody = requestBody; }
    public ApiResponse getResponse() { return response; }
    public void setResponse(ApiResponse response) { this.response = response; }
    public long getDurationMs() { return durationMs; }
    public void setDurationMs(long durationMs) { this.durationMs = durationMs; }
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    public Map<String, Object> getExtractedVariables() { return extractedVariables; }
    public void setExtractedVariables(Map<String, Object> extractedVariables) { this.extractedVariables = extractedVariables; }

    public static class Builder {
        private int stepIndex;
        private String stepName;
        private String method;
        private String url;
        private Map<String, String> headers;
        private String requestBody;
        private ApiResponse response;
        private long durationMs;
        private boolean success;
        private String errorMessage;
        private Map<String, Object> extractedVariables;

        public Builder stepIndex(int v) { this.stepIndex = v; return this; }
        public Builder stepName(String v) { this.stepName = v; return this; }
        public Builder method(String v) { this.method = v; return this; }
        public Builder url(String v) { this.url = v; return this; }
        public Builder headers(Map<String, String> v) { this.headers = v; return this; }
        public Builder requestBody(String v) { this.requestBody = v; return this; }
        public Builder response(ApiResponse v) { this.response = v; return this; }
        public Builder durationMs(long v) { this.durationMs = v; return this; }
        public Builder success(boolean v) { this.success = v; return this; }
        public Builder errorMessage(String v) { this.errorMessage = v; return this; }
        public Builder extractedVariables(Map<String, Object> v) { this.extractedVariables = v; return this; }
        public StepRecord build() { return new StepRecord(this); }
    }
}
