package com.gsb.apitest.context;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class VariableScope {

    private final String name;
    private final VariableScope parent;
    private final Map<String, Object> variables = new LinkedHashMap<>();

    public VariableScope(String name) {
        this(name, null);
    }

    public VariableScope(String name, VariableScope parent) {
        this.name = name;
        this.parent = parent;
    }

    public String getName() {
        return name;
    }

    public VariableScope getParent() {
        return parent;
    }

    public void set(String key, Object value) {
        variables.put(key, value);
    }

    public void setAll(Map<String, Object> vars) {
        if (vars != null) {
            variables.putAll(vars);
        }
    }

    public Object get(String key) {
        if (variables.containsKey(key)) {
            return variables.get(key);
        }
        if (parent != null) {
            return parent.get(key);
        }
        return null;
    }

    public String getAsString(String key) {
        Object value = get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof String) {
            return (String) value;
        }
        return String.valueOf(value);
    }

    public boolean has(String key) {
        if (variables.containsKey(key)) {
            return true;
        }
        return parent != null && parent.has(key);
    }

    public Set<String> localKeys() {
        return Collections.unmodifiableSet(variables.keySet());
    }

    public Map<String, Object> localVariables() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(variables));
    }

    public void clear() {
        variables.clear();
    }

    public VariableScope createChild(String childName) {
        return new VariableScope(childName, this);
    }

    @Override
    public String toString() {
        return "VariableScope{name='" + name + "', vars=" + variables.keySet() +
                (parent != null ? ", parent=" + parent.name : "") + "}";
    }
}
