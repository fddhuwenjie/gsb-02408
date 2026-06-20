package com.gsb.apitest.unit;

import com.gsb.apitest.context.TestContext;
import com.gsb.apitest.template.TemplateRenderer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** 模板替换器的纯单元测试，避免依赖任何 HTTP。 */
class TemplateRendererUnitTest {

    @Test
    void shouldReplaceVariablesAndFunctions() {
        TestContext ctx = new TestContext("UnitClass", "case1");
        ctx.setVar("name", "alice");
        TemplateRenderer renderer = new TemplateRenderer();

        String result = renderer.render("hello ${name}, id=${uuid()}", ctx);
        assertTrue(result.startsWith("hello alice, id="));
        assertTrue(result.length() > "hello alice, id=".length());
    }

    @Test
    void shouldEncodeFileToBase64FromClasspath() {
        TestContext ctx = new TestContext("UnitClass", "case2");
        TemplateRenderer renderer = new TemplateRenderer();

        String body = renderer.render(
                "{\"content\":\"${fileToBase64(samples/sample-payload.txt)}\"}",
                ctx);

        // 校验确实是 base64：解码后包含原文中的标识
        int start = body.indexOf("\"content\":\"") + "\"content\":\"".length();
        int end = body.lastIndexOf("\"");
        String b64 = body.substring(start, end);
        assertNotNull(b64);
        assertTrue(b64.length() > 0);
        String decoded = new String(java.util.Base64.getDecoder().decode(b64));
        assertTrue(decoded.contains("hello-from-gsb-apitest-framework"));
    }

    @Test
    void variableScopesShouldNotLeakAcrossCases() {
        TestContext.resetClassScopes();
        TestContext a = new TestContext("ClassA", "caseA");
        TestContext b = new TestContext("ClassB", "caseB");
        a.setVar("token", "tk-A");
        b.setVar("token", "tk-B");
        assertEquals("tk-A", a.getVar("token"));
        assertEquals("tk-B", b.getVar("token"));
    }
}
