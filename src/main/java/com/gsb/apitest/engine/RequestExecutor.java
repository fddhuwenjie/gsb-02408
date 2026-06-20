package com.gsb.apitest.engine;

import com.gsb.apitest.config.TestConfiguration;
import com.gsb.apitest.model.ApiResponse;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class RequestExecutor {
    private static final Logger log = LoggerFactory.getLogger(RequestExecutor.class);
    private static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json; charset=utf-8");

    private final OkHttpClient httpClient;

    public RequestExecutor() {
        TestConfiguration config = TestConfiguration.getInstance();
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(config.getConnectTimeoutMs(), TimeUnit.MILLISECONDS)
                .readTimeout(config.getReadTimeoutMs(), TimeUnit.MILLISECONDS)
                .writeTimeout(config.getReadTimeoutMs(), TimeUnit.MILLISECONDS)
                .build();
    }

    public ApiResponse post(String url, String jsonBody) throws IOException {
        return post(url, jsonBody, new HashMap<>());
    }

    public ApiResponse post(String url, String jsonBody, Map<String, String> headers) throws IOException {
        log.info("发送POST请求: {}", url);
        log.debug("请求体: {}", jsonBody);

        Map<String, String> allHeaders = new HashMap<>(TestConfiguration.getInstance().getDefaultHeaders());
        allHeaders.putAll(headers);
        allHeaders.putIfAbsent("Content-Type", "application/json");

        RequestBody body = RequestBody.create(jsonBody == null ? "" : jsonBody, JSON_MEDIA_TYPE);
        Request.Builder requestBuilder = new Request.Builder().url(url).post(body);

        for (Map.Entry<String, String> entry : allHeaders.entrySet()) {
            requestBuilder.addHeader(entry.getKey(), entry.getValue());
        }

        long start = System.currentTimeMillis();
        try (Response response = httpClient.newCall(requestBuilder.build()).execute()) {
            long elapsed = System.currentTimeMillis() - start;

            ResponseBody responseBody = response.body();
            String bodyStr = responseBody != null ? responseBody.string() : "";

            Map<String, String> responseHeaders = new HashMap<>();
            for (String name : response.headers().names()) {
                responseHeaders.put(name, response.header(name));
            }

            ApiResponse result = ApiResponse.builder()
                    .statusCode(response.code())
                    .headers(responseHeaders)
                    .body(bodyStr)
                    .responseTimeMs(elapsed)
                    .build();

            log.info("响应状态: {}, 耗时: {}ms", result.getStatusCode(), elapsed);
            log.debug("响应体: {}", bodyStr);

            return result;
        }
    }

    public ApiResponse get(String url) throws IOException {
        return get(url, new HashMap<>());
    }

    public ApiResponse get(String url, Map<String, String> headers) throws IOException {
        log.info("发送GET请求: {}", url);

        Map<String, String> allHeaders = new HashMap<>(TestConfiguration.getInstance().getDefaultHeaders());
        allHeaders.putAll(headers);

        Request.Builder requestBuilder = new Request.Builder().url(url).get();

        for (Map.Entry<String, String> entry : allHeaders.entrySet()) {
            requestBuilder.addHeader(entry.getKey(), entry.getValue());
        }

        long start = System.currentTimeMillis();
        try (Response response = httpClient.newCall(requestBuilder.build()).execute()) {
            long elapsed = System.currentTimeMillis() - start;

            ResponseBody responseBody = response.body();
            String bodyStr = responseBody != null ? responseBody.string() : "";

            Map<String, String> responseHeaders = new HashMap<>();
            for (String name : response.headers().names()) {
                responseHeaders.put(name, response.header(name));
            }

            ApiResponse result = ApiResponse.builder()
                    .statusCode(response.code())
                    .headers(responseHeaders)
                    .body(bodyStr)
                    .responseTimeMs(elapsed)
                    .build();

            log.info("响应状态: {}, 耗时: {}ms", result.getStatusCode(), elapsed);
            return result;
        }
    }
}
