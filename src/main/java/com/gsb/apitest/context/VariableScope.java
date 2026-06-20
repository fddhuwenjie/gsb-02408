package com.gsb.apitest.context;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 变量作用域：保存某一层级（global / class / case）下的命名变量。
 * 内部使用 ConcurrentHashMap，保证多线程并发执行用例时不会串数据。
 */
public class VariableScope {

    private final String name;
    private final VariableScope parent;
    private final Map<String, Object> values = new ConcurrentHashMap<>();

    public VariableScope(String name, VariableScope parent) {
        this.name = name;
        this.parent = parent;
    }

    public String name() {
        return name;
    }

    public VariableScope parent() {
        return parent;
    }

    public void put(String key, Object value) {
        if (key == null) throw new IllegalArgumentException("variable key cannot be null");
        if (value == null) {
            values.remove(key);
        } else {
            values.put(key, value);
        }
    }

    public Object getLocal(String key) {
        return values.get(key);
    }

    /** 沿父链向上查找变量。 */
    public Object resolve(String key) {
        Object v = values.get(key);
        if (v != null) return v;
        if (parent != null) return parent.resolve(key);
        return null;
    }

    public boolean contains(String key) {
        if (values.containsKey(key)) return true;
        return parent != null && parent.contains(key);
    }

    public Map<String, Object> snapshotLocal() {
        return new ConcurrentHashMap<>(values);
    }
}
