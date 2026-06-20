package com.gsb.apitest;

import com.gsb.apitest.mock.MockApiServer;
import org.junit.jupiter.api.*;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class FrameworkDemoTest extends ApiTestBase {

    private static MockApiServer mockServer;

    @BeforeAll
    static void startServer() throws IOException {
        mockServer = new MockApiServer();
        mockServer.start();
    }

    @AfterAll
    static void stopServer() {
        mockServer.stop();
    }

    @BeforeEach
    void setUp() {
        setBaseUrl(mockServer.getBaseUrl());
        setGlobalHeader("X-Request-From", "ApiTestFramework");
    }

    @Test
    @Order(1)
    @DisplayName("示例1: 普通POST请求 - 登录接口直接发送JSON")
    void testSimplePostRequest() throws IOException {
        steps()
            .step("用户登录")
                .post("/api/login")
                .jsonBody("{\"username\":\"admin\",\"password\":\"123456\"}")
                .expectStatus(200)
                .assertThat("$.code").equalsTo(0)
                .assertThat("$.data.username").equalsTo("admin")
                .extract("token", "$.data.token")
                .extract("userId", "$.data.userId")
                .execute();

        assertNotNull(var("token"));
        assertEquals(10001, var("userId"));
        log.info("提取的token: {}", varStr("token"));
    }

    @Test
    @Order(2)
    @DisplayName("示例2: 字段提取复用 + 多步骤串联 - 登录->查用户信息")
    void testVariableExtractionAndMultiStep() throws IOException {
        setGlobalVariable("username", "admin");
        setGlobalVariable("password", "123456");

        steps()
            .step("第一步: 用户登录获取token")
                .post("/api/login")
                .body("{\"username\":\"${username}\",\"password\":\"${password}\"}")
                .assertThat("$.code").equalsTo(0)
                .assertThat("$.data.token").notNull()
                .extract("authToken", "$.data.token")
                .execute()

            .and().step("第二步: 使用token查询用户信息")
                .get("/api/user/info")
                .header("Authorization", "${authToken}")
                .assertThat("$.code").equalsTo(0)
                .assertThat("$.data.email").contains("@example.com")
                .assertThat("$.data.roles[0]").equalsTo("admin")
                .extract("userEmail", "$.data.email")
                .execute();

        assertNotNull(var("authToken"));
        assertTrue(varStr("userEmail").endsWith("@example.com"));
        log.info("用户邮箱: {}", varStr("userEmail"));
    }

    @Test
    @Order(3)
    @DisplayName("示例3: 从模板文件读取请求体 + fileToBase64文件转Base64")
    void testTemplateAndFileToBase64() throws IOException {
        setGlobalVariable("username", "admin");
        setGlobalVariable("fileName", "document.txt");

        steps()
            .step("文件上传(使用模板+Base64)")
                .post("/api/upload")
                .bodyTemplate("upload.json")
                .assertThat("$.code").equalsTo(0)
                .assertThat("$.data.fileName").equalsTo("document.txt")
                .assertThat("$.data.status").equalsTo("uploaded")
                .assertThat("$.data.fileSize").greaterThan(0)
                .extract("fileId", "$.data.fileId")
                .execute();

        assertNotNull(var("fileId"));
        log.info("上传成功，文件ID: {}", varStr("fileId"));
    }

    @Test
    @Order(4)
    @DisplayName("示例4: 完整业务流程 - 登录->建单->校验多步骤变量传递")
    void testFullBusinessFlow() throws IOException {
        setGlobalVariable("username", "admin");
        setGlobalVariable("password", "123456");
        setGlobalVariable("productId", "PROD001");
        setGlobalVariable("amount", 2);

        steps()
            .step("1-用户登录")
                .post("/api/login")
                .bodyTemplate("login.json")
                .assertThat("$.code").equalsTo(0)
                .extract("token", "$.data.token")
                .extract("userId", "$.data.userId")
                .execute()

            .and().step("2-创建订单(复用前面步骤的变量)")
                .post("/api/order/create")
                .bodyTemplate("create_order.json")
                .assertThat("$.code").equalsTo(0)
                .assertThat("$.data.productId").equalsTo("PROD001")
                .assertThat("$.data.amount").equalsTo(2)
                .assertThat("$.data.totalPrice").greaterThan(0)
                .extract("orderId", "$.data.orderId")
                .execute()

            .and().step("3-验证订单数据")
                .post("/api/echo")
                .jsonBody("{\"orderId\":\"${orderId}\",\"check\":true}")
                .assertThat("$.code").equalsTo(0)
                .assertThat("$.body").contains("ORD")
                .execute();

        assertNotNull(var("orderId"));
        assertTrue(varStr("orderId").startsWith("ORD"));
        log.info("完整流程完成，订单号: {}", varStr("orderId"));
    }
}
