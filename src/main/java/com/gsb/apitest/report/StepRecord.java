package com.gsb.apitest.report;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 单个步骤的执行记录，用于报告输出与失败定位。 */
public class StepRecord {

    public enum Status { PASSED, FAILED, ERROR, SKIPPED }

    private final String stepName;
    private final Instant startedAt;
    private long elapsedMs;
    private Status status = Status.PASSED;

    private String requestMethod;
    private String requestUrl;
    private Map<String, String> requestHeaders = new LinkedHashMap<>();
    private String requestBody;

    private Integer responseStatus;
    private String responseBody;

    private Map<String, Object> extractedVars = new LinkedHashMap<>();
    private List<String> assertionFailures;
    private String errorMessage;

    public StepRecord(String stepName) {
        this.stepName = stepName;
        this.startedAt = Instant.now();
    }

    public String getStepName() { return stepName; }
    public Instant getStartedAt() { return startedAt; }
    public long getElapsedMs() { return elapsedMs; }
    public void setElapsedMs(long elapsedMs) { this.elapsedMs = elapsedMs; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public String getRequestMethod() { return requestMethod; }
    public void setRequestMethod(String requestMethod) { this.requestMethod = requestMethod; }

    public String getRequestUrl() { return requestUrl; }
    public void setRequestUrl(String requestUrl) { this.requestUrl = requestUrl; }

    public Map<String, String> getRequestHeaders() { return requestHeaders; }
    public void setRequestHeaders(Map<String, String> headers) { this.requestHeaders = headers; }

    public String getRequestBody() { return requestBody; }
    public void setRequestBody(String requestBody) { this.requestBody = requestBody; }

    public Integer getResponseStatus() { return responseStatus; }
    public void setResponseStatus(Integer responseStatus) { this.responseStatus = responseStatus; }

    public String getResponseBody() { return responseBody; }
    public void setResponseBody(String responseBody) { this.responseBody = responseBody; }

    public Map<String, Object> getExtractedVars() { return extractedVars; }
    public void setExtractedVars(Map<String, Object> vars) { this.extractedVars = vars; }

    public List<String> getAssertionFailures() { return assertionFailures; }
    public void setAssertionFailures(List<String> failures) { this.assertionFailures = failures; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
}
