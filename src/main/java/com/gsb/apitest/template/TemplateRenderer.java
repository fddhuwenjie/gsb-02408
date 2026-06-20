package com.gsb.apitest.template;

import com.gsb.apitest.context.TestContext;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 模板替换器：在请求模板（URL / Header / Body）里把 ${...} 占位符替换为
 * 上下文里的真实值。同时支持调用内置函数：
 *
 * <ul>
 *     <li>${var}                  —— 直接读取上下文变量</li>
 *     <li>${fileToBase64(path)}   —— 读取文件并 base64 编码</li>
 *     <li>${env(KEY)}             —— 读取环境变量</li>
 *     <li>${uuid()}               —— 生成一个随机 UUID</li>
 * </ul>
 *
 * 函数注册表是开放的，业务侧可以通过 {@link #registerFunction} 注入新函数。
 */
public class TemplateRenderer {

    @FunctionalInterface
    public interface TemplateFunction {
        String apply(String arg, TestContext ctx);
    }

    private static final Pattern PLACEHOLDER = Pattern.compile("\\$\\{([^}]+)\\}");
    private static final Pattern FUNCTION = Pattern.compile("^([a-zA-Z_][a-zA-Z0-9_]*)\\((.*)\\)$");

    private final Map<String, TemplateFunction> functions = new HashMap<>();

    public TemplateRenderer() {
        registerFunction("fileToBase64", (arg, ctx) -> fileToBase64(arg));
        registerFunction("env", (arg, ctx) -> {
            String v = System.getenv(arg);
            return v == null ? "" : v;
        });
        registerFunction("uuid", (arg, ctx) -> java.util.UUID.randomUUID().toString());
    }

    public void registerFunction(String name, TemplateFunction fn) {
        functions.put(name, fn);
    }

    public String render(String template, TestContext ctx) {
        if (template == null || template.isEmpty()) return template;
        Matcher m = PLACEHOLDER.matcher(template);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String expr = m.group(1).trim();
            String replacement = resolveExpression(expr, ctx);
            m.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    private String resolveExpression(String expr, TestContext ctx) {
        Matcher fm = FUNCTION.matcher(expr);
        if (fm.matches()) {
            String fnName = fm.group(1);
            String rawArg = fm.group(2).trim();
            // 函数参数本身允许嵌套使用变量，比如 fileToBase64(${imgPath})
            String resolvedArg = render(rawArg, ctx);
            // 去掉两端可能存在的引号
            if (resolvedArg.length() >= 2
                    && ((resolvedArg.startsWith("\"") && resolvedArg.endsWith("\""))
                    || (resolvedArg.startsWith("'") && resolvedArg.endsWith("'")))) {
                resolvedArg = resolvedArg.substring(1, resolvedArg.length() - 1);
            }
            TemplateFunction fn = functions.get(fnName);
            if (fn == null) {
                throw new IllegalArgumentException("Unknown template function: " + fnName);
            }
            return fn.apply(resolvedArg, ctx);
        }
        Object value = ctx.getVar(expr);
        if (value == null) {
            // 兼容 a.b.c 形式：先尝试取 a，再取嵌套字段（基于 Map）
            value = resolveDotted(expr, ctx);
        }
        return value == null ? "" : String.valueOf(value);
    }

    @SuppressWarnings("unchecked")
    private Object resolveDotted(String expr, TestContext ctx) {
        String[] parts = expr.split("\\.");
        Object cur = ctx.getVar(parts[0]);
        for (int i = 1; i < parts.length && cur != null; i++) {
            if (cur instanceof Map) {
                cur = ((Map<String, Object>) cur).get(parts[i]);
            } else {
                return null;
            }
        }
        return cur;
    }

    /**
     * 读取文件内容并 base64 编码。优先按文件系统路径读取，
     * 找不到时再尝试从 classpath 加载，方便业务把样本文件放在 src/test/resources。
     */
    public static String fileToBase64(String pathLike) {
        try {
            Path p = Paths.get(pathLike);
            if (Files.exists(p)) {
                byte[] data = Files.readAllBytes(p);
                return Base64.getEncoder().encodeToString(data);
            }
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            try (InputStream in = cl.getResourceAsStream(pathLike)) {
                if (in == null) {
                    throw new IllegalArgumentException(
                            "fileToBase64: cannot find file on disk or classpath: " + pathLike);
                }
                byte[] data = in.readAllBytes();
                return Base64.getEncoder().encodeToString(data);
            }
        } catch (IOException e) {
            throw new IllegalStateException("fileToBase64 failed for: " + pathLike, e);
        }
    }

    /** 从 classpath 直接读模板字符串。 */
    public static String loadTemplate(String classpathResource) {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        try (InputStream in = cl.getResourceAsStream(classpathResource)) {
            if (in == null) {
                throw new IllegalArgumentException("template not found: " + classpathResource);
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("failed to load template: " + classpathResource, e);
        }
    }
}
