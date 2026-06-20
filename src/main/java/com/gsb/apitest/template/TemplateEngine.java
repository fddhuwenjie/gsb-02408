package com.gsb.apitest.template;

import com.gsb.apitest.context.TestContext;
import com.gsb.apitest.util.FileUtils;
import com.gsb.apitest.util.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TemplateEngine {

    private static final Logger log = LoggerFactory.getLogger(TemplateEngine.class);

    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\$\\{([^}]+)\\}");
    private static final Pattern FUNCTION_PATTERN = Pattern.compile("^(\\w+)\\((.*)\\)$");

    private final Map<String, TemplateFunction> functions = new LinkedHashMap<>();

    public TemplateEngine() {
        registerFunction(new FileToBase64Function());
        registerFunction(new EnvFunction());
    }

    public void registerFunction(TemplateFunction function) {
        functions.put(function.name(), function);
    }

    public String render(String template, TestContext context) {
        if (template == null || template.isEmpty()) {
            return template;
        }
        return renderString(template, context);
    }

    public String renderTemplate(String templateResource, TestContext context) {
        String resolved = FileUtils.resolveTemplatePath(templateResource);
        String raw = FileUtils.readResource(resolved);
        log.debug("加载模板文件: {}", resolved);
        return render(raw, context);
    }

    public String renderTemplateRaw(String templateContent, TestContext context) {
        return render(templateContent, context);
    }

    private String renderString(String text, TestContext context) {
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(text);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String expression = matcher.group(1).trim();
            String value = resolveExpression(expression, context);
            String escaped = Matcher.quoteReplacement(value != null ? value : "");
            matcher.appendReplacement(sb, escaped);
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private String resolveExpression(String expression, TestContext context) {
        Matcher fnMatcher = FUNCTION_PATTERN.matcher(expression);
        if (fnMatcher.matches()) {
            String fnName = fnMatcher.group(1);
            String argsStr = fnMatcher.group(2);
            return executeFunction(fnName, argsStr, context);
        }
        return resolveVariable(expression, context);
    }

    private String executeFunction(String fnName, String argsStr, TestContext context) {
        TemplateFunction fn = functions.get(fnName);
        if (fn == null) {
            throw new RuntimeException("未知的模板函数: " + fnName + "()，已注册函数: " + functions.keySet());
        }
        List<String> args = parseArgs(argsStr, context);
        String result = fn.execute(args, context);
        log.debug("模板函数 {}({}) = [{}...]", fnName, argsStr,
                result == null ? "null" : result.substring(0, Math.min(50, result.length())));
        return result;
    }

    private List<String> parseArgs(String argsStr, TestContext context) {
        List<String> args = new ArrayList<>();
        if (argsStr == null || argsStr.trim().isEmpty()) {
            return args;
        }
        List<String> rawArgs = splitArgs(argsStr);
        for (String arg : rawArgs) {
            String trimmed = arg.trim();
            String resolved = renderString(trimmed, context);
            args.add(resolved);
        }
        return args;
    }

    private List<String> splitArgs(String argsStr) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inSingleQuote = false;
        boolean inDoubleQuote = false;
        int depth = 0;

        for (int i = 0; i < argsStr.length(); i++) {
            char c = argsStr.charAt(i);
            if (c == '\'' && !inDoubleQuote) {
                inSingleQuote = !inSingleQuote;
                current.append(c);
            } else if (c == '"' && !inSingleQuote) {
                inDoubleQuote = !inDoubleQuote;
                current.append(c);
            } else if (c == '(' && !inSingleQuote && !inDoubleQuote) {
                depth++;
                current.append(c);
            } else if (c == ')' && !inSingleQuote && !inDoubleQuote) {
                depth--;
                current.append(c);
            } else if (c == ',' && !inSingleQuote && !inDoubleQuote && depth == 0) {
                result.add(current.toString());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }
        if (current.length() > 0) {
            result.add(current.toString());
        }
        return result;
    }

    private String resolveVariable(String varName, TestContext context) {
        Object value = context.resolveVariable(varName);
        if (value == null) {
            log.warn("模板变量 '{}' 未找到，替换为空字符串", varName);
            return "";
        }
        if (value instanceof String) {
            return (String) value;
        }
        return JsonUtils.toJson(value);
    }
}
