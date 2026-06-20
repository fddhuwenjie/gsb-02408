package com.gsb.apitest.extension;

import com.gsb.apitest.config.ConfigLoader;
import com.gsb.apitest.config.TestConfig;
import com.gsb.apitest.context.ExecutionContext;
import com.gsb.apitest.http.HttpClientWrapper;
import com.gsb.apitest.report.TestReportGenerator;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApiTestExtension implements BeforeAllCallback, BeforeEachCallback, AfterEachCallback {
    private static final Logger log = LoggerFactory.getLogger(ApiTestExtension.class);
    private static TestConfig staticConfig;
    private static HttpClientWrapper staticHttpClient;

    public static void setHttpClient(HttpClientWrapper client) {
        staticHttpClient = client;
    }

    public static void resetHttpClient() {
        if (staticConfig != null) {
            staticHttpClient = new HttpClientWrapper(staticConfig);
        }
    }

    @Override
    public void beforeAll(ExtensionContext context) {
        if (staticConfig == null) {
            staticConfig = ConfigLoader.load();
        }
        if (staticHttpClient == null) {
            staticHttpClient = new HttpClientWrapper(staticConfig);
            log.info("API Test framework initialized with baseUrl: {}", staticConfig.getBaseUrl());
        }
    }

    @Override
    public void beforeEach(ExtensionContext context) {
        String testName = context.getDisplayName();
        if (context.getTestMethod().isPresent()) {
            testName = context.getTestMethod().get().getName();
        }

        ExecutionContext ctx = new ExecutionContext(staticConfig);
        ctx.startTest(testName);
        ExecutionContext.setCurrent(ctx);
    }

    @Override
    public void afterEach(ExtensionContext context) {
        ExecutionContext ctx = ExecutionContext.getCurrent();
        if (ctx != null) {
            ctx.endTest();
            TestReportGenerator reporter = new TestReportGenerator();
            reporter.generateReport(ctx);
            if (ctx.isFailed()) {
                reporter.logFailure(ctx);
            }
            ctx.getVariables().clearStepScope();
            ExecutionContext.clearCurrent();
        }
    }

    public static TestConfig getConfig() {
        return staticConfig;
    }

    public static HttpClientWrapper getHttpClient() {
        return staticHttpClient;
    }
}
