package com.gsb.apitest.junit;

import com.gsb.apitest.config.ConfigManager;
import com.gsb.apitest.context.TestContext;
import com.gsb.apitest.http.HttpExecutor;
import com.gsb.apitest.report.ReportWriter;
import com.gsb.apitest.step.Step;
import com.gsb.apitest.template.TemplateRenderer;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * JUnit 5 扩展：在每个 @Test 执行前后注入 / 清理 TestContext，
 * 并在结束时自动落地一份 JSON 报告。业务侧只需在测试类上加
 * {@code @ExtendWith(ApiTestExtension.class)}，
 * 或继承 {@link com.gsb.apitest.junit.AbstractApiTest}。
 */
public class ApiTestExtension implements BeforeEachCallback, AfterEachCallback {

    private static final ExtensionContext.Namespace NS =
            ExtensionContext.Namespace.create("com.gsb.apitest");
    private static final String CTX_KEY = "ctx";

    @Override
    public void beforeEach(ExtensionContext context) {
        String klass = context.getRequiredTestClass().getName();
        String method = context.getRequiredTestMethod().getName();
        TestContext ctx = new TestContext(klass, method);
        context.getStore(NS).put(CTX_KEY, ctx);
    }

    @Override
    public void afterEach(ExtensionContext context) {
        TestContext ctx = context.getStore(NS).get(CTX_KEY, TestContext.class);
        if (ctx != null) {
            try {
                ReportWriter.writeReport(ctx);
            } catch (RuntimeException e) {
                // 报告失败不应淹没真正的用例失败原因
                System.err.println("[apitest] report failure: " + e.getMessage());
            }
        }
    }

    public static TestContext currentContext(ExtensionContext context) {
        return context.getStore(NS).get(CTX_KEY, TestContext.class);
    }

    /** 业务侧可以在 @Test 内通过这个工厂快速创建 Step。 */
    public static Step newStep(String name, TestContext ctx) {
        TemplateRenderer renderer = new TemplateRenderer();
        HttpExecutor executor = new HttpExecutor(ConfigManager.getInstance().getTimeoutMs());
        return new Step(name, ctx, renderer, executor);
    }
}
