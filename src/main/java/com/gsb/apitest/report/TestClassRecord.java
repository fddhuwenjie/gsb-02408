package com.gsb.apitest.report;

import java.util.ArrayList;
import java.util.List;

public class TestClassRecord {

    private String className;
    private final List<TestMethodRecord> methods = new ArrayList<>();
    private long startTime;

    public TestClassRecord() {
        this.startTime = System.currentTimeMillis();
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public List<TestMethodRecord> getMethods() {
        return methods;
    }

    public void addMethod(TestMethodRecord method) {
        this.methods.add(method);
    }

    public long getStartTime() {
        return startTime;
    }

    public int getTotalSteps() {
        return methods.stream().mapToInt(m -> m.getSteps().size()).sum();
    }

    public int getPassedMethods() {
        return (int) methods.stream().filter(TestMethodRecord::isPassed).count();
    }

    public int getFailedMethods() {
        return (int) methods.stream().filter(m -> !m.isPassed()).count();
    }
}
