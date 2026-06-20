package com.gsb.apitest.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class FileUtils {

    private FileUtils() {
    }

    public static String readResource(String resourcePath) {
        try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourcePath)) {
            if (is == null) {
                throw new RuntimeException("资源文件不存在: " + resourcePath);
            }
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("读取资源文件失败: " + resourcePath, e);
        }
    }

    public static byte[] readResourceBytes(String resourcePath) {
        try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourcePath)) {
            if (is == null) {
                throw new RuntimeException("资源文件不存在: " + resourcePath);
            }
            return is.readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException("读取资源文件失败: " + resourcePath, e);
        }
    }

    public static String readFile(String filePath) {
        try {
            return Files.readString(Paths.get(filePath), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("读取文件失败: " + filePath, e);
        }
    }

    public static byte[] readFileBytes(String filePath) {
        try {
            return Files.readAllBytes(Paths.get(filePath));
        } catch (IOException e) {
            throw new RuntimeException("读取文件失败: " + filePath, e);
        }
    }

    public static boolean resourceExists(String resourcePath) {
        return Thread.currentThread().getContextClassLoader().getResource(resourcePath) != null;
    }

    public static String resolveTemplatePath(String templateName) {
        String path = templateName;
        if (!path.startsWith("templates/")) {
            path = "templates/" + path;
        }
        if (!path.endsWith(".json")) {
            path = path + ".json";
        }
        return path;
    }
}
