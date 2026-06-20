package com.gsb.apitest.util;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public final class Base64Utils {

    private Base64Utils() {
    }

    public static String encode(String content) {
        if (content == null) {
            return null;
        }
        return Base64.getEncoder().encodeToString(content.getBytes(StandardCharsets.UTF_8));
    }

    public static String encode(byte[] data) {
        if (data == null) {
            return null;
        }
        return Base64.getEncoder().encodeToString(data);
    }

    public static String encodeFromResource(String resourcePath) {
        byte[] data = FileUtils.readResourceBytes(resourcePath);
        return encode(data);
    }

    public static String decode(String base64) {
        if (base64 == null) {
            return null;
        }
        byte[] decoded = Base64.getDecoder().decode(base64);
        return new String(decoded, StandardCharsets.UTF_8);
    }
}
