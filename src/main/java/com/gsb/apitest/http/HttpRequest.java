package com.gsb.apitest.http;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** 业务侧构造的请求描述，未做模板渲染前的原始结构。 */
public class HttpRequest {

    public enum Method { GET, POST, PUT, DELETE, PATCH }

    private Method method = Method.POST;
    private String url;
    private final Map<String, String> headers = new LinkedHashMap<>();
    private final Map<String, String> queryParams = new LinkedHashMap<>();
    private String body;

    public Method getMethod() { return method; }
    public HttpRequest setMethod(Method method) { this.method = method; return this; }

    public String getUrl() { return url; }
    public HttpRequest setUrl(String url) { this.url = url; return this; }

    public Map<String, String> getHeaders() { return headers; }
    public HttpRequest header(String name, String value) {
        headers.put(name, value);
        return this;
    }

    public Map<String, String> getQueryParams() { return queryParams; }
    public HttpRequest query(String name, String value) {
        queryParams.put(name, value);
        return this;
    }

    public String getBody() { return body; }
    public HttpRequest setBody(String body) { this.body = body; return this; }

    public Map<String, String> headersUnmodifiable() {
        return Collections.unmodifiableMap(headers);
    }
}
