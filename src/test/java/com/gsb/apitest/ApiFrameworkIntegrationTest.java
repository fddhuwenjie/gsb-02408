package com.gsb.apitest;

import com.gsb.apitest.config.ConfigLoader;
import com.gsb.apitest.config.TestConfig;
import com.gsb.apitest.dsl.ApiTest;
import com.gsb.apitest.extension.ApiTestExtension;
import com.gsb.apitest.http.HttpClientWrapper;
import com.gsb.apitest.mock.MockApiInterceptor;
import org.junit.jupiter.api.*;

import static com.gsb.apitest.dsl.ApiTestDsl.*;
import static org.junit.jupiter.api.Assertions.*;

@ApiTest
public class ApiFrameworkIntegrationTest {

    @BeforeAll
    static void setupMockClient() {
        TestConfig config = ConfigLoader.load();
        HttpClientWrapper mockClient = HttpClientWrapper.custom(config)
                .addInterceptor(new MockApiInterceptor())
                .build();
        ApiTestExtension.setHttpClient(mockClient);
    }

    @AfterAll
    static void resetClient() {
        ApiTestExtension.resetHttpClient();
    }

    @Test
    @DisplayName("场景1: 普通POST请求 + 内联body + 状态码断言")
    void testSimplePostRequest() {
        prepare(() -> {
            setVar("visitorName", "TraeAI");
        });

        post("/api/greet")
                .body("{\"name\": \"${visitorName}\"}")
                .then()
                .statusCodeIs(200)
                .and()
                .bodyField("$.code").isEqualTo(0)
                .and()
                .bodyField("$.message").contains("Hello, TraeAI");
    }

    @Test
    @DisplayName("场景2: 字段提取 + 变量复用 + 多步骤串联（登录拿token -> 查询用户信息 -> 创建订单）")
    void testLoginAndMultiStepChaining() {
        prepare(() -> {
            setVar("username", "admin");
            setVar("password", "123456");
        });

        step("Step1 - 用户登录获取Token", () -> {
            post("/api/login")
                    .bodyFromTemplate("templates/login-request.json")
                    .then()
                    .statusCodeIs(200)
                    .bodyField("$.code").isEqualTo(0)
                    .bodyField("$.data.token").isNotNull()
                    .extract("$.data.token", "authToken")
                    .extract("$.data.userId", "uid");
        });

        step("Step2 - 使用Token拉取用户信息", () -> {
            assertNotNull(getVar("authToken"), "上一步提取的token应该存在");

            post("/api/user/info")
                    .header("Authorization", "Bearer ${authToken}")
                    .body("{}")
                    .then()
                    .statusCodeIs(200)
                    .bodyField("$.code").isEqualTo(0)
                    .bodyField("$.data.userId").isEqualTo(getVar("uid", Integer.class))
                    .extract("$.data.email", "userEmail");
        });

        step("Step3 - 使用用户ID创建订单（多步变量传递验证）", () -> {
            assertEquals("admin@gsb.com", getVar("userEmail"), "第二步提取的email可在第三步使用");

            setVar("productName", "API-Testing-Course");
            setVar("quantity", 2);
            setVar("price", 299.00);

            post("/api/order/create")
                    .body("{\"userId\": ${uid}, \"productName\": \"${productName}\", \"quantity\": ${quantity}, \"price\": ${price}}")
                    .then()
                    .statusCodeIs(200)
                    .bodyField("$.code").isEqualTo(0)
                    .bodyField("$.data.status").isEqualTo("CREATED")
                    .bodyField("$.data.productName").isEqualTo("API-Testing-Course")
                    .extract("$.data.orderId", "newOrderId");
        });

        assertNotNull(getVar("newOrderId"), "三步后最终orderId应保存在变量中");
    }

    @Test
    @DisplayName("场景3: fileToBase64() 模板函数自动编码文件嵌入请求体")
    void testFileToBase64TemplateFunction() {
        prepare(() -> {
            setVar("username", "admin");
        });

        post("/api/upload")
                .bodyFromTemplate("templates/upload-request.json")
                .then()
                .statusCodeIs(200)
                .bodyField("$.code").isEqualTo(0)
                .bodyField("$.data.base64Verified").isEqualTo(true);
    }
}
