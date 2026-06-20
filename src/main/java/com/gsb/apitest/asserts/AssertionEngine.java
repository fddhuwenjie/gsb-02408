package com.gsb.apitest.asserts;

import com.gsb.apitest.context.ExecutionContext;
import com.gsb.apitest.context.VariableScope;
import com.gsb.apitest.extract.JsonPathExtractor;
import com.gsb.apitest.model.ApiResponse;
import org.opentest4j.AssertionFailedError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.function.Consumer;

public class AssertionEngine {
    private static final Logger log = LoggerFactory.getLogger(AssertionEngine.class);
    private final ApiResponse response;
    private final JsonPathExtractor extractor;
    private final ExecutionContext context;
    private String currentPath;

    public AssertionEngine(ApiResponse response) {
        this.response = response;
        this.extractor = new JsonPathExtractor(response.getBody() != null ? response.getBody() : "{}");
        this.context = ExecutionContext.getCurrent();
    }

    public AssertionEngine statusCodeIs(int expected) {
        int actual = response.getStatusCode();
        if (actual != expected) {
            throw new AssertionFailedError(
                    "Status code mismatch: expected <" + expected + "> but was <" + actual + ">",
                    expected, actual
            );
        }
        log.debug("Status code assertion passed: {}", actual);
        return this;
    }

    public AssertionEngine statusCodeIs2xx() {
        int code = response.getStatusCode();
        if (code < 200 || code >= 300) {
            throw new AssertionFailedError(
                    "Expected 2xx status code but was <" + code + ">", "2xx", code
            );
        }
        return this;
    }

    public AssertionEngine statusCodeIsSuccessful() {
        return statusCodeIs2xx();
    }

    public AssertionEngine bodyField(String jsonPath) {
        this.currentPath = jsonPath;
        return this;
    }

    public AssertionEngine isEqualTo(Object expected) {
        Object actual = extractor.read(currentPath);
        if (!Objects.equals(actual, expected)) {
            String expectedStr = expected != null ? expected.toString() : "null";
            String actualStr = actual != null ? actual.toString() : "null";
            throw new AssertionFailedError(
                    "Field '" + currentPath + "' mismatch: expected <" + expectedStr + "> but was <" + actualStr + ">",
                    expectedStr, actualStr
            );
        }
        log.debug("Assertion passed: '{}' == {}", currentPath, expected);
        return this;
    }

    public AssertionEngine isNotNull() {
        Object actual = extractor.read(currentPath);
        if (actual == null) {
            throw new AssertionFailedError("Field '" + currentPath + "' was null but expected not null");
        }
        return this;
    }

    public AssertionEngine isNull() {
        Object actual = extractor.read(currentPath);
        if (actual != null) {
            throw new AssertionFailedError(
                    "Field '" + currentPath + "' was expected to be null but was <" + actual + ">",
                    null, actual
            );
        }
        return this;
    }

    public AssertionEngine contains(String expectedSubstring) {
        Object actual = extractor.read(currentPath);
        String actualStr = actual != null ? actual.toString() : "";
        if (!actualStr.contains(expectedSubstring)) {
            throw new AssertionFailedError(
                    "Field '" + currentPath + "' expected to contain '" + expectedSubstring + "' but was '" + actualStr + "'"
            );
        }
        return this;
    }

    public AssertionEngine matches(java.util.regex.Pattern pattern) {
        Object actual = extractor.read(currentPath);
        String actualStr = actual != null ? actual.toString() : "";
        if (!pattern.matcher(actualStr).matches()) {
            throw new AssertionFailedError(
                    "Field '" + currentPath + "' does not match pattern: " + pattern.pattern()
            );
        }
        return this;
    }

    public AssertionEngine body(Consumer<String> bodyValidator) {
        bodyValidator.accept(response.getBody());
        return this;
    }

    public AssertionEngine header(String headerName) {
        this.currentPath = "_header:" + headerName;
        return this;
    }

    public AssertionEngine headerValueIs(String expectedValue) {
        String headerName = currentPath.substring(8);
        String actual = response.getHeader(headerName);
        if (!Objects.equals(actual, expectedValue)) {
            throw new AssertionFailedError(
                    "Header '" + headerName + "' mismatch: expected <" + expectedValue + "> but was <" + actual + ">"
            );
        }
        return this;
    }

    public AssertionEngine and() {
        return this;
    }

    public AssertionEngine extract(String jsonPath, String variableName) {
        return extract(jsonPath, variableName, VariableScope.TEST);
    }

    public AssertionEngine extract(String jsonPath, String variableName, VariableScope scope) {
        Object value = extractor.read(jsonPath);
        if (context != null) {
            context.setVariable(scope, variableName, value);
        }
        log.debug("Extracted '{}' from '{}' into variable '{}'", value, jsonPath, variableName);
        return this;
    }

    public AssertionEngine extractHeader(String headerName, String variableName) {
        String value = response.getHeader(headerName);
        if (context != null) {
            context.setVariable(variableName, value);
        }
        return this;
    }

    public AssertionEngine saveAs(String variableName) {
        if (context != null) {
            context.setVariable(variableName, response.getBody());
        }
        return this;
    }

    public ApiResponse getResponse() {
        return response;
    }

    public JsonPathExtractor getExtractor() {
        return extractor;
    }
}
