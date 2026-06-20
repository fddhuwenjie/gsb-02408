package com.gsb.apitest;

import com.gsb.apitest.config.TestConfig;
import com.gsb.apitest.context.ContextHolder;
import com.gsb.apitest.context.TestContext;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.io.IOException;

public class MockServerTestSupport {

    protected MockWebServer server;

    @BeforeEach
    void setUpServer() throws IOException {
        server = new MockWebServer();
        server.start();

        TestConfig config = new TestConfig();
        config.setBaseUrl(server.url("/").toString());
        config.setLogRequest(false);
        config.setLogResponse(false);
        config.setReportEnabled(false);
        ContextHolder.init(config);
        ContextHolder.get().enterClass(getClass().getSimpleName());
    }

    @AfterEach
    void tearDownServer() throws IOException {
        TestContext ctx = ContextHolder.get();
        if (ctx != null) {
            ctx.exitClass();
        }
        ContextHolder.clear();
        if (server != null) {
            server.shutdown();
        }
    }

    protected void enqueueResponse(int statusCode, String body) {
        server.enqueue(new MockResponse()
                .setResponseCode(statusCode)
                .addHeader("Content-Type", "application/json;charset=UTF-8")
                .setBody(body));
    }

    protected String baseUrl() {
        return server.url("/").toString();
    }
}
