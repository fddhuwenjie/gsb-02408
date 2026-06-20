package com.gsb.apitest.context;

import com.gsb.apitest.config.TestConfiguration;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ExecutionContext {
    private static final ThreadLocal<ExecutionContext> CONTEXT_HOLDER = new ThreadLocal<>();

    private final String testId;
    private final Deque<Map<String, Object>> variableScopes;
    private final List<StepRecord> stepRecords;
    private long startTime;
    private Object lastResponse;

    ExecutionContext(String testId) {
        this.testId = testId;
        this.variableScopes = new ArrayDeque<>();
        this.stepRecords = Collections.synchronizedList(new ArrayList<>());
        this.startTime = System.currentTimeMillis();
        pushScope();
        for (Map.Entry<String, Object> entry : TestConfiguration.getInstance().getGlobalVariables().entrySet()) {
            set(entry.getKey(), entry.getValue());
        }
    }

    public static ExecutionContext create(String testId) {
        return new ExecutionContext(testId);
    }

    public static ExecutionContext getCurrent() {
        ExecutionContext ctx = CONTEXT_HOLDER.get();
        if (ctx == null) {
            ctx = new ExecutionContext(UUID.randomUUID().toString());
            CONTEXT_HOLDER.set(ctx);
        }
        return ctx;
    }

    public static void setCurrent(ExecutionContext ctx) {
        CONTEXT_HOLDER.set(ctx);
    }

    public static void clear() {
        CONTEXT_HOLDER.remove();
    }

    public String getTestId() { return testId; }
    public long getStartTime() { return startTime; }
    public Object getLastResponse() { return lastResponse; }

    public void pushScope() {
        variableScopes.push(new ConcurrentHashMap<>());
    }

    public void popScope() {
        if (variableScopes.size() > 1) {
            variableScopes.pop();
        }
    }

    public void set(String key, Object value) {
        variableScopes.peek().put(key, value);
    }

    public void setGlobal(String key, Object value) {
        variableScopes.peekLast().put(key, value);
    }

    public Object get(String key) {
        for (Map<String, Object> scope : variableScopes) {
            if (scope.containsKey(key)) {
                return scope.get(key);
            }
        }
        return null;
    }

    public String getString(String key) {
        Object val = get(key);
        return val == null ? null : String.valueOf(val);
    }

    public Map<String, Object> getAllVariables() {
        Map<String, Object> result = new LinkedHashMap<>();
        Iterator<Map<String, Object>> it = variableScopes.descendingIterator();
        while (it.hasNext()) {
            result.putAll(it.next());
        }
        return result;
    }

    public void recordStep(StepRecord record) {
        stepRecords.add(record);
        this.lastResponse = record.getResponse();
    }

    public List<StepRecord> getStepRecords() {
        return Collections.unmodifiableList(stepRecords);
    }

    public StepRecord getCurrentStep() {
        return stepRecords.isEmpty() ? null : stepRecords.get(stepRecords.size() - 1);
    }

    public long getElapsedMs() {
        return System.currentTimeMillis() - startTime;
    }

    public void reset() {
        this.startTime = System.currentTimeMillis();
        this.stepRecords.clear();
        this.variableScopes.clear();
        pushScope();
        for (Map.Entry<String, Object> entry : TestConfiguration.getInstance().getGlobalVariables().entrySet()) {
            set(entry.getKey(), entry.getValue());
        }
    }
}
