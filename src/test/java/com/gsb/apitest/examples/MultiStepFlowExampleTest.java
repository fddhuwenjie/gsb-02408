package com.gsb.apitest.examples;

import com.gsb.apitest.context.TestContext;
import com.gsb.apitest.support.MockBackedApiTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * 示例 2：多步骤变量传递。
 * 第一步登录拿 token，第二步用 ${token} 访问 /me，
 * 同时演示从请求体 / 响应体里抽字段保存。
 */
@DisplayName("示例2：多步骤串联 + 字段提取复用")
class MultiStepFlowExampleTest extends MockBackedApiTest {

    @Override
    protected void prepareContext(TestContext ctx) {
        ctx.setVar("clientId", "ios-app");
    }

    @Test
    @DisplayName("登录拿 token -> 用 token 访问 /me")
    void loginThenFetchProfile() {
        // step1: 登录，从请求体抽 username 保存为 loginUser，从响应抽 token + userId
        step("login")
                .post("/login")
                .bodyFromTemplate("templates/login_request.json")
                .extractFromRequest("loginUser", "$.username")
                .extractFromResponse("token", "$.token")
                .extractFromResponse("userId", "$.userId")
                .assertThat(ra -> ra
                        .statusCode(200)
                        .jsonPathExists("$.token"))
                .run();

        assertEquals("alice", getVar("loginUser"));
        assertNotNull(getVar("token"));
        assertEquals("u-100", getVar("userId"));

        // step2: 通过 ${token} 自动渲染 Authorization
        step("fetch-profile")
                .get("/me")
                .header("Authorization", "Bearer ${token}")
                .assertThat(ra -> ra
                        .statusCode(200)
                        .jsonPathEquals("$.userId", "u-100"))
                .run();
    }

    @Test
    @DisplayName("登录失败时应当被 expectFailure 接住，不污染流程")
    void loginFailureShouldBeRecorded() {
        step("login-bad")
                .post("/login")
                .body("{\"username\":\"alice\",\"password\":\"WRONG\"}")
                .expectFailure()
                .assertThat(ra -> ra
                        .statusCode(401)
                        .jsonPathEquals("$.error", "invalid_credentials"))
                .run();
    }
}
