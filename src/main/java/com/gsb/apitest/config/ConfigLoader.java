package com.gsb.apitest.config;

import com.gsb.apitest.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class ConfigLoader {

    private static final Logger log = LoggerFactory.getLogger(ConfigLoader.class);
    private static final String CONFIG_FILE = "apitest.properties";
    private static volatile TestConfig cachedConfig;

    private ConfigLoader() {
    }

    public static TestConfig load() {
        if (cachedConfig != null) {
            return cachedConfig;
        }
        synchronized (ConfigLoader.class) {
            if (cachedConfig != null) {
                return cachedConfig;
            }
            cachedConfig = doLoad();
            return cachedConfig;
        }
    }

    public static void reset() {
        cachedConfig = null;
    }

    private static TestConfig doLoad() {
        TestConfig config = new TestConfig();
        Properties props = new Properties();

        try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (is != null) {
                props.load(is);
                log.info("加载配置文件: {}", CONFIG_FILE);
            } else {
                log.info("未找到配置文件 {}, 使用默认配置", CONFIG_FILE);
            }
        } catch (IOException e) {
            log.warn("加载配置文件失败，使用默认配置", e);
        }

        String baseUrl = props.getProperty("apitest.base-url", "");
        config.setBaseUrl(baseUrl);

        String connectTimeout = props.getProperty("apitest.connect-timeout-ms", "10000");
        config.setConnectTimeoutMs(Long.parseLong(connectTimeout));

        String readTimeout = props.getProperty("apitest.read-timeout-ms", "30000");
        config.setReadTimeoutMs(Long.parseLong(readTimeout));

        String writeTimeout = props.getProperty("apitest.write-timeout-ms", "30000");
        config.setWriteTimeoutMs(Long.parseLong(writeTimeout));

        String contentType = props.getProperty("apitest.default-content-type", "application/json;charset=UTF-8");
        if (contentType != null && !contentType.isEmpty()) {
            config.addDefaultHeader("Content-Type", contentType);
        }

        String accept = props.getProperty("apitest.default-accept", "application/json");
        if (accept != null && !accept.isEmpty()) {
            config.addDefaultHeader("Accept", accept);
        }

        for (String name : props.stringPropertyNames()) {
            if (name.startsWith("apitest.header.")) {
                String headerName = name.substring("apitest.header.".length());
                String headerValue = props.getProperty(name);
                config.addDefaultHeader(headerName, headerValue);
            }
        }

        String reportEnabled = props.getProperty("apitest.report.enabled", "true");
        config.setReportEnabled(Boolean.parseBoolean(reportEnabled));

        String reportDir = props.getProperty("apitest.report.dir", "target/test-reports");
        config.setReportDir(reportDir);

        String logRequest = props.getProperty("apitest.log.request", "true");
        config.setLogRequest(Boolean.parseBoolean(logRequest));

        String logResponse = props.getProperty("apitest.log.response", "true");
        config.setLogResponse(Boolean.parseBoolean(logResponse));

        log.info("配置加载完成: baseUrl={}", config.getBaseUrl());
        return config;
    }
}
