package com.gsb.apitest.examples;

import com.gsb.apitest.context.TestContext;
import com.gsb.apitest.http.HttpResponse;
import com.gsb.apitest.support.MockBackedApiTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * 示例 1：最朴素的 POST 用例。
 * 验证“配置统一加载 + 默认 header + 简单断言”能正常工作。
 */
@DisplayName("示例1：基础 POST 调用 + 响应断言")
class BasicPostExampleTest extends MockBackedApiTest {

    @Override
    protected void prepareContext(TestContext ctx) {
        // 用例前置准备：注入业务级常量到 case scope
        ctx.setVar("clientId", "demo-client");
    }

    @Test
    @DisplayName("POST /echo 应回写请求体并附带 sequence")
    void shouldEchoRequestBody() {
        HttpResponse response = step("post-echo")
                .post("/echo")
                .body("{\"hello\":\"world\",\"client\":\"${clientId}\"}")
                .assertThat(ra -> ra
                        .statusCode(200)
                        .jsonPathEquals("$.echo.hello", "world")
                        .jsonPathEquals("$.echo.client", "demo-client")
                        .jsonPathExists("$.sequence"))
                .run();

        assertEquals(200, response.getStatusCode());
        assertNotNull(response.getBodyJson());
    }
}
