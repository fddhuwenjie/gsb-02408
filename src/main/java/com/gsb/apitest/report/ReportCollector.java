package com.gsb.apitest.report;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class ReportCollector {

    private static final Logger log = LoggerFactory.getLogger(ReportCollector.class);

    private final List<TestClassRecord> classRecords = new ArrayList<>();
    private TestClassRecord currentClass;
    private TestMethodRecord currentMethod;

    public void startClass(String className) {
        currentClass = new TestClassRecord();
        currentClass.setClassName(className);
        classRecords.add(currentClass);
        log.debug("开始收集测试类报告: {}", className);
    }

    public void endClass() {
        if (currentClass != null) {
            log.debug("结束测试类报告: {}, 方法数: {}", currentClass.getClassName(), currentClass.getMethods().size());
        }
        currentClass = null;
    }

    public void startMethod(String methodName) {
        currentMethod = new TestMethodRecord();
        currentMethod.setMethodName(methodName);
        if (currentClass != null) {
            currentClass.addMethod(currentMethod);
        }
        log.debug("开始收集测试方法报告: {}", methodName);
    }

    public void endMethod(boolean passed, String errorMessage) {
        if (currentMethod != null) {
            currentMethod.setPassed(passed);
            currentMethod.setErrorMessage(errorMessage);
            currentMethod.finish();
        }
        currentMethod = null;
    }

    public void recordStep(StepRecord step) {
        if (currentMethod != null) {
            currentMethod.addStep(step);
        }
    }

    public List<TestClassRecord> getClassRecords() {
        return classRecords;
    }

    public TestClassRecord getCurrentClass() {
        return currentClass;
    }

    public TestMethodRecord getCurrentMethod() {
        return currentMethod;
    }

    public void printConsoleSummary() {
        ConsoleReporter.printSummary(this);
    }

    public String toJson() {
        return JsonReportGenerator.toJson(this);
    }

    public void reset() {
        classRecords.clear();
        currentClass = null;
        currentMethod = null;
    }
}
