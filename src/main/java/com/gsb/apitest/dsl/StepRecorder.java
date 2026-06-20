package com.gsb.apitest.dsl;

import com.gsb.apitest.assertion.AssertionEngine;
import com.gsb.apitest.config.TestConfiguration;
import com.gsb.apitest.context.ExecutionContext;
import com.gsb.apitest.context.StepRecord;
import com.gsb.apitest.engine.RequestExecutor;
import com.gsb.apitest.engine.TemplateEngine;
import com.gsb.apitest.model.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class StepRecorder {
    private static final Logger log = LoggerFactory.getLogger(StepRecorder.class);

    private final ExecutionContext context;
    private final TemplateEngine templateEngine;
    private final RequestExecutor requestExecutor;
    private final AssertionEngine assertionEngine;
    private final AtomicInteger stepCounter;
    private final List<StepBuilder> steps;

    public StepRecorder(ExecutionContext context) {
        this.context = context;
        this.templateEngine = new TemplateEngine(context);
        this.requestExecutor = new RequestExecutor();
        this.assertionEngine = new AssertionEngine(context);
        this.stepCounter = new AtomicInteger(0);
        this.steps = new ArrayList<>();
    }

    public StepBuilder step(String name) {
        StepBuilder builder = new StepBuilder(this, name, stepCounter.incrementAndGet());
        steps.add(builder);
        return builder;
    }

    public StepBuilder step() {
        return step("Step-" + stepCounter.incrementAndGet());
    }

    public ExecutionContext getContext() {
        return context;
    }

    public TemplateEngine getTemplateEngine() {
        return templateEngine;
    }

    public AssertionEngine getAssertionEngine() {
        return assertionEngine;
    }

    ApiResponse execute(StepBuilder stepBuilder) throws IOException {
        String url = TestConfiguration.getInstance().getFullUrl(templateEngine.resolve(stepBuilder.url));
        String body = templateEngine.resolve(stepBuilder.body);
        Map<String, String> headers = new HashMap<>();
        for (Map.Entry<String, String> entry : stepBuilder.headers.entrySet()) {
            headers.put(entry.getKey(), templateEngine.resolve(entry.getValue()));
        }

        long start = System.currentTimeMillis();
        ApiResponse response = null;
        boolean success = true;
        String errorMsg = null;
        Map<String, Object> extracted = new LinkedHashMap<>();

        try {
            if ("POST".equalsIgnoreCase(stepBuilder.method)) {
                response = requestExecutor.post(url, body, headers);
            } else if ("GET".equalsIgnoreCase(stepBuilder.method)) {
                response = requestExecutor.get(url, headers);
            } else {
                throw new IllegalArgumentException("不支持的HTTP方法: " + stepBuilder.method);
            }

            for (Map.Entry<String, String> entry : stepBuilder.extractions.entrySet()) {
                String varName = entry.getKey();
                String jsonPath = entry.getValue();
                try {
                    Object value = response.jsonPath(jsonPath);
                    context.set(varName, value);
                    extracted.put(varName, value);
                    log.info("提取变量 {} = {} (从路径 {})", varName, value, jsonPath);
                } catch (Exception e) {
                    log.warn("提取变量失败 [{}] 路径 [{}]: {}", varName, jsonPath, e.getMessage());
                }
            }

            assertionEngine.executeAll(stepBuilder.assertions, response, stepBuilder.stepName);

        } catch (AssertionError e) {
            success = false;
            errorMsg = "断言失败: " + e.getMessage();
            throw e;
        } catch (Exception e) {
            success = false;
            errorMsg = e.getMessage();
            throw new RuntimeException("步骤 [" + stepBuilder.stepName + "] 执行失败: " + e.getMessage(), e);
        } finally {
            long duration = System.currentTimeMillis() - start;
            StepRecord record = StepRecord.builder()
                    .stepIndex(stepBuilder.stepIndex)
                    .stepName(stepBuilder.stepName)
                    .method(stepBuilder.method)
                    .url(url)
                    .headers(headers)
                    .requestBody(body)
                    .response(response)
                    .durationMs(duration)
                    .success(success)
                    .errorMessage(errorMsg)
                    .extractedVariables(extracted)
                    .build();
            context.recordStep(record);
            log.info("步骤 [{}] 完成, 耗时: {}ms, 成功: {}", stepBuilder.stepName, duration, success);
        }

        return response;
    }

    public static class StepBuilder {
        private final StepRecorder recorder;
        private final int stepIndex;
        private final String stepName;
        private String method = "POST";
        private String url;
        private String body;
        private ApiResponse response;
        private final Map<String, String> headers = new LinkedHashMap<>();
        private final Map<String, String> extractions = new LinkedHashMap<>();
        private final List<AssertionBuilder> assertions = new ArrayList<>();

        StepBuilder(StepRecorder recorder, String name, int index) {
            this.recorder = recorder;
            this.stepName = name;
            this.stepIndex = index;
        }

        public int getStepIndex() { return stepIndex; }
        public String getStepName() { return stepName; }
        public ApiResponse getResponse() { return response; }

        public StepBuilder name(String name) {
            return this;
        }

        public StepBuilder post(String url) {
            this.method = "POST";
            this.url = url;
            return this;
        }

        public StepBuilder get(String url) {
            this.method = "GET";
            this.url = url;
            return this;
        }

        public StepBuilder url(String url) {
            this.url = url;
            return this;
        }

        public StepBuilder body(String body) {
            this.body = body;
            return this;
        }

        public StepBuilder bodyTemplate(String templateName) throws IOException {
            this.body = recorder.getTemplateEngine().loadTemplate(templateName);
            return this;
        }

        public StepBuilder jsonBody(String json) {
            this.body = json;
            return this;
        }

        public StepBuilder header(String key, String value) {
            this.headers.put(key, value);
            return this;
        }

        public StepBuilder contentType(String contentType) {
            return header("Content-Type", contentType);
        }

        public StepBuilder extract(String varName, String jsonPath) {
            this.extractions.put(varName, jsonPath);
            return this;
        }

        public AssertionBuilder assertThat(String jsonPath) {
            AssertionBuilder assertion = new AssertionBuilder(this, jsonPath);
            this.assertions.add(assertion);
            return assertion;
        }

        public StepBuilder expectStatus(int expectedStatus) {
            assertThat("$.statusCode").equalsTo(expectedStatus);
            return this;
        }

        public StepBuilder execute() throws IOException {
            this.response = recorder.execute(this);
            return this;
        }

        public StepRecorder and() {
            return recorder;
        }
    }

    public static class AssertionBuilder {
        private final StepBuilder stepBuilder;
        private final String jsonPath;
        private Object expectedValue;
        private String assertionType;

        AssertionBuilder(StepBuilder stepBuilder, String jsonPath) {
            this.stepBuilder = stepBuilder;
            this.jsonPath = jsonPath;
        }

        public StepBuilder equalsTo(Object expected) {
            this.expectedValue = expected;
            this.assertionType = "equals";
            return stepBuilder;
        }

        public StepBuilder notNull() {
            this.assertionType = "notNull";
            return stepBuilder;
        }

        public StepBuilder isNull() {
            this.assertionType = "isNull";
            return stepBuilder;
        }

        public StepBuilder contains(String substring) {
            this.expectedValue = substring;
            this.assertionType = "contains";
            return stepBuilder;
        }

        public StepBuilder greaterThan(Number value) {
            this.expectedValue = value;
            this.assertionType = "greaterThan";
            return stepBuilder;
        }

        public StepBuilder lessThan(Number value) {
            this.expectedValue = value;
            this.assertionType = "lessThan";
            return stepBuilder;
        }

        public String getJsonPath() {
            return jsonPath;
        }

        public Object getExpectedValue() {
            return expectedValue;
        }

        public String getAssertionType() {
            return assertionType;
        }
    }
}
