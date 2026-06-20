package com.gsb.apitest;

import com.gsb.apitest.config.TestConfig;
import com.gsb.apitest.context.ContextHolder;
import com.gsb.apitest.http.HttpResponse;
import com.gsb.apitest.step.ApiFlow;
import com.gsb.apitest.util.Base64Utils;
import com.gsb.apitest.util.FileUtils;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class FileToBase64Test extends MockServerTestSupport {

    @Test
    void testFileToBase64InTemplate() throws Exception {
        enqueueResponse(200, "{\n" +
                "  \"code\": 0,\n" +
                "  \"message\": \"upload success\",\n" +
                "  \"data\": {\n" +
                "    \"fileId\": \"FILE-ABC-123\",\n" +
                "    \"fileSize\": 256\n" +
                "  }\n" +
                "}");

        ApiFlow.setVar("fileName", "sample.txt");
        ApiFlow.setVar("fileType", "text/plain");
        ApiFlow.setVar("description", "测试文件上传");

        HttpResponse response = ApiFlow.post("/api/file/upload")
                .name("文件上传")
                .template("upload")
                .expectStatus(200)
                .expect(a -> a
                        .jsonPath("$.code", 0)
                        .jsonPath("$.message", "upload success")
                        .jsonPathNotNull("$.data.fileId")
                )
                .extract("fileId", "$.data.fileId")
                .execute();

        assertEquals("FILE-ABC-123", response.jsonPath("$.data.fileId"));
        assertEquals("FILE-ABC-123", ApiFlow.varStr("fileId"));

        RecordedRequest request = server.takeRequest();
        assertEquals("/api/file/upload", request.getPath());

        String requestBody = request.getBody().readUtf8();
        String expectedBase64 = Base64Utils.encode(FileUtils.readResourceBytes("files/sample.txt"));

        assertTrue(requestBody.contains("\"fileName\":\"sample.txt\""));
        assertTrue(requestBody.contains("\"fileType\":\"text/plain\""));
        assertTrue(requestBody.contains("\"description\":\"测试文件上传\""));
        assertTrue(requestBody.contains("\"fileContent\":\"" + expectedBase64 + "\""),
                "请求体应包含fileToBase64()编码后的Base64字符串");

        String decodedContent = Base64Utils.decode(expectedBase64);
        assertTrue(decodedContent.contains("Hello, this is a test file"));
        assertTrue(decodedContent.contains("fileToBase64() function works!"));
    }

    @Test
    void testFileToBase64ReusedVariable() throws Exception {
        enqueueResponse(200, "{\n" +
                "  \"code\": 0,\n" +
                "  \"data\": { \"fileId\": \"F001\" }\n" +
                "}");
        enqueueResponse(200, "{\n" +
                "  \"code\": 0,\n" +
                "  \"data\": { \"attachId\": \"A002\", \"originalFileId\": \"${fileId}\" }\n" +
                "}");

        ApiFlow.post("/api/file/upload")
                .name("步骤1-上传文件")
                .body("{\"name\":\"doc.txt\",\"content\":\"${fileToBase64('files/sample.txt')}\"}")
                .expectStatus(200)
                .extract("fileId", "$.data.fileId")
                .execute();

        assertEquals("F001", ApiFlow.varStr("fileId"));

        RecordedRequest uploadReq = server.takeRequest();
        String uploadBody = uploadReq.getBody().readUtf8();
        String expectedBase64 = Base64Utils.encode(FileUtils.readResourceBytes("files/sample.txt"));
        assertTrue(uploadBody.contains(expectedBase64));

        HttpResponse attachResp = ApiFlow.post("/api/attachment/bind")
                .name("步骤2-绑定附件")
                .body("{\"fileId\":\"${fileId}\",\"type\":\"REPORT\"}")
                .expectStatus(200)
                .expect(a -> a.jsonPath("$.code", 0))
                .execute();

        RecordedRequest bindReq = server.takeRequest();
        String bindBody = bindReq.getBody().readUtf8();
        assertTrue(bindBody.contains("\"fileId\":\"F001\""));
        assertTrue(bindBody.contains("\"type\":\"REPORT\""));
    }

    @Test
    void testConcurrentContextIsolation() throws Exception {
        int threadCount = 5;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        List<String> failures = new ArrayList<>();

        for (int t = 0; t < threadCount; t++) {
            final int idx = t;
            executor.submit(() -> {
                MockWebServer localServer = null;
                try {
                    localServer = new MockWebServer();
                    localServer.start();

                    TestConfig config = new TestConfig();
                    config.setBaseUrl(localServer.url("/").toString());
                    config.setLogRequest(false);
                    config.setLogResponse(false);
                    config.setReportEnabled(false);
                    ContextHolder.init(config);
                    ContextHolder.get().enterClass("ConcurrentTest-" + idx);
                    ContextHolder.get().enterMethod("testRun-" + idx);

                    startLatch.await();

                    String threadToken = "THREAD-TOKEN-" + idx;
                    localServer.enqueue(new okhttp3.mockwebserver.MockResponse()
                            .setResponseCode(200)
                            .addHeader("Content-Type", "application/json")
                            .setBody("{\"code\":0,\"data\":{\"token\":\"" + threadToken + "\"}}"));
                    localServer.enqueue(new okhttp3.mockwebserver.MockResponse()
                            .setResponseCode(200)
                            .addHeader("Content-Type", "application/json")
                            .setBody("{\"code\":0,\"data\":{\"value\":\"" + threadToken + "\"}}"));

                    ApiFlow.setVar("user", "user" + idx);

                    ApiFlow.post("/login")
                            .body("{\"user\":\"${user}\"}")
                            .extract("token", "$.data.token")
                            .execute();

                    ApiFlow.post("/verify")
                            .body("{\"token\":\"${token}\"}")
                            .expect(a -> a.jsonPath("$.data.value", threadToken))
                            .execute();

                    String actualToken = ApiFlow.varStr("token");
                    if (!threadToken.equals(actualToken)) {
                        throw new AssertionError("线程 " + idx + " token串线: 期望=" + threadToken + " 实际=" + actualToken);
                    }

                    localServer.takeRequest();
                    localServer.takeRequest();

                    successCount.incrementAndGet();
                } catch (Exception | AssertionError e) {
                    synchronized (failures) {
                        failures.add("线程" + idx + ": " + e.getMessage());
                    }
                } finally {
                    ContextHolder.get().exitMethod();
                    ContextHolder.get().exitClass();
                    ContextHolder.clear();
                    if (localServer != null) {
                        try { localServer.shutdown(); } catch (Exception ignored) {}
                    }
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        doneLatch.await(15, TimeUnit.SECONDS);
        executor.shutdown();

        assertTrue(failures.isEmpty(), "并发执行不应有异常: " + failures);
        assertEquals(threadCount, successCount.get(), "所有线程应成功完成");
    }
}
