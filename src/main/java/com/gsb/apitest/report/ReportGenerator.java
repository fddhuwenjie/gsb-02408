package com.gsb.apitest.report;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.gsb.apitest.config.TestConfiguration;
import com.gsb.apitest.context.ExecutionContext;
import com.gsb.apitest.context.StepRecord;
import com.gsb.apitest.model.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportGenerator {
    private static final Logger log = LoggerFactory.getLogger(ReportGenerator.class);
    private static final ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    public void generateReport(ExecutionContext context, String testClassName, String testMethodName) {
        String reportDir = TestConfiguration.getInstance().getReportDirectory();
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        String fileName = String.format("%s-%s-%s.json", testClassName, testMethodName, timestamp);

        Map<String, Object> report = buildReport(context, testClassName, testMethodName);

        try {
            Path dirPath = Paths.get(reportDir);
            Files.createDirectories(dirPath);
            Path filePath = dirPath.resolve(fileName);

            String json = mapper.writeValueAsString(report);
            Files.writeString(filePath, json, StandardCharsets.UTF_8);
            log.info("测试报告已生成: {}", filePath.toAbsolutePath());

            generateHtmlReport(context, dirPath.resolve(fileName.replace(".json", ".html")),
                    testClassName, testMethodName);
        } catch (IOException e) {
            log.error("生成报告失败: {}", e.getMessage(), e);
        }
    }

    private Map<String, Object> buildReport(ExecutionContext context, String testClass, String testMethod) {
        Map<String, Object> report = new HashMap<>();
        report.put("testId", context.getTestId());
        report.put("testClass", testClass);
        report.put("testMethod", testMethod);
        report.put("startTime", LocalDateTime.now().toString());
        report.put("totalElapsedMs", context.getElapsedMs());
        report.put("variables", context.getAllVariables());

        List<Map<String, Object>> steps = new ArrayList<>();
        boolean allSuccess = true;

        for (StepRecord record : context.getStepRecords()) {
            Map<String, Object> stepMap = new HashMap<>();
            stepMap.put("index", record.getStepIndex());
            stepMap.put("name", record.getStepName());
            stepMap.put("method", record.getMethod());
            stepMap.put("url", record.getUrl());
            stepMap.put("headers", record.getHeaders());
            stepMap.put("requestBody", record.getRequestBody());
            stepMap.put("durationMs", record.getDurationMs());
            stepMap.put("success", record.isSuccess());
            stepMap.put("errorMessage", record.getErrorMessage());
            stepMap.put("extractedVariables", record.getExtractedVariables());

            ApiResponse resp = record.getResponse();
            if (resp != null) {
                Map<String, Object> respMap = new HashMap<>();
                respMap.put("statusCode", resp.getStatusCode());
                respMap.put("headers", resp.getHeaders());
                respMap.put("body", resp.getBody());
                respMap.put("responseTimeMs", resp.getResponseTimeMs());
                stepMap.put("response", respMap);
            }

            if (!record.isSuccess()) {
                allSuccess = false;
            }
            steps.add(stepMap);
        }

        report.put("success", allSuccess);
        report.put("totalSteps", steps.size());
        report.put("steps", steps);
        return report;
    }

    private void generateHtmlReport(ExecutionContext context, Path filePath,
                                    String testClass, String testMethod) throws IOException {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><head><meta charset=\"UTF-8\">");
        html.append("<title>API Test Report</title>");
        html.append("<style>");
        html.append("body{font-family:Arial,sans-serif;margin:20px;background:#f5f5f5;}");
        html.append(".container{max-width:1200px;margin:0 auto;background:white;padding:20px;border-radius:8px;box-shadow:0 2px 4px rgba(0,0,0,0.1);}");
        html.append(".success{color:#28a745;}.failure{color:#dc3545;}");
        html.append(".step{border:1px solid #ddd;border-radius:4px;margin:10px 0;padding:15px;}");
        html.append(".step.success{border-left:4px solid #28a745;}");
        html.append(".step.failure{border-left:4px solid #dc3545;}");
        html.append("pre{background:#f8f9fa;padding:10px;border-radius:4px;overflow-x:auto;font-size:12px;}");
        html.append("table{width:100%;border-collapse:collapse;margin:10px 0;}");
        html.append("th,td{border:1px solid #ddd;padding:8px;text-align:left;}");
        html.append("th{background:#f8f9fa;}");
        html.append("</style></head><body>");
        html.append("<div class=\"container\">");
        html.append("<h1>API Test Report</h1>");
        html.append("<p><strong>Test Class:</strong> ").append(testClass).append("</p>");
        html.append("<p><strong>Test Method:</strong> ").append(testMethod).append("</p>");
        html.append("<p><strong>Total Time:</strong> ").append(context.getElapsedMs()).append("ms</p>");

        boolean allSuccess = context.getStepRecords().stream().allMatch(StepRecord::isSuccess);
        html.append("<p><strong>Result:</strong> <span class='").append(allSuccess ? "success" : "failure").append("'>");
        html.append(allSuccess ? "PASS" : "FAIL").append("</span></p>");

        html.append("<h2>Steps</h2>");
        for (StepRecord record : context.getStepRecords()) {
            html.append("<div class='step ").append(record.isSuccess() ? "success" : "failure").append("'>");
            html.append("<h3>").append(record.getStepIndex()).append(". ").append(record.getStepName());
            html.append(" <span class='").append(record.isSuccess() ? "success" : "failure").append("'>");
            html.append(record.isSuccess() ? "✓" : "✗").append("</span>");
            html.append(" <small>(").append(record.getDurationMs()).append("ms)</small></h3>");
            html.append("<p><strong>").append(record.getMethod()).append("</strong> ").append(record.getUrl()).append("</p>");

            if (record.getRequestBody() != null) {
                html.append("<h4>Request</h4><pre>").append(escapeHtml(record.getRequestBody())).append("</pre>");
            }

            ApiResponse resp = record.getResponse();
            if (resp != null) {
                html.append("<h4>Response (").append(resp.getStatusCode()).append(")</h4>");
                html.append("<pre>").append(escapeHtml(resp.getBody())).append("</pre>");
            }

            if (record.getErrorMessage() != null) {
                html.append("<p class='failure'><strong>Error:</strong> ").append(escapeHtml(record.getErrorMessage())).append("</p>");
            }

            if (!record.getExtractedVariables().isEmpty()) {
                html.append("<h4>Extracted Variables</h4><table>");
                html.append("<tr><th>Variable</th><th>Value</th></tr>");
                for (Map.Entry<String, Object> entry : record.getExtractedVariables().entrySet()) {
                    html.append("<tr><td>").append(entry.getKey()).append("</td><td>").append(entry.getValue()).append("</td></tr>");
                }
                html.append("</table>");
            }

            html.append("</div>");
        }

        html.append("<h2>Final Variables</h2>");
        html.append("<table><tr><th>Key</th><th>Value</th></tr>");
        for (Map.Entry<String, Object> entry : context.getAllVariables().entrySet()) {
            html.append("<tr><td>").append(entry.getKey()).append("</td><td>").append(entry.getValue()).append("</td></tr>");
        }
        html.append("</table>");

        html.append("</div></body></html>");

        Files.writeString(filePath, html.toString(), StandardCharsets.UTF_8);
        log.info("HTML报告已生成: {}", filePath.toAbsolutePath());
    }

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
    }
}
