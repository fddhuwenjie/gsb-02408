package com.gsb.apitest;

import com.gsb.apitest.http.HttpResponse;
import com.gsb.apitest.step.ApiFlow;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ExtractAndReuseTest extends MockServerTestSupport {

    @Test
    void testExtractTokenAndReuseInNextStep() throws Exception {
        enqueueResponse(200, "{\n" +
                "  \"code\": 0,\n" +
                "  \"message\": \"login ok\",\n" +
                "  \"data\": {\n" +
                "    \"token\": \"eyJhbGciOiJIUzI1NiJ9.test.token.value\",\n" +
                "    \"expiresIn\": 3600\n" +
                "  }\n" +
                "}");

        enqueueResponse(200, "{\n" +
                "  \"code\": 0,\n" +
                "  \"message\": \"ok\",\n" +
                "  \"data\": {\n" +
                "    \"profile\": {\n" +
                "      \"name\": \"Bob\",\n" +
                "      \"role\": \"admin\"\n" +
                "    }\n" +
                "  }\n" +
                "}");

        ApiFlow.setVar("username", "bob");
        ApiFlow.setVar("password", "Bob@2024");

        ApiFlow.post("/api/auth/login")
                .name("步骤1-登录获取Token")
                .body("{\"username\":\"${username}\",\"password\":\"${password}\"}")
                .expectStatus(200)
                .expect(a -> a.jsonPath("$.code", 0))
                .extract("token", "$.data.token")
                .execute();

        assertNotNull(ApiFlow.varStr("token"));
        assertEquals("eyJhbGciOiJIUzI1NiJ9.test.token.value", ApiFlow.varStr("token"));

        HttpResponse profileResponse = ApiFlow.get("/api/user/profile")
                .name("步骤2-带Token查询用户信息")
                .bearerToken(ApiFlow.varStr("token"))
                .expectStatus(200)
                .expect(a -> a
                        .jsonPath("$.code", 0)
                        .jsonPath("$.data.profile.name", "Bob")
                        .jsonPath("$.data.profile.role", "admin")
                )
                .execute();

        assertEquals("Bob", profileResponse.jsonPath("$.data.profile.name"));

        RecordedRequest loginReq = server.takeRequest();
        assertEquals("/api/auth/login", loginReq.getPath());
        String loginBody = loginReq.getBody().readUtf8();
        assertTrue(loginBody.contains("\"username\":\"bob\""));

        RecordedRequest profileReq = server.takeRequest();
        assertEquals("/api/user/profile", profileReq.getPath());
        String authHeader = profileReq.getHeader("Authorization");
        assertNotNull(authHeader);
        assertEquals("Bearer eyJhbGciOiJIUzI1NiJ9.test.token.value", authHeader);
    }

    @Test
    void testMultiStepVariableChaining() throws Exception {
        enqueueResponse(200, "{\n" +
                "  \"code\": 0,\n" +
                "  \"data\": { \"orderId\": \"ORD-2024-001\" }\n" +
                "}");

        enqueueResponse(200, "{\n" +
                "  \"code\": 0,\n" +
                "  \"data\": { \"status\": \"PAID\", \"amount\": 99.50 }\n" +
                "}");

        enqueueResponse(200, "{\n" +
                "  \"code\": 0,\n" +
                "  \"data\": { \"trackingNo\": \"SF1234567890\", \"delivered\": false }\n" +
                "}");

        ApiFlow.setVar("productId", "P-100");
        ApiFlow.setVar("quantity", "2");
        ApiFlow.setVar("userId", "U-500");

        ApiFlow.post("/api/order/create")
                .name("步骤1-创建订单")
                .template("create-order")
                .expectStatus(200)
                .expect(a -> a.jsonPath("$.code", 0))
                .extract("orderId", "$.data.orderId")
                .execute();

        assertEquals("ORD-2024-001", ApiFlow.varStr("orderId"));

        RecordedRequest createReq = server.takeRequest();
        String createBody = createReq.getBody().readUtf8();
        assertTrue(createBody.contains("\"productId\":\"P-100\""));
        assertTrue(createBody.contains("\"quantity\":2"));
        assertTrue(createBody.contains("\"userId\":\"U-500\""));

        ApiFlow.post("/api/order/pay")
                .name("步骤2-支付订单")
                .body("{\"orderId\":\"${orderId}\",\"payType\":\"ALIPAY\"}")
                .expectStatus(200)
                .expect(a -> a
                        .jsonPath("$.code", 0)
                        .jsonPath("$.data.status", "PAID")
                        .jsonPath("$.data.amount", 99.5)
                )
                .extract("payStatus", "$.data.status")
                .execute();

        assertEquals("PAID", ApiFlow.varStr("payStatus"));

        RecordedRequest payReq = server.takeRequest();
        String payBody = payReq.getBody().readUtf8();
        assertTrue(payBody.contains("\"orderId\":\"ORD-2024-001\""));
        assertTrue(payBody.contains("\"payType\":\"ALIPAY\""));

        HttpResponse shipResp = ApiFlow.post("/api/order/ship")
                .name("步骤3-查询物流")
                .body("{\"orderId\":\"${orderId}\"}")
                .expectStatus(200)
                .expect(a -> a
                        .jsonPath("$.code", 0)
                        .jsonPathNotNull("$.data.trackingNo")
                        .jsonPath("$.data.delivered", false)
                )
                .extract("trackingNo", "$.data.trackingNo")
                .execute();

        assertEquals("SF1234567890", shipResp.jsonPath("$.data.trackingNo"));
        assertEquals("SF1234567890", ApiFlow.varStr("trackingNo"));

        RecordedRequest shipReq = server.takeRequest();
        String shipBody = shipReq.getBody().readUtf8();
        assertTrue(shipBody.contains("\"orderId\":\"ORD-2024-001\""));
    }
}
