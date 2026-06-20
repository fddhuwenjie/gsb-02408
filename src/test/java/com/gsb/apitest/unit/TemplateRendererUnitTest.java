package com.gsb.apitest.unit;

import com.gsb.apitest.template.TemplateRenderer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

public class TemplateRendererUnitTest {

    private TemplateRenderer renderer;

    @BeforeEach
    void setUp() {
        renderer = new TemplateRenderer();
    }

    @Test
    void testSimpleVariableReplacement() {
        String template = "{\"name\": \"${userName}\", \"age\": ${age}}";
        Map<String, Object> vars = new HashMap<>();
        vars.put("userName", "Alice");
        vars.put("age", 25);

        String result = renderer.render(template, vars);
        assertTrue(result.contains("Alice"));
        assertTrue(result.contains("25"));
    }

    @Test
    void testFileToBase64Function() {
        String template = "{\"data\": \"${fileToBase64.fileToBase64(\"testdata/sample.txt\")}\"}";
        String result = renderer.render(template, new HashMap<>());

        assertTrue(result.contains("SGVsbG8gR1NC"));
        String base64Part = result.split("\"")[3];
        byte[] decoded = Base64.getDecoder().decode(base64Part);
        String decodedStr = new String(decoded);
        assertTrue(decodedStr.startsWith("Hello GSB"));
    }

    @Test
    void testGlobalVariableAccess() {
        Map<String, Object> vars = new HashMap<>();
        vars.put("token", "abc123");
        String template = "{\"auth\": \"Bearer ${token}\"}";
        String result = renderer.render(template, vars);
        assertEquals("{\"auth\": \"Bearer abc123\"}", result);
    }
}
