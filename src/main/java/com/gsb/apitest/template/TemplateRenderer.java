package com.gsb.apitest.template;

import com.gsb.apitest.context.ExecutionContext;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class TemplateRenderer {
    private static final Logger log = LoggerFactory.getLogger(TemplateRenderer.class);
    private static final Configuration freemarkerConfig;
    private final Base64Function base64Function = new Base64Function();

    static {
        freemarkerConfig = new Configuration(Configuration.VERSION_2_3_32);
        freemarkerConfig.setDefaultEncoding("UTF-8");
        freemarkerConfig.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        freemarkerConfig.setLogTemplateExceptions(false);
        freemarkerConfig.setNumberFormat("0.######");
        freemarkerConfig.setBooleanFormat("true,false");
    }

    public String render(String templateContent, Map<String, Object> variables) {
        try {
            Map<String, Object> dataModel = new HashMap<>();
            if (variables != null) {
                dataModel.putAll(variables);
            }
            dataModel.put("fileToBase64", base64Function);

            Template template = new Template("inline", new StringReader(templateContent), freemarkerConfig);
            StringWriter writer = new StringWriter();
            template.process(dataModel, writer);
            String result = writer.toString();
            log.debug("Rendered template, length before: {}, after: {}", templateContent.length(), result.length());
            return result;
        } catch (Exception e) {
            log.error("Template rendering failed: {}", e.getMessage());
            throw new RuntimeException("Template rendering failed: " + e.getMessage(), e);
        }
    }

    public String renderWithGlobals(String templateContent) {
        ExecutionContext ctx = ExecutionContext.getCurrent();
        if (ctx != null) {
            return render(templateContent, ctx.getVariables().getAllVariables());
        }
        return render(templateContent, new HashMap<>());
    }

    public String renderFromClasspath(String resourcePath) {
        return renderFromClasspath(resourcePath, null);
    }

    public String renderFromClasspath(String resourcePath, Map<String, Object> extraVariables) {
        try {
            InputStream is = getClass().getClassLoader().getResourceAsStream(resourcePath);
            if (is == null) {
                throw new IllegalArgumentException("Template not found in classpath: " + resourcePath);
            }
            String content = IOUtils.toString(is, StandardCharsets.UTF_8);
            is.close();

            Map<String, Object> variables = new HashMap<>();
            ExecutionContext ctx = ExecutionContext.getCurrent();
            if (ctx != null) {
                variables.putAll(ctx.getVariables().getAllVariables());
            }
            if (extraVariables != null) {
                variables.putAll(extraVariables);
            }

            return render(content, variables);
        } catch (Exception e) {
            log.error("Failed to load/render template {}: {}", resourcePath, e.getMessage());
            throw new RuntimeException("Failed to process template: " + resourcePath, e);
        }
    }

    public String renderFromFile(String filePath) {
        return renderFromFile(filePath, null);
    }

    public String renderFromFile(String filePath, Map<String, Object> extraVariables) {
        try {
            InputStream is = java.nio.file.Files.newInputStream(java.nio.file.Paths.get(filePath));
            String content = IOUtils.toString(is, StandardCharsets.UTF_8);
            is.close();

            Map<String, Object> variables = new HashMap<>();
            ExecutionContext ctx = ExecutionContext.getCurrent();
            if (ctx != null) {
                variables.putAll(ctx.getVariables().getAllVariables());
            }
            if (extraVariables != null) {
                variables.putAll(extraVariables);
            }

            return render(content, variables);
        } catch (Exception e) {
            log.error("Failed to load/render file {}: {}", filePath, e.getMessage());
            throw new RuntimeException("Failed to process file: " + filePath, e);
        }
    }
}
