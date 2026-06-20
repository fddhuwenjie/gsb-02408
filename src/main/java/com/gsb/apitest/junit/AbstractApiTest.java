package com.gsb.apitest.junit;

import com.gsb.apitest.config.ConfigManager;
import com.gsb.apitest.context.TestContext;
import com.gsb.apitest.http.HttpExecutor;
import com.gsb.apitest.report.ReportWriter;
import com.gsb.apitest.step.Step;
import com.gsb.apitest.template.TemplateRenderer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;

/**
 * 业务测试同学的推荐基类。继承它可以：
 * <ul>
 *     <li>自动获得 {@link TestContext}（每个 @Test 一份）</li>
 *     <li>调用 {@link #step(String)} 快速创建步骤</li>
 *     <li>通过 {@link #setVar} / {@link #getVar} 直接读写跨步骤变量</li>
 *     <li>用例结束自动写一份 JSON 报告到 apitest.report.dir</li>
 * </ul>
 *
 * 同一测试类的多个 @Test 共享 class scope，互不干扰 case scope。
 * 不依赖 JUnit Extension 注册，避免基类与扩展重复创建上下文。
 */
public abstract class AbstractApiTest {

    private TestContext ctx;
    private TemplateRenderer renderer;
    private HttpExecutor executor;

    @BeforeEach
    final void __apitestSetup(TestInfo info) {
        this.ctx = new TestContext(
                info.getTestClass().map(Class::getName).orElse("unknown"),
                info.getTestMethod().map(java.lang.reflect.Method::getName).orElse("unknown"));
        this.renderer = new TemplateRenderer();
        this.executor = new HttpExecutor(ConfigManager.getInstance().getTimeoutMs());
        prepareContext(ctx);
    }

    @AfterEach
    final void __apitestTeardown() {
        if (ctx != null) {
            try {
                ReportWriter.writeReport(ctx);
            } catch (RuntimeException e) {
                System.err.println("[apitest] report failure: " + e.getMessage());
            }
        }
    }

    /** 业务侧可重写做用例前置准备，例如登录、播种数据等。 */
    protected void prepareContext(TestContext ctx) {
    }

    protected TestContext context() {
        return ctx;
    }

    protected TemplateRenderer renderer() {
        return renderer;
    }

    protected Step step(String name) {
        return new Step(name, ctx, renderer, executor);
    }

    protected void setVar(String name, Object value) {
        ctx.setVar(name, value);
    }

    protected Object getVar(String name) {
        return ctx.getVar(name);
    }
}
