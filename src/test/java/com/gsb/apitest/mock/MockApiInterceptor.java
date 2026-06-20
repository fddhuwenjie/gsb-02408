package com.gsb.apitest.mock;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public class MockApiInterceptor implements Interceptor {
    private static final Logger log = LoggerFactory.getLogger(MockApiInterceptor.class);
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final MediaType JSON_MEDIA = MediaType.parse("application/json; charset=utf-8");
    public static AtomicBoolean lastUploadVerified = new AtomicBoolean(false);

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        String path = request.url().encodedPath();
        String method = request.method();
        String bodyStr = "";
        if (request.body() != null) {
            try (okio.Buffer buffer = new okio.Buffer()) {
                request.body().writeTo(buffer);
                bodyStr = buffer.readUtf8();
            }
        }

        log.info("Mock intercepted: {} {} (body length: {})", method, path, bodyStr.length());

        String responseJson;
        int statusCode;

        try {
            if ("/api/greet".equals(path)) {
                JsonNode req = mapper.readTree(bodyStr.isEmpty() ? "{}" : bodyStr);
                String name = req.path("name").asText("world");
                ObjectNode resp = mapper.createObjectNode();
                resp.put("code", 0);
                resp.put("message", "Hello, " + name + "!");
                responseJson = mapper.writeValueAsString(resp);
                statusCode = 200;
            } else if ("/api/login".equals(path)) {
                JsonNode req = mapper.readTree(bodyStr);
                String username = req.path("username").asText();
                String password = req.path("password").asText();
                ObjectNode resp = mapper.createObjectNode();
                if ("admin".equals(username) && "123456".equals(password)) {
                    resp.put("code", 0);
                    resp.put("message", "success");
                    ObjectNode data = resp.putObject("data");
                    data.put("token", "TOKEN-" + UUID.randomUUID().toString().substring(0, 8));
                    data.put("userId", 10001);
                    data.put("userName", username);
                    data.put("expiresIn", 3600);
                } else {
                    resp.put("code", 401);
                    resp.put("message", "Invalid credentials");
                }
                responseJson = mapper.writeValueAsString(resp);
                statusCode = 200;
            } else if ("/api/user/info".equals(path)) {
                String auth = request.header("Authorization");
                log.info("User info request, Authorization: {}", auth);
                ObjectNode resp = mapper.createObjectNode();
                resp.put("code", 0);
                ObjectNode data = resp.putObject("data");
                data.put("userId", 10001);
                data.put("userName", "admin");
                data.put("email", "admin@gsb.com");
                data.put("roles", "admin,user");
                responseJson = mapper.writeValueAsString(resp);
                statusCode = 200;
            } else if ("/api/order/create".equals(path)) {
                JsonNode req = mapper.readTree(bodyStr);
                log.info("Create order, body: {}", bodyStr);
                ObjectNode resp = mapper.createObjectNode();
                resp.put("code", 0);
                resp.put("message", "order created");
                ObjectNode data = resp.putObject("data");
                data.put("orderId", "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
                data.put("productName", req.path("productName").asText());
                data.put("quantity", req.path("quantity").asInt(1));
                data.put("price", req.path("price").asDouble(0));
                data.put("status", "CREATED");
                responseJson = mapper.writeValueAsString(resp);
                statusCode = 200;
            } else if ("/api/upload".equals(path)) {
                JsonNode req = mapper.readTree(bodyStr);
                String fileData = req.path("fileData").asText();
                String operator = req.path("operator").asText();

                boolean verified = false;
                if (fileData != null && !fileData.isEmpty()) {
                    byte[] decoded = Base64.getDecoder().decode(fileData);
                    String content = new String(decoded, StandardCharsets.UTF_8);
                    log.info("Upload by {}, decoded content: '{}'", operator, content.trim());
                    verified = content.startsWith("Hello GSB");
                }
                lastUploadVerified.set(verified);

                ObjectNode resp = mapper.createObjectNode();
                resp.put("code", 0);
                resp.put("message", "upload success");
                ObjectNode data = resp.putObject("data");
                data.put("fileId", "FILE-" + UUID.randomUUID().toString().substring(0, 8));
                data.put("base64Verified", verified);
                responseJson = mapper.writeValueAsString(resp);
                statusCode = 200;
            } else {
                ObjectNode resp = mapper.createObjectNode();
                resp.put("code", 404);
                resp.put("message", "Mock endpoint not found: " + path);
                responseJson = mapper.writeValueAsString(resp);
                statusCode = 404;
            }
        } catch (Exception e) {
            log.error("Mock processing error, body was: {}, error: {}", bodyStr, e.getMessage(), e);
            ObjectNode resp = mapper.createObjectNode();
            resp.put("code", -1);
            resp.put("message", "Mock error: " + e.getMessage());
            responseJson = mapper.writeValueAsString(resp);
            statusCode = 500;
        }

        ResponseBody responseBody = ResponseBody.create(responseJson, JSON_MEDIA);
        return new Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(statusCode)
                .message("OK")
                .addHeader("Content-Type", "application/json")
                .body(responseBody)
                .build();
    }
}
