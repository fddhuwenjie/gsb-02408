package com.gsb.apitest.report;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.gsb.apitest.context.TestContext;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 报告输出器：把一个 TestContext 内累积的所有步骤记录序列化成 JSON 文件，
 * 落到 apitest.report.dir 下。报告文件名按 testClass#testMethod 命名。
 */
public class ReportWriter {

    private static final ObjectMapper JSON = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);

    public static Path writeReport(TestContext ctx) {
        String reportDir = ctx.config().getReportDir();
        Path dir = Paths.get(reportDir);
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            throw new IllegalStateException("cannot create report dir: " + reportDir, e);
        }
        String safeName = (ctx.getTestClassName() + "_" + ctx.getTestMethodName())
                .replaceAll("[^a-zA-Z0-9_.-]", "_");
        Path target = dir.resolve(safeName + ".json");

        List<StepRecord> records = ctx.getStepRecords();
        Map<String, Object> root = new LinkedHashMap<>();
        root.put("testClass", ctx.getTestClassName());
        root.put("testMethod", ctx.getTestMethodName());
        root.put("totalSteps", records.size());
        root.put("status", computeOverall(records));
        root.put("steps", records.stream().map(ReportWriter::toMap).collect(Collectors.toList()));

        try {
            byte[] bytes = JSON.writeValueAsBytes(root);
            Files.write(target, bytes);
            // 同步在 stdout 打一行摘要，便于在 CI 控制台看到位置
            System.out.println("[apitest][report] " + target.toAbsolutePath());
            return target;
        } catch (IOException e) {
            throw new IllegalStateException("failed to write report: " + target, e);
        }
    }

    private static String computeOverall(List<StepRecord> records) {
        for (StepRecord r : records) {
            if (r.getStatus() == StepRecord.Status.FAILED
                    || r.getStatus() == StepRecord.Status.ERROR) {
                return r.getStatus().name();
            }
        }
        return "PASSED";
    }

    private static Map<String, Object> toMap(StepRecord r) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("step", r.getStepName());
        m.put("startedAt", r.getStartedAt().toString());
        m.put("elapsedMs", r.getElapsedMs());
        m.put("status", r.getStatus().name());
        m.put("requestMethod", r.getRequestMethod());
        m.put("requestUrl", r.getRequestUrl());
        m.put("requestHeaders", r.getRequestHeaders());
        m.put("requestBody", truncate(r.getRequestBody()));
        m.put("responseStatus", r.getResponseStatus());
        m.put("responseBody", truncate(r.getResponseBody()));
        m.put("extractedVars", r.getExtractedVars());
        if (r.getAssertionFailures() != null) {
            m.put("assertionFailures", r.getAssertionFailures());
        }
        if (r.getErrorMessage() != null) {
            m.put("errorMessage", r.getErrorMessage());
        }
        return m;
    }

    private static String truncate(String s) {
        if (s == null) return null;
        int max = 8 * 1024;
        if (s.length() <= max) return s;
        return s.substring(0, max) + "...<truncated " + (s.length() - max) + " chars>";
    }

    /** 给业务侧的便捷工具：把任意字符串再写一份到报告目录便于回查。 */
    public static void writeAttachment(TestContext ctx, String name, String content) {
        Path dir = Paths.get(ctx.config().getReportDir(), "attachments");
        try {
            Files.createDirectories(dir);
            Files.write(dir.resolve(name), content.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new IllegalStateException("failed to write attachment", e);
        }
    }
}
