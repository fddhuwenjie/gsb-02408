package com.gsb.apitest.step;

import com.gsb.apitest.config.ConfigLoader;
import com.gsb.apitest.config.TestConfig;
import com.gsb.apitest.context.ContextHolder;
import com.gsb.apitest.context.TestContext;
import com.gsb.apitest.report.JsonReportGenerator;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApiTestExtension implements BeforeAllCallback, AfterAllCallback, BeforeEachCallback, AfterEachCallback {

    private static final Logger log = LoggerFactory.getLogger(ApiTestExtension.class);

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        TestConfig config = buildConfig(context);
        ContextHolder.init(config);
        TestContext ctx = ContextHolder.get();
        String className = context.getRequiredTestClass().getSimpleName();
        ctx.enterClass(className);
        log.info("初始化测试类: {}", className);
    }

    @Override
    public void afterAll(ExtensionContext context) throws Exception {
        TestContext ctx = ContextHolder.get();
        String className = context.getRequiredTestClass().getSimpleName();

        boolean allPassed = context.getExecutionException().isEmpty();
        for (var method : ctx.getReportCollector().getCurrentClass() != null
                ? ctx.getReportCollector().getCurrentClass().getMethods() : java.util.Collections.emptyList()) {
            // method records already finalized
        }

        ctx.getReportCollector().endClass();
        ctx.exitClass();

        if (config(ctx).isReportEnabled()) {
            ctx.getReportCollector().printConsoleSummary();
            JsonReportGenerator.writeToFile(ctx.getReportCollector(), config(ctx).getReportDir());
        }

        ContextHolder.clear();
        log.info("测试类完成: {}", className);
    }

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        TestContext ctx = ContextHolder.get();
        String methodName = context.getRequiredTestMethod().getName();
        ctx.enterMethod(methodName);
        log.info("━━▶ 开始测试方法: {}", methodName);
    }

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        TestContext ctx = ContextHolder.get();
        String methodName = context.getRequiredTestMethod().getName();
        boolean passed = context.getExecutionException().isEmpty();
        String error = context.getExecutionException()
                .map(Throwable::getMessage)
                .orElse(null);
        ctx.getReportCollector().endMethod(passed, error);
        ctx.exitMethod();
        log.info("━━▶ 测试方法结束: {} {}", methodName, passed ? "✓ PASS" : "✗ FAIL");
    }

    private TestConfig buildConfig(ExtensionContext context) {
        TestConfig baseConfig = ConfigLoader.load();
        TestConfig config = baseConfig.copy();

        context.getTestClass().ifPresent(testClass -> {
            ApiTest apiTest = testClass.getAnnotation(ApiTest.class);
            if (apiTest != null) {
                if (!apiTest.baseUrl().isEmpty()) {
                    config.setBaseUrl(apiTest.baseUrl());
                }
            }
        });

        return config;
    }

    private TestConfig config(TestContext ctx) {
        return ctx.getConfig();
    }
}
