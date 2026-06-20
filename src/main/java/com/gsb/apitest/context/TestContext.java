package com.gsb.apitest.context;

import com.gsb.apitest.config.ConfigManager;
import com.gsb.apitest.report.StepRecord;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 测试执行上下文：每个测试用例（method）独立持有一个，保证并发执行互不干扰。
 *
 * <p>变量作用域分为三层：
 * <ul>
 *     <li>global  —— 跨用例共享（来自配置或显式注入）</li>
 *     <li>class   —— 同一个测试类的多个 @Test 共享</li>
 *     <li>case    —— 当前 @Test 内部多步骤共享</li>
 * </ul>
 *
 * <p>另外维护一个 stepRecords 列表，按步骤顺序保存请求/响应/断言信息，
 * 用于失败定位与最终报告输出。
 */
public class TestContext {

    private static final ConcurrentHashMap<String, VariableScope> CLASS_SCOPES = new ConcurrentHashMap<>();
    private static final VariableScope GLOBAL = new VariableScope("global", null);

    private final String testId;
    private final String testClassName;
    private final String testMethodName;
    private final VariableScope caseScope;
    private final List<StepRecord> stepRecords = new CopyOnWriteArrayList<>();
    private final ConfigManager config;

    public TestContext(String testClassName, String testMethodName) {
        this.testClassName = testClassName;
        this.testMethodName = testMethodName;
        this.testId = testClassName + "#" + testMethodName + "@" + System.nanoTime();
        VariableScope classScope = CLASS_SCOPES.computeIfAbsent(
                testClassName, k -> new VariableScope("class:" + k, GLOBAL));
        this.caseScope = new VariableScope("case:" + testMethodName, classScope);
        this.config = ConfigManager.getInstance();
    }

    public String getTestId() {
        return testId;
    }

    public String getTestClassName() {
        return testClassName;
    }

    public String getTestMethodName() {
        return testMethodName;
    }

    public ConfigManager config() {
        return config;
    }

    public VariableScope vars() {
        return caseScope;
    }

    public static VariableScope global() {
        return GLOBAL;
    }

    public void setVar(String key, Object value) {
        caseScope.put(key, value);
    }

    public Object getVar(String key) {
        return caseScope.resolve(key);
    }

    public void recordStep(StepRecord record) {
        stepRecords.add(record);
    }

    public List<StepRecord> getStepRecords() {
        return new ArrayList<>(stepRecords);
    }

    /** 仅供测试时清理 class scope。 */
    public static void resetClassScopes() {
        CLASS_SCOPES.clear();
    }
}
