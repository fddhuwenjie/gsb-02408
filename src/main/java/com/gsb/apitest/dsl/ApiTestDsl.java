package com.gsb.apitest.dsl;

import com.gsb.apitest.context.ExecutionContext;
import com.gsb.apitest.context.VariableScope;

import java.util.function.Consumer;

public final class ApiTestDsl {

    private ApiTestDsl() {
    }

    public static RequestBuilder post(String path) {
        return new RequestBuilder("POST", path);
    }

    public static RequestBuilder get(String path) {
        return new RequestBuilder("GET", path);
    }

    public static RequestBuilder put(String path) {
        return new RequestBuilder("PUT", path);
    }

    public static RequestBuilder delete(String path) {
        return new RequestBuilder("DELETE", path);
    }

    public static void step(String stepName, Runnable stepAction) {
        ExecutionContext ctx = ExecutionContext.getCurrent();
        ctx.getVariables().pushStepScope();
        ctx.setCurrentStepForBlock(stepName);
        try {
            stepAction.run();
        } catch (Exception e) {
            StepRecorder.recordFailure(ctx, e, null);
            throw e;
        } finally {
            ctx.getVariables().popStepScope();
        }
    }

    public static void prepare(Runnable setupAction) {
        setupAction.run();
    }

    public static void setVar(String key, Object value) {
        setVar(VariableScope.TEST, key, value);
    }

    public static void setVar(VariableScope scope, String key, Object value) {
        ExecutionContext ctx = ExecutionContext.getCurrent();
        ctx.setVariable(scope, key, value);
    }

    public static String getVar(String key) {
        ExecutionContext ctx = ExecutionContext.getCurrent();
        return ctx.getVariableAsString(key);
    }

    public static <T> T getVar(String key, Class<T> type) {
        ExecutionContext ctx = ExecutionContext.getCurrent();
        return ctx.getVariable(key, type);
    }

    public static Object varValue(String key) {
        ExecutionContext ctx = ExecutionContext.getCurrent();
        return ctx.getVariable(key);
    }

    public static void setGlobalVar(String key, Object value) {
        setVar(VariableScope.GLOBAL, key, value);
    }

    public static void setSuiteVar(String key, Object value) {
        setVar(VariableScope.SUITE, key, value);
    }
}
