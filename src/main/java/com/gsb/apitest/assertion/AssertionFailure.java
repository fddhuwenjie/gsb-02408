package com.gsb.apitest.assertion;

public class AssertionFailure extends AssertionError {

    private final String stepName;
    private final String assertionType;
    private final Object expected;
    private final Object actual;

    public AssertionFailure(String stepName, String assertionType, Object expected, Object actual, String message) {
        super(buildMessage(stepName, assertionType, expected, actual, message));
        this.stepName = stepName;
        this.assertionType = assertionType;
        this.expected = expected;
        this.actual = actual;
    }

    private static String buildMessage(String stepName, String assertionType, Object expected, Object actual, String message) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n[步骤: ").append(stepName != null ? stepName : "<unknown>").append("] ");
        sb.append("断言失败 [").append(assertionType).append("]");
        if (message != null && !message.isEmpty()) {
            sb.append(": ").append(message);
        }
        if (expected != null) {
            sb.append("\n  期望: ").append(expected);
        }
        if (actual != null) {
            sb.append("\n  实际: ").append(truncate(String.valueOf(actual), 500));
        }
        return sb.toString();
    }

    private static String truncate(String s, int max) {
        if (s == null) return null;
        return s.length() <= max ? s : s.substring(0, max) + "...";
    }

    public String getStepName() {
        return stepName;
    }

    public String getAssertionType() {
        return assertionType;
    }

    public Object getExpected() {
        return expected;
    }

    public Object getActual() {
        return actual;
    }
}
