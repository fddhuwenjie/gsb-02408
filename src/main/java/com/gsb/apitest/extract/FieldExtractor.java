package com.gsb.apitest.extract;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;

/**
 * 字段提取器：基于 JsonPath 从任意 JSON（Map / List / String）里抽字段。
 * 这一层独立出来，是因为业务侧可能要从“请求 body”和“响应 body”里都抽字段，
 * 让它们共用一个表达式语法。
 */
public class FieldExtractor {

    private static final Configuration CONFIG = Configuration.defaultConfiguration()
            .addOptions(Option.SUPPRESS_EXCEPTIONS);

    public static Object extract(Object source, String jsonPath) {
        if (source == null) return null;
        if (jsonPath == null || jsonPath.isEmpty()) return null;
        if (source instanceof String) {
            return JsonPath.using(CONFIG).parse((String) source).read(jsonPath);
        }
        return JsonPath.using(CONFIG).parse(source).read(jsonPath);
    }
}
