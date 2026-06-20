package com.gsb.apitest.assertion;

import com.gsb.apitest.context.ExecutionContext;
import com.gsb.apitest.dsl.StepRecorder;
import com.gsb.apitest.model.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

public class AssertionEngine {
    private static final Logger log = LoggerFactory.getLogger(AssertionEngine.class);
    private final ExecutionContext context;

    public AssertionEngine(ExecutionContext context) {
        this.context = context;
    }

    public void executeAll(List<StepRecorder.AssertionBuilder> assertions, ApiResponse response, String stepName) {
        if (assertions == null || assertions.isEmpty()) {
            return;
        }
        for (StepRecorder.AssertionBuilder assertion : assertions) {
            execute(assertion, response, stepName);
        }
    }

    public void execute(StepRecorder.AssertionBuilder assertion, ApiResponse response, String stepName) {
        String jsonPath = assertion.getJsonPath();
        String type = assertion.getAssertionType();
        Object expected = assertion.getExpectedValue();

        Object actual;
        if ("$.statusCode".equals(jsonPath) || "$.status".equals(jsonPath)) {
            actual = response.getStatusCode();
        } else if ("$.body".equals(jsonPath)) {
            actual = response.getBody();
        } else {
            try {
                actual = response.jsonPath(jsonPath);
            } catch (Exception e) {
                fail(String.format("[步骤: %s] JSON路径 [%s] 不存在或解析失败: %s", stepName, jsonPath, e.getMessage()));
                return;
            }
        }

        log.debug("断言 [步骤: {}] 路径: {}, 实际值: {}, 期望值: {}, 类型: {}",
                stepName, jsonPath, actual, expected, type);

        switch (type) {
            case "equals":
                if (expected instanceof Number && actual instanceof Number) {
                    assertEquals(((Number) expected).doubleValue(), ((Number) actual).doubleValue(),
                            String.format("[步骤: %s] 断言失败: 路径 [%s] 期望 [%s], 实际 [%s]",
                                    stepName, jsonPath, expected, actual));
                } else {
                    assertEquals(String.valueOf(expected), String.valueOf(actual),
                            String.format("[步骤: %s] 断言失败: 路径 [%s] 期望 [%s], 实际 [%s]",
                                    stepName, jsonPath, expected, actual));
                }
                break;
            case "notNull":
                assertNotNull(actual,
                        String.format("[步骤: %s] 断言失败: 路径 [%s] 期望非null, 实际为null",
                                stepName, jsonPath));
                break;
            case "isNull":
                assertNull(actual,
                        String.format("[步骤: %s] 断言失败: 路径 [%s] 期望为null, 实际 [%s]",
                                stepName, jsonPath, actual));
                break;
            case "contains":
                assertNotNull(actual,
                        String.format("[步骤: %s] 断言失败: 路径 [%s] 为null, 无法检查包含", stepName, jsonPath));
                assertTrue(String.valueOf(actual).contains(String.valueOf(expected)),
                        String.format("[步骤: %s] 断言失败: 路径 [%s] 的值 [%s] 不包含 [%s]",
                                stepName, jsonPath, actual, expected));
                break;
            case "greaterThan":
                assertTrue(actual instanceof Number,
                        String.format("[步骤: %s] 断言失败: 路径 [%s] 不是数字类型", stepName, jsonPath));
                double actualNum = ((Number) actual).doubleValue();
                double expectedNum = ((Number) expected).doubleValue();
                assertTrue(actualNum > expectedNum,
                        String.format("[步骤: %s] 断言失败: 路径 [%s] 的值 [%s] 不大于 [%s]",
                                stepName, jsonPath, actual, expected));
                break;
            case "lessThan":
                assertTrue(actual instanceof Number,
                        String.format("[步骤: %s] 断言失败: 路径 [%s] 不是数字类型", stepName, jsonPath));
                actualNum = ((Number) actual).doubleValue();
                expectedNum = ((Number) expected).doubleValue();
                assertTrue(actualNum < expectedNum,
                        String.format("[步骤: %s] 断言失败: 路径 [%s] 的值 [%s] 不小于 [%s]",
                                stepName, jsonPath, actual, expected));
                break;
            default:
                log.warn("未知的断言类型: {}", type);
        }
    }
}
