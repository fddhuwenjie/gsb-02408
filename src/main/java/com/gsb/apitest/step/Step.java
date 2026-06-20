package com.gsb.apitest.step;

import com.gsb.apitest.assertion.ResponseAssert;
import com.gsb.apitest.config.ConfigManager;
import com.gsb.apitest.context.TestContext;
import com.gsb.apitest.extract.FieldExtractor;
import com.gsb.apitest.http.HttpExecutor;
import com.gsb.apitest.http.HttpRequest;
import com.gsb.apitest.http.HttpResponse;
import com.gsb.apitest.report.StepRecord;
import com.gsb.apitest.template.TemplateRenderer;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * 单个测试步骤构造器：业务测试同学的主要入口。它做四件事：
 *   1. 让用户描述请求（method / url / header / body / 模板）
 *   2. 在执行前对模板做变量替换 + 函数求值
 *   3. 发送请求后允许从请求/响应中抽字段并写回上下文
 *   4. 允许追加断言，并把整个过程写入 StepRecord 报告
 *
 * 这一层与具体 HTTP 库解耦：未来要换 OkHttp / 添加 mTLS，只改 HttpExecutor。
 */
public class Step {

    private final String name;
    private final TestContext ctx;
    private final TemplateRenderer renderer;
    private final HttpExecutor executor;
    private final HttpRequest request = new HttpRequest();

    private final Map<String, String> requestExtractors = new LinkedHashMap<>();
    private final Map<String, String> responseExtractors = new LinkedHashMap<>();

    private Consumer<ResponseAssert> assertions;
    private boolean expectFailure = false;

    public Step(String name, TestContext ctx, TemplateRenderer renderer, HttpExecutor executor) {
        this.name = name;
        this.ctx = ctx;
        this.renderer = renderer;
        this.executor = executor;
        // 自动应用默认 header
        for (Map.Entry<String, String> e : ctx.config().getDefaultHeaders().entrySet()) {
            request.header(e.getKey(), e.getValue());
        }
    }

    public Step post(String url) { request.setMethod(HttpRequest.Method.POST).setUrl(url); return this; }
    public Step get(String url)  { request.setMethod(HttpRequest.Method.GET).setUrl(url);  return this; }
    public Step put(String url)  { request.setMethod(HttpRequest.Method.PUT).setUrl(url);  return this; }
    public Step delete(String url){request.setMethod(HttpRequest.Method.DELETE).setUrl(url); return this; }

    public Step header(String name, String value) {
        request.header(name, value);
        return this;
    }

    public Step query(String name, String value) {
        request.query(name, value);
        return this;
    }

    public Step body(String body) {
        request.setBody(body);
        return this;
    }

    public Step bodyFromTemplate(String classpathResource) {
        request.setBody(TemplateRenderer.loadTemplate(classpathResource));
        return this;
    }

    /** 把请求体里 jsonPath 抽出的值保存为变量。常用于回写需要复用的 requestId 等。 */
    public Step extractFromRequest(String varName, String jsonPath) {
        requestExtractors.put(varName, jsonPath);
        return this;
    }

    public Step extractFromResponse(String varName, String jsonPath) {
        responseExtractors.put(varName, jsonPath);
        return this;
    }

    public Step assertThat(Consumer<ResponseAssert> assertions) {
        this.assertions = assertions;
        return this;
    }

    /** 标记本步骤“预期失败”，业务侧用来覆盖错误码场景。 */
    public Step expectFailure() {
        this.expectFailure = true;
        return this;
    }

    /**
     * 真正执行步骤。无论成功失败，都会写一条 StepRecord 到上下文里。
     * 失败时抛 AssertionError / RuntimeException，让 JUnit 5 直接标记用例失败。
     */
    public HttpResponse run() {
        StepRecord record = new StepRecord(name);
        ctx.recordStep(record);
        long start = System.currentTimeMillis();
        try {
            // 1. 渲染模板
            String url = renderer.render(joinBaseUrl(request.getUrl()), ctx);
            String body = renderer.render(request.getBody(), ctx);
            Map<String, String> headers = new LinkedHashMap<>();
            for (Map.Entry<String, String> e : request.getHeaders().entrySet()) {
                headers.put(e.getKey(), renderer.render(e.getValue(), ctx));
            }
            HttpRequest finalRequest = new HttpRequest()
                    .setMethod(request.getMethod())
                    .setUrl(url)
                    .setBody(body);
            headers.forEach(finalRequest::header);
            request.getQueryParams().forEach((k, v) ->
                    finalRequest.query(k, renderer.render(v, ctx)));

            record.setRequestMethod(finalRequest.getMethod().name());
            record.setRequestUrl(finalRequest.getUrl());
            record.setRequestHeaders(finalRequest.headersUnmodifiable());
            record.setRequestBody(finalRequest.getBody());

            // 2. 提取请求体里的字段（在发请求前做即可，body 已渲染）
            for (Map.Entry<String, String> e : requestExtractors.entrySet()) {
                Object v = FieldExtractor.extract(finalRequest.getBody(), e.getValue());
                ctx.setVar(e.getKey(), v);
                record.getExtractedVars().put(e.getKey(), v);
            }

            // 3. 真正发送
            HttpResponse response = executor.execute(finalRequest);
            record.setResponseStatus(response.getStatusCode());
            record.setResponseBody(response.getBody());

            // 4. 提取响应字段
            for (Map.Entry<String, String> e : responseExtractors.entrySet()) {
                Object v = FieldExtractor.extract(response.getBodyJson(), e.getValue());
                ctx.setVar(e.getKey(), v);
                record.getExtractedVars().put(e.getKey(), v);
            }

            // 5. 断言
            if (assertions != null) {
                ResponseAssert ra = ResponseAssert.of(response);
                assertions.accept(ra);
                if (!ra.failures().isEmpty()) {
                    record.setAssertionFailures(ra.failures());
                    record.setStatus(StepRecord.Status.FAILED);
                    if (!expectFailure) {
                        ra.verify();
                    }
                }
            }
            if (expectFailure && response.isSuccess()) {
                record.setStatus(StepRecord.Status.FAILED);
                record.setErrorMessage("expected failure but request succeeded with "
                        + response.getStatusCode());
                throw new AssertionError(record.getErrorMessage());
            }
            return response;
        } catch (RuntimeException re) {
            if (record.getStatus() == StepRecord.Status.PASSED) {
                record.setStatus(StepRecord.Status.ERROR);
            }
            record.setErrorMessage(re.getMessage());
            throw re;
        } catch (Exception e) {
            record.setStatus(StepRecord.Status.ERROR);
            record.setErrorMessage(e.getMessage());
            throw new RuntimeException("step [" + name + "] failed: " + e.getMessage(), e);
        } finally {
            record.setElapsedMs(System.currentTimeMillis() - start);
        }
    }

    private String joinBaseUrl(String url) {
        if (url == null) return null;
        if (url.startsWith("http://") || url.startsWith("https://")) return url;
        String base = ConfigManager.getInstance().getBaseUrl();
        if (base == null || base.isEmpty()) return url;
        if (base.endsWith("/") && url.startsWith("/")) {
            return base + url.substring(1);
        }
        if (!base.endsWith("/") && !url.startsWith("/")) {
            return base + "/" + url;
        }
        return base + url;
    }
}
