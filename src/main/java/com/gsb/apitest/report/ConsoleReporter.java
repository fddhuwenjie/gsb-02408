package com.gsb.apitest.report;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ConsoleReporter {

    private static final Logger log = LoggerFactory.getLogger(ConsoleReporter.class);

    private static final String GREEN = "\u001B[32m";
    private static final String RED = "\u001B[31m";
    private static final String YELLOW = "\u001B[33m";
    private static final String CYAN = "\u001B[36m";
    private static final String RESET = "\u001B[0m";
    private static final String BOLD = "\u001B[1m";

    private ConsoleReporter() {
    }

    public static void printSummary(ReportCollector collector) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n").append(BOLD).append("╔════════════════════════════════════════════════════════════╗").append(RESET).append("\n");
        sb.append(BOLD).append("║               API 测试执行报告                              ║").append(RESET).append("\n");
        sb.append(BOLD).append("╚════════════════════════════════════════════════════════════╝").append(RESET).append("\n");

        int totalClasses = collector.getClassRecords().size();
        int totalMethods = 0;
        int passedMethods = 0;
        int failedMethods = 0;
        int totalSteps = 0;

        for (TestClassRecord classRec : collector.getClassRecords()) {
            sb.append("\n").append(CYAN).append("📦 ").append(classRec.getClassName()).append(RESET).append("\n");

            for (TestMethodRecord methodRec : classRec.getMethods()) {
                totalMethods++;
                totalSteps += methodRec.getSteps().size();
                String icon = methodRec.isPassed() ? "✅" : "❌";
                String color = methodRec.isPassed() ? GREEN : RED;
                if (methodRec.isPassed()) passedMethods++;
                else failedMethods++;

                sb.append(String.format("  %s %s%s%s (%dms, %d steps)%s\n",
                        icon, color, methodRec.getMethodName(), RESET,
                        methodRec.getDurationMs(), methodRec.getSteps().size(), RESET));

                for (StepRecord step : methodRec.getSteps()) {
                    String stepIcon = step.isSuccess() && step.getAssertionFailed() == 0 ? "  ✓" : "  ✗";
                    String stepColor = step.isSuccess() && step.getAssertionFailed() == 0 ? GREEN : RED;
                    sb.append(String.format("    %s %s[%s] %s → %d (%dms)%s\n",
                            stepColor, stepIcon, step.getMethod(), truncate(step.getName(), 30),
                            step.getStatusCode(), step.getResponseTimeMs(), RESET));
                    if (step.getErrorMessage() != null) {
                        sb.append(String.format("       %s⚠ %s%s\n", RED, truncate(step.getErrorMessage(), 100), RESET));
                    }
                }

                if (methodRec.getErrorMessage() != null && !methodRec.isPassed()) {
                    sb.append(String.format("    %s⚠ %s%s\n", RED, truncate(methodRec.getErrorMessage(), 150), RESET));
                }
            }
        }

        sb.append("\n").append(BOLD).append("────────────────────────────────────────────────────────────").append(RESET).append("\n");
        sb.append(String.format("  测试类: %d | 测试方法: %d (%s%d✓%s %s%d✗%s) | 步骤总数: %d\n",
                totalClasses,
                totalMethods, GREEN, passedMethods, RESET, RED, failedMethods, RESET, totalSteps));
        sb.append(BOLD).append("────────────────────────────────────────────────────────────").append(RESET).append("\n");

        if (failedMethods > 0) {
            sb.append(RED).append("  ❌ 存在失败的用例！").append(RESET).append("\n");
        } else {
            sb.append(GREEN).append("  ✨ 所有用例通过！").append(RESET).append("\n");
        }

        System.out.println(sb);
    }

    private static String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max) + "...";
    }
}
