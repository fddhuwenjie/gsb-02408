package com.gsb.apitest.mock;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MockApiServer {
    private static final Logger log = LoggerFactory.getLogger(MockApiServer.class);
    private static final ObjectMapper mapper = new ObjectMapper();
    private HttpServer server;
    private int port;
    private String baseUrl;

    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        port = server.getAddress().getPort();
        baseUrl = "http://localhost:" + port;

        server.createContext("/api/login", new LoginHandler());
        server.createContext("/api/user/info", new UserInfoHandler());
        server.createContext("/api/order/create", new OrderCreateHandler());
        server.createContext("/api/upload", new UploadHandler());
        server.createContext("/api/echo", new EchoHandler());

        server.setExecutor(null);
        server.start();
        log.info("Mock API Server started at {}", baseUrl);
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
            log.info("Mock API Server stopped");
        }
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public int getPort() {
        return port;
    }

    private static void sendJson(HttpExchange exchange, int status, Object response) throws IOException {
        String json = mapper.writeValueAsString(response);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        byte[] bytes = json.getBytes("UTF-8");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static String readBody(HttpExchange exchange) throws IOException {
        try (InputStream is = exchange.getRequestBody()) {
            return new String(is.readAllBytes(), "UTF-8");
        }
    }

    static class LoginHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJson(exchange, 405, Map.of("error", "Method Not Allowed"));
                return;
            }
            String body = readBody(exchange);
            JsonNode json = mapper.readTree(body);
            String username = json.path("username").asText("");
            String password = json.path("password").asText("");

            if ("admin".equals(username) && "123456".equals(password)) {
                String token = "Bearer " + UUID.randomUUID().toString().replace("-", "");
                Map<String, Object> resp = new HashMap<>();
                resp.put("code", 0);
                resp.put("message", "success");
                Map<String, Object> data = new HashMap<>();
                data.put("token", token);
                data.put("userId", 10001);
                data.put("username", username);
                resp.put("data", data);
                sendJson(exchange, 200, resp);
            } else {
                sendJson(exchange, 200, Map.of(
                        "code", 401,
                        "message", "用户名或密码错误"
                ));
            }
        }
    }

    static class UserInfoHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String auth = exchange.getRequestHeaders().getFirst("Authorization");
            if (auth == null || !auth.startsWith("Bearer ")) {
                sendJson(exchange, 200, Map.of("code", 401, "message", "未登录"));
                return;
            }
            Map<String, Object> resp = new HashMap<>();
            resp.put("code", 0);
            resp.put("message", "success");
            Map<String, Object> data = new HashMap<>();
            data.put("userId", 10001);
            data.put("username", "admin");
            data.put("email", "admin@example.com");
            data.put("roles", new String[]{"admin", "user"});
            resp.put("data", data);
            sendJson(exchange, 200, resp);
        }
    }

    static class OrderCreateHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String body = readBody(exchange);
            JsonNode json = mapper.readTree(body);
            String productId = json.path("productId").asText("");
            int amount = json.path("amount").asInt(0);
            String token = json.path("token").asText("");

            Map<String, Object> resp = new HashMap<>();
            resp.put("code", 0);
            resp.put("message", "success");
            Map<String, Object> data = new HashMap<>();
            data.put("orderId", "ORD" + System.currentTimeMillis());
            data.put("productId", productId);
            data.put("amount", amount);
            data.put("totalPrice", amount * 99.0);
            data.put("status", "created");
            resp.put("data", data);
            sendJson(exchange, 200, resp);
        }
    }

    static class UploadHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String body = readBody(exchange);
            JsonNode json = mapper.readTree(body);
            String fileBase64 = json.path("file").asText("");
            String fileName = json.path("fileName").asText("unknown");

            Map<String, Object> resp = new HashMap<>();
            resp.put("code", 0);
            resp.put("message", "success");
            Map<String, Object> data = new HashMap<>();
            data.put("fileId", "FILE" + UUID.randomUUID().toString().substring(0, 8));
            data.put("fileName", fileName);
            data.put("fileSize", fileBase64.length());
            data.put("status", "uploaded");
            resp.put("data", data);
            sendJson(exchange, 200, resp);
        }
    }

    static class EchoHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String body = readBody(exchange);
            Map<String, Object> resp = new HashMap<>();
            resp.put("code", 0);
            resp.put("method", exchange.getRequestMethod());
            resp.put("path", exchange.getRequestURI().getPath());
            Map<String, String> headerMap = new HashMap<>();
            for (String name : exchange.getRequestHeaders().keySet()) {
                headerMap.put(name, exchange.getRequestHeaders().getFirst(name));
            }
            resp.put("headers", headerMap);
            resp.put("body", body);
            sendJson(exchange, 200, resp);
        }
    }
}
