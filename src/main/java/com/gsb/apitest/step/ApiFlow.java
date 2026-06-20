package com.gsb.apitest.step;

import com.gsb.apitest.assertion.ResponseAssertions;
import com.gsb.apitest.context.ContextHolder;
import com.gsb.apitest.context.TestContext;
import com.gsb.apitest.http.HttpMethod;
import com.gsb.apitest.http.HttpResponse;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

public class ApiFlow {

    private final StepDefinition stepDef = new StepDefinition();

    private ApiFlow(HttpMethod method, String url) {
        stepDef.setMethod(method);
        stepDef.setUrl(url);
    }

    public static ApiFlow post(String url) {
        return new ApiFlow(HttpMethod.POST, url);
    }

    public static ApiFlow get(String url) {
        return new ApiFlow(HttpMethod.GET, url);
    }

    public static ApiFlow put(String url) {
        return new ApiFlow(HttpMethod.PUT, url);
    }

    public static ApiFlow delete(String url) {
        return new ApiFlow(HttpMethod.DELETE, url);
    }

    public static ApiFlow patch(String url) {
        return new ApiFlow(HttpMethod.PATCH, url);
    }

    public static ApiFlow method(HttpMethod method, String url) {
        return new ApiFlow(method, url);
    }

    public ApiFlow name(String stepName) {
        stepDef.setName(stepName);
        return this;
    }

    public ApiFlow body(String jsonBody) {
        stepDef.setBody(jsonBody);
        return this;
    }

    public ApiFlow template(String templateName) {
        stepDef.setTemplate(templateName);
        return this;
    }

    public ApiFlow header(String key, String value) {
        if (stepDef.getHeaders() == null) {
            stepDef.setHeaders(new LinkedHashMap<>());
        }
        stepDef.getHeaders().put(key, value);
        return this;
    }

    public ApiFlow headers(Map<String, String> headers) {
        if (stepDef.getHeaders() == null) {
            stepDef.setHeaders(new LinkedHashMap<>());
        }
        stepDef.getHeaders().putAll(headers);
        return this;
    }

    public ApiFlow queryParam(String key, String value) {
        if (stepDef.getQueryParams() == null) {
            stepDef.setQueryParams(new LinkedHashMap<>());
        }
        stepDef.getQueryParams().put(key, value);
        return this;
    }

    public ApiFlow contentType(String contentType) {
        return header("Content-Type", contentType);
    }

    public ApiFlow authorization(String token) {
        return header("Authorization", token);
    }

    public ApiFlow bearerToken(String token) {
        return header("Authorization", "Bearer " + token);
    }

    public ApiFlow extract(String varName, String jsonPath) {
        if (stepDef.getExtractor() == null) {
            stepDef.setExtractor(new VariableExtractor());
        }
        stepDef.getExtractor().var(varName, jsonPath);
        return this;
    }

    public ApiFlow extractGlobal(String varName, String jsonPath) {
        if (stepDef.getExtractor() == null) {
            stepDef.setExtractor(new VariableExtractor());
        }
        stepDef.getExtractor().global().var(varName, jsonPath);
        return this;
    }

    public ApiFlow expectStatus(int statusCode) {
        stepDef.setExpectedStatusCode(statusCode);
        return this;
    }

    public ApiFlow expect(Consumer<ResponseAssertions> assertions) {
        stepDef.setAssertionConsumer(assertions);
        return this;
    }

    public HttpResponse execute() {
        TestContext context = ContextHolder.get();
        return context.getStepExecutor().execute(stepDef, context);
    }

    public static Object var(String varName) {
        return ContextHolder.get().resolveVariable(varName);
    }

    public static String varStr(String varName) {
        return ContextHolder.get().resolveVariableAsString(varName);
    }

    public static void setVar(String name, Object value) {
        TestContext ctx = ContextHolder.get();
        if (ctx.clazz() != null) {
            ctx.setClassVariable(name, value);
        } else {
            ctx.setGlobalVariable(name, value);
        }
    }

    public static TestContext context() {
        return ContextHolder.get();
    }

    public static HttpResponse lastResponse() {
        return ContextHolder.get().getLastResponse();
    }
}
