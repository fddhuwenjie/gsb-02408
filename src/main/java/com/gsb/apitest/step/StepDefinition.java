package com.gsb.apitest.step;

import com.gsb.apitest.assertion.ResponseAssertions;
import com.gsb.apitest.http.HttpMethod;

import java.util.Map;
import java.util.function.Consumer;

public class StepDefinition {

    private String name;
    private HttpMethod method = HttpMethod.POST;
    private String url;
    private String body;
    private String template;
    private Map<String, String> headers;
    private Map<String, String> queryParams;
    private int expectedStatusCode = -1;
    private VariableExtractor extractor;
    private Consumer<ResponseAssertions> assertionConsumer;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public HttpMethod getMethod() {
        return method;
    }

    public void setMethod(HttpMethod method) {
        this.method = method;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public Map<String, String> getQueryParams() {
        return queryParams;
    }

    public void setQueryParams(Map<String, String> queryParams) {
        this.queryParams = queryParams;
    }

    public int getExpectedStatusCode() {
        return expectedStatusCode;
    }

    public void setExpectedStatusCode(int expectedStatusCode) {
        this.expectedStatusCode = expectedStatusCode;
    }

    public VariableExtractor getExtractor() {
        return extractor;
    }

    public void setExtractor(VariableExtractor extractor) {
        this.extractor = extractor;
    }

    public Consumer<ResponseAssertions> getAssertionConsumer() {
        return assertionConsumer;
    }

    public void setAssertionConsumer(Consumer<ResponseAssertions> assertionConsumer) {
        this.assertionConsumer = assertionConsumer;
    }
}
