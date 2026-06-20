package com.gsb.apitest.extract;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonPathExtractor {
    private static final Logger log = LoggerFactory.getLogger(JsonPathExtractor.class);
    private static final Configuration conf = Configuration.builder()
            .options(Option.SUPPRESS_EXCEPTIONS)
            .build();

    private final DocumentContext documentContext;

    public JsonPathExtractor(String json) {
        this.documentContext = JsonPath.using(conf).parse(json);
    }

    public <T> T read(String path) {
        try {
            T result = documentContext.read(path);
            log.debug("Extracted '{}' = {}", path, result);
            return result;
        } catch (Exception e) {
            log.warn("Failed to extract path '{}': {}", path, e.getMessage());
            return null;
        }
    }

    public String readString(String path) {
        Object value = read(path);
        return value != null ? value.toString() : null;
    }

    public Integer readInt(String path) {
        Object value = read(path);
        if (value == null) return null;
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public Long readLong(String path) {
        Object value = read(path);
        if (value == null) return null;
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public Boolean readBoolean(String path) {
        Object value = read(path);
        if (value == null) return null;
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return Boolean.parseBoolean(value.toString());
    }

    public boolean pathExists(String path) {
        try {
            Object result = documentContext.read(path);
            return result != null;
        } catch (Exception e) {
            return false;
        }
    }
}
