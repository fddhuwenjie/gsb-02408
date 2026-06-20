package com.gsb.apitest.template;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

public class Base64Function {
    private static final Logger log = LoggerFactory.getLogger(Base64Function.class);

    public String fileToBase64(String filePath) {
        try {
            byte[] fileContent;
            File file = new File(filePath);
            if (file.exists()) {
                fileContent = Files.readAllBytes(file.toPath());
            } else {
                InputStream is = getClass().getClassLoader().getResourceAsStream(filePath);
                if (is != null) {
                    fileContent = is.readAllBytes();
                    is.close();
                } else {
                    throw new IllegalArgumentException("File not found: " + filePath);
                }
            }
            String encoded = Base64.getEncoder().encodeToString(fileContent);
            log.debug("Encoded file {} to base64 (length: {})", filePath, encoded.length());
            return encoded;
        } catch (Exception e) {
            log.error("Failed to encode file {} to base64: {}", filePath, e.getMessage());
            throw new RuntimeException("Failed to encode file to base64: " + filePath, e);
        }
    }

    public static String encode(byte[] data) {
        return Base64.getEncoder().encodeToString(data);
    }

    public static String encode(String content) {
        return Base64.getEncoder().encodeToString(content.getBytes(StandardCharsets.UTF_8));
    }
}
