package com.gsb.apitest.context;

import com.gsb.apitest.config.TestConfig;
import com.gsb.apitest.model.StepResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExecutionContext {
    private static final Logger log = LoggerFactory.getLogger(ExecutionContext.class);
    private static final ThreadLocal<ExecutionContext> CURRENT = new ThreadLocal<>();

    private final TestConfig config;
    private final ScopedVariableStore variables;
    private final List<StepResult> stepResults = new ArrayList<>();
    private String currentTestName;
    private String currentStepName;
    private int currentStepIndex = 0;
    private final Map<String, Object> testArtifacts = new HashMap<>();
    private Throwable failureCause;
    private String failureStepName;

    public ExecutionContext(TestConfig config) {
        this.config = config;
        this.variables = new ScopedVariableStore();
        if (config != null && config.getGlobalVariables() != null) {
            config.getGlobalVariables().forEach(this.variables::setGlobalVariable);
        }
    }

    public static ExecutionContext getCurrent() {
        return CURRENT.get();
    }

    public static void setCurrent(ExecutionContext context) {
        CURRENT.set(context);
    }

    public static void clearCurrent() {
        CURRENT.remove();
    }

    public void startTest(String testName) {
        this.currentTestName = testName;
        this.currentStepIndex = 0;
        this.stepResults.clear();
        this.variables.pushTestScope();
        log.info("Starting test: {}", testName);
    }

    public void endTest() {
        this.variables.popTestScope();
        log.info("Ending test: {}, steps executed: {}", currentTestName, stepResults.size());
    }

    public void startStep(String stepName) {
        this.currentStepName = stepName;
        this.currentStepIndex++;
        this.variables.pushStepScope();
        this.variables.setStepVariable("_stepIndex", currentStepIndex);
        this.variables.setStepVariable("_stepName", stepName);
        log.info("Starting step {}: {}", currentStepIndex, stepName);
    }

    public void setCurrentStepForBlock(String stepName) {
        this.currentStepName = stepName;
        this.currentStepIndex++;
        this.variables.setStepVariable("_stepIndex", currentStepIndex);
        this.variables.setStepVariable("_stepName", stepName);
        log.info("Starting step block {}: {}", currentStepIndex, stepName);
    }

    public StepResult endStepSuccess(int statusCode, String requestBody, String responseBody, Map<String, String> requestHeaders, Map<String, String> responseHeaders) {
        StepResult result = StepResult.success(currentStepName, currentStepIndex, statusCode, requestBody, responseBody, requestHeaders, responseHeaders);
        stepResults.add(result);
        this.variables.popStepScope();
        log.info("Step {} completed successfully, status: {}", currentStepName, statusCode);
        return result;
    }

    public StepResult endStepFailure(Throwable error, String requestBody) {
        StepResult result = StepResult.failure(currentStepName, currentStepIndex, error, requestBody);
        stepResults.add(result);
        this.failureCause = error;
        this.failureStepName = currentStepName;
        this.variables.popStepScope();
        log.error("Step {} failed: {}", currentStepName, error.getMessage());
        return result;
    }

    public TestConfig getConfig() {
        return config;
    }

    public ScopedVariableStore getVariables() {
        return variables;
    }

    public void setVariable(String key, Object value) {
        variables.setTestVariable(key, value);
    }

    public void setVariable(VariableScope scope, String key, Object value) {
        variables.setVariable(scope, key, value);
    }

    public Object getVariable(String key) {
        return variables.getVariable(key);
    }

    public String getVariableAsString(String key) {
        return variables.getVariableAsString(key);
    }

    public <T> T getVariable(String key, Class<T> type) {
        return variables.getVariable(key, type);
    }

    public List<StepResult> getStepResults() {
        return stepResults;
    }

    public void addStepResult(StepResult result) {
        for (int i = 0; i < stepResults.size(); i++) {
            StepResult existing = stepResults.get(i);
            if (existing.getStepIndex() == result.getStepIndex() && existing.getStepName().equals(result.getStepName())) {
                stepResults.set(i, result);
                return;
            }
        }
        stepResults.add(result);
    }

    public String getCurrentTestName() {
        return currentTestName;
    }

    public String getCurrentStepName() {
        return currentStepName;
    }

    public int getCurrentStepIndex() {
        return currentStepIndex;
    }

    public Map<String, Object> getTestArtifacts() {
        return testArtifacts;
    }

    public void addArtifact(String name, Object artifact) {
        testArtifacts.put(name, artifact);
    }

    public Throwable getFailureCause() {
        return failureCause;
    }

    public String getFailureStepName() {
        return failureStepName;
    }

    public boolean isFailed() {
        return failureCause != null;
    }
}
