package com.gsb.apitest.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 仅用于测试的内嵌 HTTP 服务器。提供以下端点：
 * <ul>
 *     <li>POST /echo —— 回写请求体里的 JSON，并附带一个递增 sequence</li>
 *     <li>POST /login —— 校验 username/password，返回 token</li>
 *     <li>POST /upload —— 接收 base64，返回长度与摘要</li>
 *     <li>GET  /me   —— 校验 Authorization header 并返回 userId</li>
 * </ul>
 */
public final class MockServer implements AutoCloseable {

    private static final ObjectMapper JSON = new ObjectMapper();

    private final HttpServer server;
    private final int port;
    private final AtomicInteger seq = new AtomicInteger(0);

    public MockServer() throws IOException {
        this.server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        this.port = server.getAddress().getPort();
        register();
        server.start();
    }

    public String baseUrl() {
        return "http://127.0.0.1:" + port;
    }

    private void register() {
        server.createContext("/echo", this::handleEcho);
        server.createContext("/login", this::handleLogin);
        server.createContext("/upload", this::handleUpload);
        server.createContext("/me", this::handleMe);
    }

    private void handleEcho(HttpExchange ex) throws IOException {
        String body = readBody(ex);
        Object parsed = body.isEmpty() ? null : JSON.readValue(body, Object.class);
        Map<String, Object> resp = new HashMap<>();
        resp.put("echo", parsed);
        resp.put("sequence", seq.incrementAndGet());
        resp.put("contentType", first(ex.getRequestHeaders().getOrDefault("Content-Type", java.util.Collections.emptyList())));
        write(ex, 200, resp);
    }

    private void handleLogin(HttpExchange ex) throws IOException {
        String body = readBody(ex);
        @SuppressWarnings("unchecked")
        Map<String, Object> req = body.isEmpty() ? new HashMap<>() : JSON.readValue(body, Map.class);
        Map<String, Object> resp = new HashMap<>();
        if ("alice".equals(req.get("username")) && "secret".equals(req.get("password"))) {
            resp.put("token", "tk-" + System.currentTimeMillis());
            resp.put("userId", "u-100");
            resp.put("expiresIn", 3600);
            write(ex, 200, resp);
        } else {
            resp.put("error", "invalid_credentials");
            write(ex, 401, resp);
        }
    }

    private void handleUpload(HttpExchange ex) throws IOException {
        String body = readBody(ex);
        @SuppressWarnings("unchecked")
        Map<String, Object> req = body.isEmpty() ? new HashMap<>() : JSON.readValue(body, Map.class);
        String content = (String) req.getOrDefault("content", "");
        Map<String, Object> resp = new HashMap<>();
        resp.put("name", req.get("name"));
        resp.put("contentLength", content.length());
        resp.put("checksum", Integer.toHexString(content.hashCode()));
        write(ex, 200, resp);
    }

    private void handleMe(HttpExchange ex) throws IOException {
        String auth = first(ex.getRequestHeaders().getOrDefault("Authorization", java.util.Collections.emptyList()));
        Map<String, Object> resp = new HashMap<>();
        if (auth == null || !auth.startsWith("Bearer ")) {
            resp.put("error", "missing_token");
            write(ex, 401, resp);
            return;
        }
        resp.put("token", auth.substring("Bearer ".length()));
        resp.put("userId", "u-100");
        resp.put("name", "Alice");
        write(ex, 200, resp);
    }

    private static String readBody(HttpExchange ex) throws IOException {
        try (InputStream in = ex.getRequestBody()) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static String first(java.util.List<String> list) {
        return (list == null || list.isEmpty()) ? null : list.get(0);
    }

    private static void write(HttpExchange ex, int status, Object body) throws IOException {
        byte[] data = JSON.writeValueAsBytes(body);
        ex.getResponseHeaders().add("Content-Type", "application/json");
        ex.sendResponseHeaders(status, data.length);
        ex.getResponseBody().write(data);
        ex.getResponseBody().close();
    }

    @Override
    public void close() {
        server.stop(0);
    }
}
