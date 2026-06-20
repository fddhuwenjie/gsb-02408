package com.gsb.apitest.http;

import java.util.List;
import java.util.Map;

/** HTTP 响应描述。bodyJson 为反序列化后的 JSON 对象（如果响应可解析）。 */
public class HttpResponse {

    private final int statusCode;
    private final Map<String, List<String>> headers;
    private final String body;
    private final Object bodyJson;
    private final long elapsedMs;

    public HttpResponse(int statusCode, Map<String, List<String>> headers,
                        String body, Object bodyJson, long elapsedMs) {
        this.statusCode = statusCode;
        this.headers = headers;
        this.body = body;
        this.bodyJson = bodyJson;
        this.elapsedMs = elapsedMs;
    }

    public int getStatusCode() { return statusCode; }
    public Map<String, List<String>> getHeaders() { return headers; }
    public String getBody() { return body; }
    public Object getBodyJson() { return bodyJson; }
    public long getElapsedMs() { return elapsedMs; }

    public boolean isSuccess() {
        return statusCode >= 200 && statusCode < 300;
    }
}
