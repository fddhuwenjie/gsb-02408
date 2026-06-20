package com.gsb.apitest.report;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.gsb.apitest.context.ExecutionContext;
import com.gsb.apitest.model.StepResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestReportGenerator {
    private static final Logger log = LoggerFactory.getLogger(TestReportGenerator.class);
    private static final ObjectMapper objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    private static final String REPORT_DIR = "target/test-reports";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    public void generateReport(ExecutionContext context) {
        try {
            Path reportDir = Paths.get(REPORT_DIR);
            if (!Files.exists(reportDir)) {
                Files.createDirectories(reportDir);
            }

            String timestamp = LocalDateTime.now().format(FORMATTER);
            String testName = context.getCurrentTestName().replaceAll("[^a-zA-Z0-9.-]", "_");
            String baseName = testName + "-" + timestamp;

            generateJsonReport(context, reportDir, baseName);
            generateTextReport(context, reportDir, baseName);
            log.info("Test report generated for: {}", context.getCurrentTestName());
        } catch (Exception e) {
            log.warn("Failed to generate test report: {}", e.getMessage());
        }
    }

    private void generateJsonReport(ExecutionContext context, Path reportDir, String baseName) throws IOException {
        Map<String, Object> report = new HashMap<>();
        report.put("testName", context.getCurrentTestName());
        report.put("success", !context.isFailed());
        report.put("failureStep", context.getFailureStepName());
        report.put("failureMessage", context.getFailureCause() != null ? context.getFailureCause().getMessage() : null);
        report.put("totalSteps", context.getStepResults().size());
        report.put("timestamp", LocalDateTime.now().toString());

        List<Map<String, Object>> steps = new ArrayList<>();
        for (StepResult result : context.getStepResults()) {
            Map<String, Object> stepMap = new HashMap<>();
            stepMap.put("index", result.getStepIndex());
            stepMap.put("name", result.getStepName());
            stepMap.put("success", result.isSuccess());
            stepMap.put("statusCode", result.getStatusCode());
            stepMap.put("requestBody", result.getRequestBody());
            stepMap.put("responseBody", result.getResponseBody());
            stepMap.put("requestHeaders", result.getRequestHeaders());
            stepMap.put("responseHeaders", result.getResponseHeaders());
            stepMap.put("error", result.getError() != null ? result.getError().toString() : null);
            stepMap.put("timestamp", result.getTimestamp().toString());
            steps.add(stepMap);
        }
        report.put("steps", steps);

        File jsonFile = reportDir.resolve(baseName + ".json").toFile();
        objectMapper.writeValue(jsonFile, report);
        log.debug("JSON report written to: {}", jsonFile.getAbsolutePath());
    }

    private void generateTextReport(ExecutionContext context, Path reportDir, String baseName) throws IOException {
        File txtFile = reportDir.resolve(baseName + ".txt").toFile();
        try (PrintWriter writer = new PrintWriter(new FileWriter(txtFile))) {
            writer.println("========================================");
            writer.println("  API Test Report");
            writer.println("========================================");
            writer.println("Test: " + context.getCurrentTestName());
            writer.println("Result: " + (context.isFailed() ? "FAILED" : "PASSED"));
            writer.println("Steps Executed: " + context.getStepResults().size());
            if (context.isFailed()) {
                writer.println("Failure Step: " + context.getFailureStepName());
                writer.println("Failure Reason: " + context.getFailureCause().getMessage());
            }
            writer.println("========================================");
            writer.println();

            for (StepResult step : context.getStepResults()) {
                writer.println("----------------------------------------");
                writer.println("Step " + step.getStepIndex() + ": " + step.getStepName());
                writer.println("Status: " + (step.isSuccess() ? "PASS" : "FAIL"));
                if (step.isSuccess()) {
                    writer.println("HTTP Status: " + step.getStatusCode());
                    writer.println();
                    if (step.getRequestBody() != null && !step.getRequestBody().isEmpty()) {
                        writer.println(">>> Request Body:");
                        writer.println(step.getRequestBody());
                        writer.println();
                    }
                    if (step.getResponseBody() != null && !step.getResponseBody().isEmpty()) {
                        writer.println("<<< Response Body:");
                        writer.println(step.getResponseBody());
                    }
                } else {
                    writer.println("Error: " + step.getError());
                    step.getError().printStackTrace(writer);
                }
                writer.println();
            }
        }
        log.debug("Text report written to: {}", txtFile.getAbsolutePath());
    }

    public void logFailure(ExecutionContext context) {
        if (context.isFailed()) {
            log.error("========================================");
            log.error("TEST FAILED: {}", context.getCurrentTestName());
            log.error("Failed at step: {}", context.getFailureStepName());
            log.error("Error: {}", context.getFailureCause() != null ? context.getFailureCause().getMessage() : "n/a");
            log.error("========================================");
        }
    }
}
