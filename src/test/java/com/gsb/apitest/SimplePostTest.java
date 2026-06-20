package com.gsb.apitest;

import com.gsb.apitest.context.ContextHolder;
import com.gsb.apitest.http.HttpResponse;
import com.gsb.apitest.step.ApiFlow;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SimplePostTest extends MockServerTestSupport {

    @Test
    void testSimplePostWithInlineBody() throws Exception {
        enqueueResponse(200, "{\n" +
                "  \"code\": 0,\n" +
                "  \"message\": \"success\",\n" +
                "  \"data\": {\n" +
                "    \"id\": 1001,\n" +
                "    \"name\": \"Alice\"\n" +
                "  }\n" +
                "}");

        HttpResponse response = ApiFlow.post("/api/user/create")
                .name("创建用户")
                .body("{\"username\":\"alice\",\"email\":\"alice@example.com\"}")
                .expectStatus(200)
                .expect(a -> a
                        .jsonPath("$.code", 0)
                        .jsonPath("$.message", "success")
                        .jsonPath("$.data.name", "Alice")
                        .jsonPathNotNull("$.data.id")
                        .bodyContains("success")
                )
                .execute();

        assertEquals(200, response.getStatusCode());
        assertEquals("Alice", response.jsonPath("$.data.name"));
        assertEquals(1001, ((Number) response.jsonPath("$.data.id")).intValue());

        RecordedRequest request = server.takeRequest();
        assertEquals("POST", request.getMethod());
        assertEquals("/api/user/create", request.getPath());
        String requestBody = request.getBody().readUtf8();
        assertTrue(requestBody.contains("\"username\":\"alice\""));
        assertTrue(requestBody.contains("\"email\":\"alice@example.com\""));
    }

    @Test
    void testPostWithTemplateFile() throws Exception {
        enqueueResponse(200, "{\n" +
                "  \"code\": 0,\n" +
                "  \"message\": \"login success\",\n" +
                "  \"data\": {\n" +
                "    \"userId\": 8888\n" +
                "  }\n" +
                "}");

        ContextHolder.get().setGlobalVariable("username", "admin");
        ContextHolder.get().setGlobalVariable("password", "Admin@123");

        HttpResponse response = ApiFlow.post("/api/login")
                .name("用户登录")
                .template("login")
                .expectStatus(200)
                .expect(a -> a
                        .jsonPath("$.code", 0)
                        .jsonPath("$.data.userId", 8888)
                )
                .execute();

        assertEquals(200, response.getStatusCode());
        assertEquals(8888, ((Number) response.jsonPath("$.data.userId")).intValue());

        RecordedRequest request = server.takeRequest();
        String requestBody = request.getBody().readUtf8();
        assertTrue(requestBody.contains("\"username\":\"admin\""));
        assertTrue(requestBody.contains("\"password\":\"Admin@123\""));
        assertTrue(requestBody.contains("\"rememberMe\":true"));
    }
}
