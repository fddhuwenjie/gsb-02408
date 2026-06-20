package com.gsb.apitest.context;

import com.gsb.apitest.config.TestConfig;
import com.gsb.apitest.http.ApiHttpClient;
import com.gsb.apitest.http.HttpResponse;
import com.gsb.apitest.report.ReportCollector;
import com.gsb.apitest.step.StepExecutor;
import com.gsb.apitest.template.TemplateEngine;

public class TestContext {

    private final TestConfig config;
    private final TemplateEngine templateEngine;
    private final ApiHttpClient httpClient;
    private final ReportCollector reportCollector;
    private StepExecutor stepExecutor;
    private final VariableScope globalScope;
    private VariableScope classScope;
    private VariableScope stepScope;
    private HttpResponse lastResponse;
    private String currentStepName;
    private int stepIndex;
    private String currentTestClass;
    private String currentTestMethod;

    public TestContext(TestConfig config) {
        this.config = config;
        this.templateEngine = new TemplateEngine();
        this.httpClient = new ApiHttpClient(config);
        this.reportCollector = new ReportCollector();
        this.stepExecutor = new StepExecutor(templateEngine, httpClient, reportCollector);
        this.globalScope = new VariableScope("global");
    }

    public TestConfig getConfig() {
        return config;
    }

    public TemplateEngine getTemplateEngine() {
        return templateEngine;
    }

    public ApiHttpClient getHttpClient() {
        return httpClient;
    }

    public ReportCollector getReportCollector() {
        return reportCollector;
    }

    public StepExecutor getStepExecutor() {
        return stepExecutor;
    }

    public VariableScope global() {
        return globalScope;
    }

    public VariableScope clazz() {
        return classScope;
    }

    public VariableScope step() {
        return stepScope;
    }

    public void enterClass(String className) {
        this.currentTestClass = className;
        this.classScope = globalScope.createChild("class:" + className);
        this.reportCollector.startClass(className);
    }

    public void exitClass() {
        this.classScope = null;
        this.currentTestClass = null;
    }

    public void enterMethod(String methodName) {
        this.currentTestMethod = methodName;
        this.reportCollector.startMethod(methodName);
    }

    public void exitMethod() {
        this.currentTestMethod = null;
    }

    public void enterStep(String stepName, int index) {
        this.currentStepName = stepName;
        this.stepIndex = index;
        if (classScope != null) {
            this.stepScope = classScope.createChild("step:" + stepName);
        } else {
            this.stepScope = globalScope.createChild("step:" + stepName);
        }
    }

    public void exitStep() {
        this.stepScope = null;
        this.currentStepName = null;
    }

    public Object resolveVariable(String key) {
        if (stepScope != null && stepScope.has(key)) {
            return stepScope.get(key);
        }
        if (classScope != null && classScope.has(key)) {
            return classScope.get(key);
        }
        return globalScope.get(key);
    }

    public String resolveVariableAsString(String key) {
        Object value = resolveVariable(key);
        if (value == null) {
            return null;
        }
        if (value instanceof String) {
            return (String) value;
        }
        return String.valueOf(value);
    }

    public void setGlobalVariable(String key, Object value) {
        globalScope.set(key, value);
    }

    public void setClassVariable(String key, Object value) {
        if (classScope == null) {
            throw new IllegalStateException("未进入类作用域，无法设置类变量");
        }
        classScope.set(key, value);
    }

    public void setStepVariable(String key, Object value) {
        if (stepScope == null) {
            throw new IllegalStateException("未进入步骤作用域，无法设置步骤变量");
        }
        stepScope.set(key, value);
    }

    public HttpResponse getLastResponse() {
        return lastResponse;
    }

    public void setLastResponse(HttpResponse lastResponse) {
        this.lastResponse = lastResponse;
    }

    public String getCurrentStepName() {
        return currentStepName;
    }

    public int getStepIndex() {
        return stepIndex;
    }

    public String getCurrentTestClass() {
        return currentTestClass;
    }

    public String getCurrentTestMethod() {
        return currentTestMethod;
    }
}
