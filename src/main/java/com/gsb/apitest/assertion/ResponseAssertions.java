package com.gsb.apitest.assertion;

import com.gsb.apitest.http.HttpResponse;
import com.gsb.apitest.util.JsonUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class ResponseAssertions {

    private final HttpResponse response;
    private final String stepName;
    private final List<AssertionResult> results = new ArrayList<>();
    private boolean softAssert = false;

    public ResponseAssertions(HttpResponse response, String stepName) {
        this.response = response;
        this.stepName = stepName;
    }

    public ResponseAssertions soft() {
        this.softAssert = true;
        return this;
    }

    public List<AssertionResult> getResults() {
        return results;
    }

    private void addResult(AssertionResult result) {
        results.add(result);
        if (!result.isPassed() && !softAssert) {
            throw new AssertionFailure(
                    result.getStepName(),
                    result.getType(),
                    result.getExpected(),
                    result.getActual(),
                    result.getMessage()
            );
        }
    }

    public ResponseAssertions statusCode(int expected) {
        int actual = response.getStatusCode();
        boolean passed = actual == expected;
        if (passed) {
            addResult(AssertionResult.pass(stepName, "statusCode"));
        } else {
            addResult(AssertionResult.fail(stepName, "statusCode",
                    "状态码不匹配", expected, actual));
        }
        return this;
    }

    public ResponseAssertions is2xx() {
        boolean passed = response.is2xxSuccessful();
        if (passed) {
            addResult(AssertionResult.pass(stepName, "is2xx"));
        } else {
            addResult(AssertionResult.fail(stepName, "is2xx",
                    "期望2xx响应", "2xx", response.getStatusCode()));
        }
        return this;
    }

    public ResponseAssertions statusCodeIn(int... codes) {
        int actual = response.getStatusCode();
        boolean passed = false;
        List<Integer> expectedList = new ArrayList<>();
        for (int c : codes) {
            expectedList.add(c);
            if (actual == c) passed = true;
        }
        if (passed) {
            addResult(AssertionResult.pass(stepName, "statusCodeIn"));
        } else {
            addResult(AssertionResult.fail(stepName, "statusCodeIn",
                    "状态码不在期望列表中", expectedList, actual));
        }
        return this;
    }

    public ResponseAssertions jsonPath(String jsonPath, Object expectedValue) {
        Object actual = response.jsonPath(jsonPath);
        boolean passed = equalsValue(actual, expectedValue);
        if (passed) {
            addResult(AssertionResult.pass(stepName, "jsonPath:" + jsonPath));
        } else {
            addResult(AssertionResult.fail(stepName, "jsonPath:" + jsonPath,
                    "JSON路径值不匹配", expectedValue, actual));
        }
        return this;
    }

    public ResponseAssertions jsonPathNotNull(String jsonPath) {
        Object actual = response.jsonPath(jsonPath);
        boolean passed = actual != null;
        if (passed) {
            addResult(AssertionResult.pass(stepName, "jsonPathNotNull:" + jsonPath));
        } else {
            addResult(AssertionResult.fail(stepName, "jsonPathNotNull:" + jsonPath,
                    "JSON路径值为空", "not null", null));
        }
        return this;
    }

    public ResponseAssertions jsonPathContains(String jsonPath, String expectedSubstring) {
        Object actual = response.jsonPath(jsonPath);
        String actualStr = actual == null ? "" : String.valueOf(actual);
        boolean passed = actualStr.contains(expectedSubstring);
        if (passed) {
            addResult(AssertionResult.pass(stepName, "jsonPathContains:" + jsonPath));
        } else {
            addResult(AssertionResult.fail(stepName, "jsonPathContains:" + jsonPath,
                    "JSON路径值不包含期望子串", expectedSubstring, actualStr));
        }
        return this;
    }

    public ResponseAssertions bodyContains(String expectedSubstring) {
        String body = response.getBody() == null ? "" : response.getBody();
        boolean passed = body.contains(expectedSubstring);
        if (passed) {
            addResult(AssertionResult.pass(stepName, "bodyContains"));
        } else {
            addResult(AssertionResult.fail(stepName, "bodyContains",
                    "响应体不包含期望内容", expectedSubstring, truncate(body, 200)));
        }
        return this;
    }

    public ResponseAssertions bodyMatches(String regex) {
        String body = response.getBody() == null ? "" : response.getBody();
        boolean passed = Pattern.compile(regex, Pattern.DOTALL).matcher(body).find();
        if (passed) {
            addResult(AssertionResult.pass(stepName, "bodyMatches"));
        } else {
            addResult(AssertionResult.fail(stepName, "bodyMatches",
                    "响应体不匹配正则", regex, truncate(body, 200)));
        }
        return this;
    }

    public ResponseAssertions header(String headerName, String expectedValue) {
        String actual = response.getHeader(headerName);
        boolean passed = expectedValue == null ? actual == null : expectedValue.equals(actual);
        if (passed) {
            addResult(AssertionResult.pass(stepName, "header:" + headerName));
        } else {
            addResult(AssertionResult.fail(stepName, "header:" + headerName,
                    "响应头不匹配", expectedValue, actual));
        }
        return this;
    }

    public ResponseAssertions headerExists(String headerName) {
        String actual = response.getHeader(headerName);
        boolean passed = actual != null;
        if (passed) {
            addResult(AssertionResult.pass(stepName, "headerExists:" + headerName));
        } else {
            addResult(AssertionResult.fail(stepName, "headerExists:" + headerName,
                    "响应头不存在", headerName, null));
        }
        return this;
    }

    public ResponseAssertions responseTimeLessThan(long maxMs) {
        long actual = response.getResponseTimeMs();
        boolean passed = actual < maxMs;
        if (passed) {
            addResult(AssertionResult.pass(stepName, "responseTimeLessThan"));
        } else {
            addResult(AssertionResult.fail(stepName, "responseTimeLessThan",
                    "响应时间超限", "<" + maxMs + "ms", actual + "ms"));
        }
        return this;
    }

    public int totalCount() {
        return results.size();
    }

    public int passCount() {
        return (int) results.stream().filter(AssertionResult::isPassed).count();
    }

    public int failCount() {
        return (int) results.stream().filter(r -> !r.isPassed()).count();
    }

    public void assertAll() {
        List<AssertionResult> failures = new ArrayList<>();
        for (AssertionResult r : results) {
            if (!r.isPassed()) failures.add(r);
        }
        if (!failures.isEmpty()) {
            StringBuilder sb = new StringBuilder("\n[步骤: ").append(stepName).append("] 存在").append(failures.size()).append("个断言失败:");
            for (AssertionResult f : failures) {
                sb.append("\n  - [").append(f.getType()).append("] ");
                if (f.getMessage() != null) sb.append(f.getMessage());
                sb.append(" | 期望: ").append(f.getExpected()).append(" | 实际: ").append(truncate(String.valueOf(f.getActual()), 200));
            }
            throw new AssertionError(sb.toString());
        }
    }

    private boolean equalsValue(Object actual, Object expected) {
        if (expected == null) return actual == null;
        if (actual == null) return false;
        if (expected instanceof Number && actual instanceof Number) {
            return ((Number) expected).doubleValue() == ((Number) actual).doubleValue();
        }
        String expectedStr = String.valueOf(expected);
        String actualStr = String.valueOf(actual);
        if (isNumeric(expectedStr) && isNumeric(actualStr)) {
            try {
                return Double.parseDouble(expectedStr) == Double.parseDouble(actualStr);
            } catch (NumberFormatException ignored) {}
        }
        return expectedStr.equals(actualStr);
    }

    private boolean isNumeric(String s) {
        if (s == null) return false;
        return s.matches("-?\\d+(\\.\\d+)?");
    }

    private String truncate(String s, int max) {
        if (s == null) return null;
        return s.length() <= max ? s : s.substring(0, max) + "...";
    }
}
