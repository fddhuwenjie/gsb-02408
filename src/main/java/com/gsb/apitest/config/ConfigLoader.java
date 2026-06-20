package com.gsb.apitest.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

public class ConfigLoader {
    private static final Logger log = LoggerFactory.getLogger(ConfigLoader.class);
    private static final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
    private static final ObjectMapper jsonMapper = new ObjectMapper();

    public static TestConfig load() {
        String env = System.getProperty("env", "test");
        log.info("Loading configuration for environment: {}", env);

        TestConfig config = loadFromClasspath("application-" + env + ".yaml");
        if (config == null) {
            config = loadFromClasspath("application-" + env + ".yml");
        }
        if (config == null) {
            config = loadFromClasspath("application-" + env + ".json");
        }
        if (config == null) {
            config = loadFromProperties("application-" + env + ".properties");
        }
        if (config == null) {
            config = loadFromClasspath("application.yaml");
        }
        if (config == null) {
            config = loadFromClasspath("application.yml");
        }
        if (config == null) {
            log.warn("No config file found, using default configuration");
            config = new TestConfig();
            config.setBaseUrl("http://localhost:8080");
        }
        return config;
    }

    private static TestConfig loadFromClasspath(String resourceName) {
        try (InputStream is = ConfigLoader.class.getClassLoader().getResourceAsStream(resourceName)) {
            if (is == null) {
                return null;
            }
            log.info("Loading config from classpath: {}", resourceName);
            if (resourceName.endsWith(".yaml") || resourceName.endsWith(".yml")) {
                return yamlMapper.readValue(is, TestConfig.class);
            } else if (resourceName.endsWith(".json")) {
                return jsonMapper.readValue(is, TestConfig.class);
            }
        } catch (Exception e) {
            log.warn("Failed to load config from {}: {}", resourceName, e.getMessage());
        }
        return null;
    }

    private static TestConfig loadFromProperties(String resourceName) {
        try (InputStream is = ConfigLoader.class.getClassLoader().getResourceAsStream(resourceName)) {
            if (is == null) {
                return null;
            }
            log.info("Loading config from properties: {}", resourceName);
            Properties props = new Properties();
            props.load(is);
            TestConfig config = new TestConfig();
            config.setBaseUrl(props.getProperty("baseUrl", "http://localhost:8080"));
            if (props.containsKey("connectTimeout")) {
                config.setConnectTimeout(Integer.parseInt(props.getProperty("connectTimeout")));
            }
            if (props.containsKey("readTimeout")) {
                config.setReadTimeout(Integer.parseInt(props.getProperty("readTimeout")));
            }
            for (String name : props.stringPropertyNames()) {
                if (name.startsWith("header.")) {
                    String headerName = name.substring(7);
                    config.addGlobalHeader(headerName, props.getProperty(name));
                } else if (name.startsWith("var.")) {
                    String varName = name.substring(4);
                    config.setGlobalVariable(varName, props.getProperty(name));
                }
            }
            return config;
        } catch (Exception e) {
            log.warn("Failed to load properties from {}: {}", resourceName, e.getMessage());
        }
        return null;
    }

    public static TestConfig loadFromFile(String filePath) {
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                return null;
            }
            log.info("Loading config from file: {}", filePath);
            if (filePath.endsWith(".yaml") || filePath.endsWith(".yml")) {
                return yamlMapper.readValue(file, TestConfig.class);
            } else if (filePath.endsWith(".json")) {
                return jsonMapper.readValue(file, TestConfig.class);
            }
        } catch (Exception e) {
            log.warn("Failed to load config from file {}: {}", filePath, e.getMessage());
        }
        return null;
    }
}
