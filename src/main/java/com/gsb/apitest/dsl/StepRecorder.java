package com.gsb.apitest.dsl;

import com.gsb.apitest.context.ExecutionContext;
import com.gsb.apitest.model.StepResult;

import java.util.Map;

public class StepRecorder {

    public static void recordSuccess(ExecutionContext ctx, int statusCode,
                                      String requestBody, String responseBody,
                                      Map<String, String> requestHeaders,
                                      Map<String, String> responseHeaders) {
        String stepName = ctx.getCurrentStepName();
        int stepIndex = ctx.getCurrentStepIndex();
        StepResult result = StepResult.success(stepName, stepIndex, statusCode, requestBody, responseBody, requestHeaders, responseHeaders);
        ctx.addStepResult(result);
    }

    public static void recordFailure(ExecutionContext ctx, Throwable error, String requestBody) {
        String stepName = ctx.getCurrentStepName();
        int stepIndex = ctx.getCurrentStepIndex();
        StepResult result = StepResult.failure(stepName, stepIndex, error, requestBody);
        ctx.addStepResult(result);
    }
}
