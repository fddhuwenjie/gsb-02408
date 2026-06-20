package com.gsb.apitest.context;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ScopedVariableStore {
    private final Map<String, Object> globalScope = new ConcurrentHashMap<>();
    private final Map<String, Object> suiteScope = new ConcurrentHashMap<>();
    private final ThreadLocal<Deque<Map<String, Object>>> testScopeStack = ThreadLocal.withInitial(ArrayDeque::new);
    private final ThreadLocal<Deque<Map<String, Object>>> stepScopeStack = ThreadLocal.withInitial(ArrayDeque::new);

    public void setGlobalVariable(String key, Object value) {
        globalScope.put(key, value);
    }

    public void setSuiteVariable(String key, Object value) {
        suiteScope.put(key, value);
    }

    public void pushTestScope() {
        testScopeStack.get().push(new ConcurrentHashMap<>());
    }

    public void popTestScope() {
        Deque<Map<String, Object>> stack = testScopeStack.get();
        if (!stack.isEmpty()) {
            stack.pop();
        }
    }

    public void setTestVariable(String key, Object value) {
        Deque<Map<String, Object>> stack = testScopeStack.get();
        if (stack.isEmpty()) {
            pushTestScope();
            stack = testScopeStack.get();
        }
        stack.peek().put(key, value);
    }

    public void pushStepScope() {
        stepScopeStack.get().push(new HashMap<>());
    }

    public void popStepScope() {
        Deque<Map<String, Object>> stack = stepScopeStack.get();
        if (!stack.isEmpty()) {
            stack.pop();
        }
    }

    public void setStepVariable(String key, Object value) {
        Deque<Map<String, Object>> stack = stepScopeStack.get();
        if (stack.isEmpty()) {
            pushStepScope();
            stack = stepScopeStack.get();
        }
        stack.peek().put(key, value);
    }

    public void setVariable(VariableScope scope, String key, Object value) {
        switch (scope) {
            case GLOBAL:
                setGlobalVariable(key, value);
                break;
            case SUITE:
                setSuiteVariable(key, value);
                break;
            case TEST:
                setTestVariable(key, value);
                break;
            case STEP:
                setStepVariable(key, value);
                break;
        }
    }

    public Object getVariable(String key) {
        Deque<Map<String, Object>> stepStack = stepScopeStack.get();
        for (Map<String, Object> scope : stepStack) {
            if (scope.containsKey(key)) {
                return scope.get(key);
            }
        }

        Deque<Map<String, Object>> testStack = testScopeStack.get();
        for (Map<String, Object> scope : testStack) {
            if (scope.containsKey(key)) {
                return scope.get(key);
            }
        }

        if (suiteScope.containsKey(key)) {
            return suiteScope.get(key);
        }

        if (globalScope.containsKey(key)) {
            return globalScope.get(key);
        }

        return null;
    }

    public <T> T getVariable(String key, Class<T> type) {
        Object value = getVariable(key);
        if (value == null) {
            return null;
        }
        if (type.isInstance(value)) {
            return type.cast(value);
        }
        if (type == String.class) {
            return type.cast(value.toString());
        }
        return null;
    }

    public String getVariableAsString(String key) {
        Object value = getVariable(key);
        return value != null ? value.toString() : null;
    }

    public boolean hasVariable(String key) {
        return getVariable(key) != null;
    }

    public Map<String, Object> getAllVariables() {
        Map<String, Object> result = new HashMap<>();
        result.putAll(globalScope);
        result.putAll(suiteScope);

        Deque<Map<String, Object>> testStack = testScopeStack.get();
        for (Map<String, Object> scope : testStack) {
            result.putAll(scope);
        }

        Deque<Map<String, Object>> stepStack = stepScopeStack.get();
        for (Map<String, Object> scope : stepStack) {
            result.putAll(scope);
        }

        return result;
    }

    public void clearStepScope() {
        stepScopeStack.get().clear();
    }

    public void clear() {
        globalScope.clear();
        suiteScope.clear();
        testScopeStack.get().clear();
        stepScopeStack.get().clear();
    }
}
