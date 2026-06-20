package com.gsb.apitest.step;

import com.gsb.apitest.context.TestContext;
import com.gsb.apitest.http.HttpResponse;
import com.gsb.apitest.util.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;

public class VariableExtractor {

    private static final Logger log = LoggerFactory.getLogger(VariableExtractor.class);

    private final Map<String, String> extractions = new LinkedHashMap<>();
    private String scope = "class";

    public VariableExtractor var(String name, String jsonPath) {
        extractions.put(name, jsonPath);
        return this;
    }

    public VariableExtractor global() {
        this.scope = "global";
        return this;
    }

    public VariableExtractor clazz() {
        this.scope = "class";
        return this;
    }

    public VariableExtractor stepScope() {
        this.scope = "step";
        return this;
    }

    public void extract(HttpResponse response, TestContext context) {
        if (extractions.isEmpty() || response == null) {
            return;
        }
        for (Map.Entry<String, String> entry : extractions.entrySet()) {
            String varName = entry.getKey();
            String jsonPath = entry.getValue();
            Object value = response.jsonPath(jsonPath);
            String valueStr = value == null ? null : (value instanceof String ? (String) value : JsonUtils.toJson(value));
            switch (scope) {
                case "global":
                    context.setGlobalVariable(varName, valueStr);
                    break;
                case "step":
                    context.setStepVariable(varName, valueStr);
                    break;
                default:
                    context.setClassVariable(varName, valueStr);
            }
            log.info("提取变量: {} = {} (来源: {})", varName,
                    valueStr == null ? "null" : (valueStr.length() > 80 ? valueStr.substring(0, 80) + "..." : valueStr),
                    jsonPath);
        }
    }

    public Map<String, String> getExtractions() {
        return extractions;
    }
}
