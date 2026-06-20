package com.gsb.apitest.http;

import com.gsb.apitest.config.TestConfig;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ApiHttpClient {

    private static final Logger log = LoggerFactory.getLogger(ApiHttpClient.class);
    private static final MediaType JSON_MEDIA = MediaType.parse("application/json;charset=UTF-8");

    private final OkHttpClient client;
    private final TestConfig config;

    public ApiHttpClient(TestConfig config) {
        this.config = config;
        this.client = new OkHttpClient.Builder()
                .connectTimeout(config.getConnectTimeoutMs(), TimeUnit.MILLISECONDS)
                .readTimeout(config.getReadTimeoutMs(), TimeUnit.MILLISECONDS)
                .writeTimeout(config.getWriteTimeoutMs(), TimeUnit.MILLISECONDS)
                .followRedirects(true)
                .build();
    }

    public HttpResponse execute(HttpRequest apiRequest) {
        long start = System.currentTimeMillis();

        String url = buildUrl(apiRequest);
        Map<String, String> mergedHeaders = config.mergedHeaders(apiRequest.getHeaders());

        Request.Builder reqBuilder = new Request.Builder().url(url);

        for (Map.Entry<String, String> entry : mergedHeaders.entrySet()) {
            reqBuilder.addHeader(entry.getKey(), entry.getValue());
        }

        RequestBody okBody = buildRequestBody(apiRequest);
        reqBuilder.method(apiRequest.getMethod().name(), okBody);

        Request okRequest = reqBuilder.build();

        if (config.isLogRequest()) {
            log.info(">>> {} {}", apiRequest.getMethod(), url);
            mergedHeaders.forEach((k, v) -> log.debug(">>> {}: {}", k, v));
            if (apiRequest.getBody() != null) {
                log.debug(">>> Body: {}", truncate(apiRequest.getBody(), 2000));
            }
        }

        HttpResponse apiResponse = new HttpResponse();
        apiResponse.setRequestUrl(url);
        apiResponse.setRequestMethod(apiRequest.getMethod().name());
        apiResponse.setRequestBody(apiRequest.getBody());

        try (Response response = client.newCall(okRequest).execute()) {
            long elapsed = System.currentTimeMillis() - start;
            apiResponse.setStatusCode(response.code());
            apiResponse.setStatusMessage(response.message());
            apiResponse.setHeaders(response.headers().toMultimap());
            apiResponse.setResponseTimeMs(elapsed);

            if (response.body() != null) {
                String body = response.body().string();
                apiResponse.setBody(body);
            }

            if (config.isLogResponse()) {
                log.info("<<< {} {} ({}ms)", response.code(), response.message(), elapsed);
                if (apiResponse.getBody() != null) {
                    log.debug("<<< Body: {}", truncate(apiResponse.getBody(), 2000));
                }
            }

            return apiResponse;
        } catch (IOException e) {
            long elapsed = System.currentTimeMillis() - start;
            apiResponse.setResponseTimeMs(elapsed);
            throw new RuntimeException("HTTP请求失败: " + apiRequest.getMethod() + " " + url, e);
        }
    }

    private String buildUrl(HttpRequest request) {
        String url = config.resolveUrl(request.getUrl());
        Map<String, String> params = request.getQueryParams();
        if (params == null || params.isEmpty()) {
            return url;
        }
        StringBuilder sb = new StringBuilder(url);
        boolean hasQuery = url.contains("?");
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (hasQuery) {
                sb.append("&");
            } else {
                sb.append("?");
                hasQuery = true;
            }
            sb.append(entry.getKey()).append("=");
            if (entry.getValue() != null) {
                sb.append(entry.getValue());
            }
        }
        return sb.toString();
    }

    private RequestBody buildRequestBody(HttpRequest request) {
        String body = request.getBody();
        if (body == null) {
            if (request.getMethod() == HttpMethod.GET || request.getMethod() == HttpMethod.HEAD) {
                return null;
            }
            return RequestBody.create("", null);
        }
        String contentType = request.getHeaders().get("Content-Type");
        MediaType mediaType = contentType != null ? MediaType.parse(contentType) : JSON_MEDIA;
        return RequestBody.create(body, mediaType);
    }

    private String truncate(String s, int maxLen) {
        if (s == null) return null;
        if (s.length() <= maxLen) return s;
        return s.substring(0, maxLen) + "...(truncated)";
    }
}
