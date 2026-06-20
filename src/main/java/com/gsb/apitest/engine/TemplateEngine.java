package com.gsb.apitest.engine;

import com.gsb.apitest.config.TestConfiguration;
import com.gsb.apitest.context.ExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TemplateEngine {
    private static final Logger log = LoggerFactory.getLogger(TemplateEngine.class);

    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\$\\{([^}]+)\\}");
    private static final Pattern FUNC_CALL_PATTERN = Pattern.compile("\\$\\{\\s*(fileToBase64)\\(\\s*['\"]([^'\"]+)['\"]\\s*\\)\\s*\\}");

    private final ExecutionContext context;

    public TemplateEngine(ExecutionContext context) {
        this.context = context;
    }

    public String resolve(String template) {
        if (template == null) {
            return null;
        }
        String result = template;
        result = resolveFunctions(result);
        result = resolveVariables(result);
        return result;
    }

    private String resolveFunctions(String template) {
        Matcher matcher = FUNC_CALL_PATTERN.matcher(template);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String funcName = matcher.group(1);
            String arg = matcher.group(2);
            try {
                String replacement;
                if ("fileToBase64".equals(funcName)) {
                    replacement = fileToBase64(arg);
                } else {
                    replacement = matcher.group(0);
                }
                matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
            } catch (IOException e) {
                throw new RuntimeException("函数调用失败 " + funcName + "('" + arg + "'): " + e.getMessage(), e);
            }
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private String resolveVariables(String template) {
        Matcher matcher = VARIABLE_PATTERN.matcher(template);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String varName = matcher.group(1).trim();
            Object value = context.get(varName);
            if (value == null) {
                log.warn("变量 [{}] 未找到，保留原样", varName);
                matcher.appendReplacement(sb, Matcher.quoteReplacement(matcher.group(0)));
            } else {
                matcher.appendReplacement(sb, Matcher.quoteReplacement(String.valueOf(value)));
            }
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    public String fileToBase64(String filePath) throws IOException {
        Path path = resolveFilePath(filePath);
        byte[] fileBytes = Files.readAllBytes(path);
        return Base64.getEncoder().encodeToString(fileBytes);
    }

    public String loadTemplate(String templateName) throws IOException {
        Path path = resolveTemplatePath(templateName);
        String content = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
        return resolve(content);
    }

    private Path resolveFilePath(String filePath) {
        Path path = Paths.get(filePath);
        if (Files.exists(path)) {
            return path;
        }
        path = Paths.get("src/test/resources", filePath);
        if (Files.exists(path)) {
            return path;
        }
        throw new RuntimeException("文件不存在: " + filePath);
    }

    private Path resolveTemplatePath(String templateName) {
        String templateDir = TestConfiguration.getInstance().getTemplateDirectory();
        Path path = Paths.get(templateDir, templateName);
        if (Files.exists(path)) {
            return path;
        }
        path = Paths.get("src/test/resources/templates", templateName);
        if (Files.exists(path)) {
            return path;
        }
        path = Paths.get(templateName);
        if (Files.exists(path)) {
            return path;
        }
        throw new RuntimeException("模板文件不存在: " + templateName);
    }
}
