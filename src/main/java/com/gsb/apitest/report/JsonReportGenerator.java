package com.gsb.apitest.report;

import com.gsb.apitest.util.JsonUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class JsonReportGenerator {

    private JsonReportGenerator() {
    }

    public static String toJson(ReportCollector collector) {
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("generatedAt", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(new Date()));

        List<Map<String, Object>> classes = collector.getClassRecords().stream()
                .map(JsonReportGenerator::classToMap)
                .collect(Collectors.toList());
        report.put("testClasses", classes);

        int totalMethods = classes.stream().mapToInt(c -> (int) c.get("totalMethods")).sum();
        int passedMethods = classes.stream().mapToInt(c -> (int) c.get("passedMethods")).sum();
        int failedMethods = classes.stream().mapToInt(c -> (int) c.get("failedMethods")).sum();
        int totalSteps = classes.stream().mapToInt(c -> (int) c.get("totalSteps")).sum();

        report.put("summary", Map.of(
                "totalClasses", classes.size(),
                "totalMethods", totalMethods,
                "passedMethods", passedMethods,
                "failedMethods", failedMethods,
                "totalSteps", totalSteps
        ));

        return JsonUtils.toPrettyJson(report);
    }

    public static void writeToFile(ReportCollector collector, String reportDir) {
        try {
            Path dir = Paths.get(reportDir);
            Files.createDirectories(dir);
            String timestamp = new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date());
            Path file = dir.resolve("api-test-report-" + timestamp + ".json");
            Files.writeString(file, toJson(collector), StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println("写入报告文件失败: " + e.getMessage());
        }
    }

    private static Map<String, Object> classToMap(TestClassRecord classRec) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("className", classRec.getClassName());
        map.put("startTime", classRec.getStartTime());

        List<Map<String, Object>> methods = classRec.getMethods().stream()
                .map(JsonReportGenerator::methodToMap)
                .collect(Collectors.toList());
        map.put("methods", methods);
        map.put("totalMethods", methods.size());
        map.put("passedMethods", classRec.getPassedMethods());
        map.put("failedMethods", classRec.getFailedMethods());
        map.put("totalSteps", classRec.getTotalSteps());
        return map;
    }

    private static Map<String, Object> methodToMap(TestMethodRecord methodRec) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("methodName", methodRec.getMethodName());
        map.put("passed", methodRec.isPassed());
        map.put("durationMs", methodRec.getDurationMs());
        map.put("errorMessage", methodRec.getErrorMessage());

        List<Map<String, Object>> steps = methodRec.getSteps().stream()
                .map(JsonReportGenerator::stepToMap)
                .collect(Collectors.toList());
        map.put("steps", steps);
        return map;
    }

    private static Map<String, Object> stepToMap(StepRecord step) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("name", step.getName());
        map.put("method", step.getMethod());
        map.put("url", step.getUrl());
        map.put("statusCode", step.getStatusCode());
        map.put("responseTimeMs", step.getResponseTimeMs());
        map.put("totalTimeMs", step.getTotalTimeMs());
        map.put("success", step.isSuccess());
        map.put("errorMessage", step.getErrorMessage());
        map.put("assertionTotal", step.getAssertionTotal());
        map.put("assertionPassed", step.getAssertionPassed());
        map.put("assertionFailed", step.getAssertionFailed());
        map.put("extractions", step.getExtractions());
        return map;
    }
}
