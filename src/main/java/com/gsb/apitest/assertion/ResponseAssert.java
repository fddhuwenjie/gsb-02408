package com.gsb.apitest.assertion;

import com.gsb.apitest.extract.FieldExtractor;
import com.gsb.apitest.http.HttpResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 链式断言器：业务侧写法类似
 * <pre>
 *   ResponseAssert.of(resp)
 *       .statusCode(200)
 *       .jsonPathEquals("$.json.userId", "u-100")
 *       .jsonPathExists("$.headers.Host");
 * </pre>
 *
 * 内部不立即抛 AssertionError，而是先收集所有失败原因，最后一次性抛出，
 * 便于报告里看到“一个步骤里多个断言都挂了”的全貌。
 */
public class ResponseAssert {

    private final HttpResponse response;
    private final List<String> failures = new ArrayList<>();

    private ResponseAssert(HttpResponse response) {
        this.response = response;
    }

    public static ResponseAssert of(HttpResponse response) {
        return new ResponseAssert(response);
    }

    public ResponseAssert statusCode(int expected) {
        if (response.getStatusCode() != expected) {
            failures.add("expected statusCode=" + expected
                    + " but got " + response.getStatusCode());
        }
        return this;
    }

    public ResponseAssert success() {
        if (!response.isSuccess()) {
            failures.add("expected 2xx but got " + response.getStatusCode());
        }
        return this;
    }

    public ResponseAssert jsonPathEquals(String path, Object expected) {
        Object actual = FieldExtractor.extract(response.getBodyJson(), path);
        if (!equalsLoose(expected, actual)) {
            failures.add("jsonPath " + path + " expected=" + expected
                    + " but actual=" + actual);
        }
        return this;
    }

    public ResponseAssert jsonPathExists(String path) {
        Object actual = FieldExtractor.extract(response.getBodyJson(), path);
        if (actual == null) {
            failures.add("jsonPath " + path + " expected to exist but was null");
        }
        return this;
    }

    public ResponseAssert bodyContains(String fragment) {
        if (response.getBody() == null || !response.getBody().contains(fragment)) {
            failures.add("body expected to contain: " + fragment);
        }
        return this;
    }

    public ResponseAssert custom(String description, java.util.function.Predicate<HttpResponse> predicate) {
        if (!predicate.test(response)) {
            failures.add("custom assertion failed: " + description);
        }
        return this;
    }

    public List<String> failures() {
        return new ArrayList<>(failures);
    }

    /** 抛出所有累积的断言失败。如果没有失败则什么都不做。 */
    public void verify() {
        if (failures.isEmpty()) return;
        StringBuilder sb = new StringBuilder("Assertion failures:\n");
        for (String f : failures) {
            sb.append(" - ").append(f).append('\n');
        }
        throw new AssertionError(sb.toString());
    }

    private static boolean equalsLoose(Object expected, Object actual) {
        if (Objects.equals(expected, actual)) return true;
        if (expected == null || actual == null) return false;
        // 数字 / 字符串宽松比较
        return String.valueOf(expected).equals(String.valueOf(actual));
    }
}
