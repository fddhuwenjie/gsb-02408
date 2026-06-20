package com.gsb.apitest.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * 配置管理器：负责加载 classpath 下的 apitest.properties，
 * 并允许通过 JVM 系统属性 / 环境变量覆盖。该类是单例，
 * 加载一次即可在所有用例中复用。
 */
public final class ConfigManager {

    private static final String DEFAULT_RESOURCE = "apitest.properties";
    private static final String HEADER_PREFIX = "apitest.defaultHeader.";

    private static volatile ConfigManager instance;

    private final Map<String, String> properties;

    private ConfigManager(Map<String, String> properties) {
        this.properties = Collections.unmodifiableMap(properties);
    }

    public static ConfigManager getInstance() {
        if (instance == null) {
            synchronized (ConfigManager.class) {
                if (instance == null) {
                    instance = load(DEFAULT_RESOURCE);
                }
            }
        }
        return instance;
    }

    static ConfigManager load(String resource) {
        Properties props = new Properties();
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        try (InputStream in = cl.getResourceAsStream(resource)) {
            if (in != null) {
                props.load(in);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load config: " + resource, e);
        }

        Map<String, String> merged = new HashMap<>();
        for (String key : props.stringPropertyNames()) {
            merged.put(key, props.getProperty(key));
        }
        // 系统属性覆盖
        for (String key : System.getProperties().stringPropertyNames()) {
            if (key.startsWith("apitest.")) {
                merged.put(key, System.getProperty(key));
            }
        }
        // 环境变量覆盖（APITEST_BASE_URL -> apitest.baseUrl）
        System.getenv().forEach((k, v) -> {
            if (k.startsWith("APITEST_")) {
                String normalized = "apitest." + toCamel(k.substring("APITEST_".length()));
                merged.put(normalized, v);
            }
        });
        return new ConfigManager(merged);
    }

    private static String toCamel(String upperWithUnderscore) {
        String[] parts = upperWithUnderscore.toLowerCase().split("_");
        StringBuilder sb = new StringBuilder(parts[0]);
        for (int i = 1; i < parts.length; i++) {
            if (parts[i].isEmpty()) continue;
            sb.append(Character.toUpperCase(parts[i].charAt(0)));
            sb.append(parts[i].substring(1));
        }
        return sb.toString();
    }

    public String get(String key) {
        return properties.get(key);
    }

    public String get(String key, String defaultValue) {
        return properties.getOrDefault(key, defaultValue);
    }

    public int getInt(String key, int defaultValue) {
        String v = properties.get(key);
        if (v == null || v.isEmpty()) return defaultValue;
        return Integer.parseInt(v.trim());
    }

    public String getBaseUrl() {
        return get("apitest.baseUrl", "");
    }

    public int getTimeoutMs() {
        return getInt("apitest.timeoutMs", 15000);
    }

    public String getReportDir() {
        return get("apitest.report.dir", "target/apitest-report");
    }

    public Map<String, String> getDefaultHeaders() {
        Map<String, String> headers = new HashMap<>();
        for (Map.Entry<String, String> e : properties.entrySet()) {
            if (e.getKey().startsWith(HEADER_PREFIX)) {
                headers.put(e.getKey().substring(HEADER_PREFIX.length()), e.getValue());
            }
        }
        return headers;
    }

    /** 测试场景下重置配置，仅供框架内部使用。 */
    static void resetForTesting() {
        synchronized (ConfigManager.class) {
            instance = null;
        }
    }
}
