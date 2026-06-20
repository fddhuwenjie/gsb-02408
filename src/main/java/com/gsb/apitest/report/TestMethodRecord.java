package com.gsb.apitest.report;

import java.util.ArrayList;
import java.util.List;

public class TestMethodRecord {

    private String methodName;
    private boolean passed;
    private long durationMs;
    private String errorMessage;
    private final List<StepRecord> steps = new ArrayList<>();
    private long startTime;

    public TestMethodRecord() {
        this.startTime = System.currentTimeMillis();
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public boolean isPassed() {
        return passed;
    }

    public void setPassed(boolean passed) {
        this.passed = passed;
    }

    public long getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(long durationMs) {
        this.durationMs = durationMs;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public List<StepRecord> getSteps() {
        return steps;
    }

    public void addStep(StepRecord step) {
        this.steps.add(step);
    }

    public long getStartTime() {
        return startTime;
    }

    public void finish() {
        this.durationMs = System.currentTimeMillis() - startTime;
    }
}
