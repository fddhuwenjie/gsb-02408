package com.gsb.apitest.http;

import com.gsb.apitest.config.TestConfig;
import com.gsb.apitest.context.ExecutionContext;
import com.gsb.apitest.model.ApiResponse;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class HttpClientWrapper {
    private static final Logger log = LoggerFactory.getLogger(HttpClientWrapper.class);
    private static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json; charset=utf-8");
    private final OkHttpClient httpClient;
    private final TestConfig config;

    public HttpClientWrapper(TestConfig config) {
        this(config, new ArrayList<>());
    }

    public HttpClientWrapper(TestConfig config, List<Interceptor> interceptors) {
        this.config = config;
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(config.getConnectTimeout(), TimeUnit.MILLISECONDS)
                .readTimeout(config.getReadTimeout(), TimeUnit.MILLISECONDS)
                .writeTimeout(config.getReadTimeout(), TimeUnit.MILLISECONDS)
                .followRedirects(true);
        for (Interceptor interceptor : interceptors) {
            builder.addInterceptor(interceptor);
        }
        this.httpClient = builder.build();
    }

    public static Builder custom(TestConfig config) {
        return new Builder(config);
    }

    public static class Builder {
        private final TestConfig config;
        private final List<Interceptor> interceptors = new ArrayList<>();

        Builder(TestConfig config) {
            this.config = config;
        }

        public Builder addInterceptor(Interceptor interceptor) {
            interceptors.add(interceptor);
            return this;
        }

        public HttpClientWrapper build() {
            return new HttpClientWrapper(config, interceptors);
        }
    }

    public ApiResponse post(String url, String jsonBody) throws IOException {
        return post(url, jsonBody, new HashMap<>());
    }

    public ApiResponse post(String url, String jsonBody, Map<String, String> headers) throws IOException {
        return executeRequest("POST", url, jsonBody, headers);
    }

    public ApiResponse get(String url) throws IOException {
        return get(url, new HashMap<>());
    }

    public ApiResponse get(String url, Map<String, String> headers) throws IOException {
        return executeRequest("GET", url, null, headers);
    }

    public ApiResponse put(String url, String jsonBody) throws IOException {
        return put(url, jsonBody, new HashMap<>());
    }

    public ApiResponse put(String url, String jsonBody, Map<String, String> headers) throws IOException {
        return executeRequest("PUT", url, jsonBody, headers);
    }

    public ApiResponse delete(String url) throws IOException {
        return delete(url, new HashMap<>());
    }

    public ApiResponse delete(String url, Map<String, String> headers) throws IOException {
        return executeRequest("DELETE", url, null, headers);
    }

    public ApiResponse executeRequest(String method, String path, String body, Map<String, String> headers) throws IOException {
        String fullUrl = buildFullUrl(path);
        log.info("Executing {} {} (body length: {})", method, fullUrl, body != null ? body.length() : 0);

        Request.Builder requestBuilder = new Request.Builder().url(fullUrl);

        Map<String, String> allHeaders = new HashMap<>();
        if (config.getGlobalHeaders() != null) {
            allHeaders.putAll(config.getGlobalHeaders());
        }
        if (headers != null) {
            allHeaders.putAll(headers);
        }
        allHeaders.putIfAbsent("Content-Type", "application/json");
        allHeaders.putIfAbsent("Accept", "application/json");

        for (Map.Entry<String, String> entry : allHeaders.entrySet()) {
            requestBuilder.addHeader(entry.getKey(), entry.getValue());
        }

        RequestBody requestBody = null;
        if (body != null && !body.isEmpty() && ("POST".equals(method) || "PUT".equals(method) || "PATCH".equals(method))) {
            requestBody = RequestBody.create(body, JSON_MEDIA_TYPE);
        }
        requestBuilder.method(method, requestBody);

        long start = System.currentTimeMillis();
        try (Response response = httpClient.newCall(requestBuilder.build()).execute()) {
            long elapsed = System.currentTimeMillis() - start;
            String responseBody = response.body() != null ? response.body().string() : "";
            log.info("Response received: {} in {}ms", response.code(), elapsed);
            return new ApiResponse(response.code(), responseBody, response.headers().toMultimap(), elapsed);
        }
    }

    private String buildFullUrl(String path) {
        if (path.startsWith("http://") || path.startsWith("https://")) {
            return path;
        }
        String base = config.getBaseUrl() != null ? config.getBaseUrl() : "";
        if (base.endsWith("/") && path.startsWith("/")) {
            return base + path.substring(1);
        }
        if (!base.endsWith("/") && !path.startsWith("/")) {
            return base + "/" + path;
        }
        return base + path;
    }

    public OkHttpClient getRawClient() {
        return httpClient;
    }
}
