package com.gsb.apitest.dsl;

import com.gsb.apitest.asserts.AssertionEngine;
import com.gsb.apitest.context.ExecutionContext;
import com.gsb.apitest.context.VariableScope;
import com.gsb.apitest.extension.ApiTestExtension;
import com.gsb.apitest.extract.JsonPathExtractor;
import com.gsb.apitest.http.HttpClientWrapper;
import com.gsb.apitest.model.ApiResponse;
import com.gsb.apitest.template.TemplateRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class RequestBuilder {
    private static final Logger log = LoggerFactory.getLogger(RequestBuilder.class);
    private final String method;
    private String path;
    private String body;
    private final Map<String, String> headers = new HashMap<>();
    private final TemplateRenderer renderer = new TemplateRenderer();

    public RequestBuilder(String method, String path) {
        this.method = method;
        this.path = path;
    }

    public RequestBuilder body(String bodyTemplate) {
        this.body = renderer.renderWithGlobals(bodyTemplate);
        return this;
    }

    public RequestBuilder bodyFromTemplate(String templateResource) {
        this.body = renderer.renderFromClasspath(templateResource);
        return this;
    }

    public RequestBuilder bodyFromFile(String filePath) {
        this.body = renderer.renderFromFile(filePath);
        return this;
    }

    public RequestBuilder header(String name, String valueTemplate) {
        String resolvedValue = renderer.renderWithGlobals(valueTemplate);
        headers.put(name, resolvedValue);
        return this;
    }

    public RequestBuilder contentType(String contentType) {
        return header("Content-Type", contentType);
    }

    public RequestBuilder authorization(String token) {
        return header("Authorization", "Bearer " + token);
    }

    public RequestBuilder path(String pathTemplate) {
        this.path = renderer.renderWithGlobals(pathTemplate);
        return this;
    }

    public ResponseActions execute() {
        ExecutionContext ctx = ExecutionContext.getCurrent();
        String autoStepName = method + " " + path;
        Object stepNameFromBlock = ctx.getVariables().getVariable("_stepName");
        boolean needAutoStep = stepNameFromBlock == null;

        if (needAutoStep) {
            ctx.startStep(autoStepName);
        }
        try {
            HttpClientWrapper client = ApiTestExtension.getHttpClient();
            ApiResponse response;
            switch (method.toUpperCase()) {
                case "POST":
                    response = client.post(path, body, headers);
                    break;
                case "GET":
                    response = client.get(path, headers);
                    break;
                case "PUT":
                    response = client.put(path, body, headers);
                    break;
                case "DELETE":
                    response = client.delete(path, headers);
                    break;
                default:
                    response = client.executeRequest(method, path, body, headers);
            }

            if (needAutoStep) {
                ctx.endStepSuccess(response.getStatusCode(), body, response.getBody(), headers, response.getSingleValueHeaders());
            } else {
                StepRecorder.recordSuccess(ctx, response.getStatusCode(), body, response.getBody(), headers, response.getSingleValueHeaders());
            }

            JsonPathExtractor extractor = new JsonPathExtractor(response.getBody() != null ? response.getBody() : "{}");
            return new ResponseActions(response, extractor, ctx);
        } catch (Exception e) {
            if (needAutoStep) {
                ctx.endStepFailure(e, body);
            } else {
                StepRecorder.recordFailure(ctx, e, body);
            }
            throw new RuntimeException("API request failed at step '" + ctx.getCurrentStepName() + "': " + e.getMessage(), e);
        }
    }

    public AssertionEngine then() {
        return execute().then();
    }
}
