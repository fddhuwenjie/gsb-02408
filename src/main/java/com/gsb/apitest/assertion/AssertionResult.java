package com.gsb.apitest.assertion;

public class AssertionResult {

    private final String stepName;
    private final String type;
    private final boolean passed;
    private final String message;
    private final Object expected;
    private final Object actual;

    public AssertionResult(String stepName, String type, boolean passed, String message, Object expected, Object actual) {
        this.stepName = stepName;
        this.type = type;
        this.passed = passed;
        this.message = message;
        this.expected = expected;
        this.actual = actual;
    }

    public static AssertionResult pass(String stepName, String type) {
        return new AssertionResult(stepName, type, true, null, null, null);
    }

    public static AssertionResult fail(String stepName, String type, String message, Object expected, Object actual) {
        return new AssertionResult(stepName, type, false, message, expected, actual);
    }

    public String getStepName() {
        return stepName;
    }

    public String getType() {
        return type;
    }

    public boolean isPassed() {
        return passed;
    }

    public String getMessage() {
        return message;
    }

    public Object getExpected() {
        return expected;
    }

    public Object getActual() {
        return actual;
    }
}
