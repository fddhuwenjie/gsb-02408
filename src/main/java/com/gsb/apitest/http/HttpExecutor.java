package com.gsb.apitest.http;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * HTTP 执行器：基于 JDK 11+ 内置 HttpClient，避免引入 OkHttp / Apache HttpClient，
 * 减少依赖。框架不绑定具体客户端实现，后续可以通过 HttpExecutor 接口扩展。
 */
public class HttpExecutor {

    public interface Executor {
        HttpResponse execute(HttpRequest request) throws IOException, InterruptedException;
    }

    private static final ObjectMapper JSON = new ObjectMapper();

    private final HttpClient client;

    public HttpExecutor(int timeoutMs) {
        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(timeoutMs))
                .build();
    }

    public HttpResponse execute(HttpRequest request) throws IOException, InterruptedException {
        if (request.getUrl() == null || request.getUrl().isEmpty()) {
            throw new IllegalArgumentException("HttpRequest.url is required");
        }
        String fullUrl = appendQuery(request.getUrl(), request.getQueryParams());
        java.net.http.HttpRequest.Builder builder = java.net.http.HttpRequest.newBuilder()
                .uri(URI.create(fullUrl))
                .timeout(Duration.ofSeconds(30));

        java.net.http.HttpRequest.BodyPublisher publisher;
        if (request.getBody() == null || request.getBody().isEmpty()) {
            publisher = java.net.http.HttpRequest.BodyPublishers.noBody();
        } else {
            publisher = java.net.http.HttpRequest.BodyPublishers.ofString(
                    request.getBody(), StandardCharsets.UTF_8);
        }

        switch (request.getMethod()) {
            case GET:    builder.GET(); break;
            case POST:   builder.POST(publisher); break;
            case PUT:    builder.PUT(publisher); break;
            case DELETE: builder.DELETE(); break;
            case PATCH:  builder.method("PATCH", publisher); break;
            default: throw new IllegalArgumentException("unsupported method: " + request.getMethod());
        }

        for (Map.Entry<String, String> e : request.getHeaders().entrySet()) {
            builder.header(e.getKey(), e.getValue());
        }

        long start = System.currentTimeMillis();
        java.net.http.HttpResponse<String> resp = client.send(
                builder.build(), java.net.http.HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        long elapsed = System.currentTimeMillis() - start;

        String body = resp.body();
        Object json = tryParseJson(body);
        return new HttpResponse(resp.statusCode(), resp.headers().map(), body, json, elapsed);
    }

    private static String appendQuery(String url, Map<String, String> params) {
        if (params == null || params.isEmpty()) return url;
        StringBuilder sb = new StringBuilder(url);
        sb.append(url.contains("?") ? '&' : '?');
        boolean first = true;
        for (Map.Entry<String, String> e : params.entrySet()) {
            if (!first) sb.append('&');
            sb.append(URLEncoder.encode(e.getKey(), StandardCharsets.UTF_8))
              .append('=')
              .append(URLEncoder.encode(e.getValue() == null ? "" : e.getValue(), StandardCharsets.UTF_8));
            first = false;
        }
        return sb.toString();
    }

    private static Object tryParseJson(String body) {
        if (body == null || body.isEmpty()) return null;
        String trimmed = body.trim();
        if (!(trimmed.startsWith("{") || trimmed.startsWith("["))) return null;
        try {
            return JSON.readValue(body, Object.class);
        } catch (IOException e) {
            return null;
        }
    }

    public static Map<String, Object> emptyMap() {
        return new LinkedHashMap<>();
    }
}
