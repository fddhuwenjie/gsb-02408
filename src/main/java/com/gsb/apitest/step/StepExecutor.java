package com.gsb.apitest.step;

import com.gsb.apitest.assertion.ResponseAssertions;
import com.gsb.apitest.config.TestConfig;
import com.gsb.apitest.context.ContextHolder;
import com.gsb.apitest.context.TestContext;
import com.gsb.apitest.http.ApiHttpClient;
import com.gsb.apitest.http.HttpMethod;
import com.gsb.apitest.http.HttpRequest;
import com.gsb.apitest.http.HttpResponse;
import com.gsb.apitest.report.ReportCollector;
import com.gsb.apitest.report.StepRecord;
import com.gsb.apitest.template.TemplateEngine;
import com.gsb.apitest.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class StepExecutor {

    private static final Logger log = LoggerFactory.getLogger(StepExecutor.class);
    private static final AtomicInteger STEP_COUNTER = new AtomicInteger(0);

    private final TemplateEngine templateEngine;
    private final ApiHttpClient httpClient;
    private final ReportCollector reportCollector;

    public StepExecutor(TemplateEngine templateEngine, ApiHttpClient httpClient, ReportCollector reportCollector) {
        this.templateEngine = templateEngine;
        this.httpClient = httpClient;
        this.reportCollector = reportCollector;
    }

    public HttpResponse execute(StepDefinition stepDef, TestContext context) {
        String stepName = stepDef.getName() != null ? stepDef.getName() : "step-" + STEP_COUNTER.incrementAndGet();
        int stepIndex = STEP_COUNTER.incrementAndGet();
        context.enterStep(stepName, stepIndex);

        StepRecord record = new StepRecord(stepName, stepDef.getMethod().name(), stepDef.getUrl());
        long start = System.currentTimeMillis();

        try {
            String body = resolveBody(stepDef, context);
            record.setRequestBody(body);

            String url = templateEngine.render(stepDef.getUrl(), context);
            Map<String, String> headers = new LinkedHashMap<>();
            if (stepDef.getHeaders() != null) {
                for (Map.Entry<String, String> h : stepDef.getHeaders().entrySet()) {
                    headers.put(h.getKey(), templateEngine.render(h.getValue(), context));
                }
            }

            HttpRequest request = HttpRequest.builder()
                    .method(stepDef.getMethod())
                    .url(url)
                    .headers(headers)
                    .body(body)
                    .build();

            if (stepDef.getQueryParams() != null) {
                for (Map.Entry<String, String> q : stepDef.getQueryParams().entrySet()) {
                    request.addQueryParam(templateEngine.render(q.getKey(), context),
                            templateEngine.render(q.getValue(), context));
                }
            }

            log.info("━━━ 步骤[{}] {} {}", stepIndex, stepName, request.getMethod());
            HttpResponse response = httpClient.execute(request);

            context.setLastResponse(response);
            record.setStatusCode(response.getStatusCode());
            record.setResponseTimeMs(response.getResponseTimeMs());
            record.setResponseBody(response.getBody());
            record.setSuccess(true);

            if (stepDef.getExtractor() != null) {
                stepDef.getExtractor().extract(response, context);
                record.setExtractions(stepDef.getExtractor().getExtractions());
            }

            ResponseAssertions assertions = new ResponseAssertions(response, stepName);
            if (stepDef.getExpectedStatusCode() > 0) {
                assertions.statusCode(stepDef.getExpectedStatusCode());
            }
            if (stepDef.getAssertionConsumer() != null) {
                stepDef.getAssertionConsumer().accept(assertions);
            }
            record.setAssertionTotal(assertions.totalCount());
            record.setAssertionPassed(assertions.passCount());
            record.setAssertionFailed(assertions.failCount());

            long elapsed = System.currentTimeMillis() - start;
            record.setTotalTimeMs(elapsed);
            reportCollector.recordStep(record);

            log.info("━━━ 步骤[{}] {} 完成: {} ({}ms, 断言 {}/{})",
                    stepIndex, stepName, response.getStatusCode(), response.getResponseTimeMs(),
                    assertions.passCount(), assertions.totalCount());

            return response;
        } catch (Exception e) {
            long elapsed = System.currentTimeMillis() - start;
            record.setTotalTimeMs(elapsed);
            record.setSuccess(false);
            record.setErrorMessage(e.getMessage());
            reportCollector.recordStep(record);
            log.error("━━━ 步骤[{}] {} 失败: {}", stepIndex, stepName, e.getMessage());
            throw e;
        } finally {
            context.exitStep();
        }
    }

    private String resolveBody(StepDefinition stepDef, TestContext context) {
        if (stepDef.getBody() != null) {
            return templateEngine.render(stepDef.getBody(), context);
        }
        if (stepDef.getTemplate() != null) {
            return templateEngine.renderTemplate(stepDef.getTemplate(), context);
        }
        return null;
    }
}
