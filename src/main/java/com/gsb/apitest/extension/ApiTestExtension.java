package com.gsb.apitest.extension;

import com.gsb.apitest.context.ExecutionContext;
import com.gsb.apitest.report.ReportGenerator;
import org.junit.jupiter.api.extension.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApiTestExtension implements BeforeEachCallback, AfterEachCallback, BeforeTestExecutionCallback, AfterTestExecutionCallback {
    private static final Logger log = LoggerFactory.getLogger(ApiTestExtension.class);

    @Override
    public void beforeEach(ExtensionContext context) {
        String testId = context.getUniqueId();
        ExecutionContext ctx = ExecutionContext.create(testId);
        ExecutionContext.setCurrent(ctx);
        log.debug("初始化测试上下文: {}", testId);
    }

    @Override
    public void afterEach(ExtensionContext context) {
        try {
            ExecutionContext ctx = ExecutionContext.getCurrent();
            if (ctx != null) {
                ReportGenerator reportGenerator = new ReportGenerator();
                String testClass = context.getRequiredTestClass().getSimpleName();
                String testMethod = context.getRequiredTestMethod().getName();
                reportGenerator.generateReport(ctx, testClass, testMethod);
            }
        } catch (Exception e) {
            log.error("生成报告失败: {}", e.getMessage(), e);
        } finally {
            ExecutionContext.clear();
            log.debug("清理测试上下文");
        }
    }

    @Override
    public void beforeTestExecution(ExtensionContext context) {
    }

    @Override
    public void afterTestExecution(ExtensionContext context) {
    }
}
