package com.gsb.apitest.support;

import com.gsb.apitest.config.ConfigManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import java.io.IOException;
import java.lang.reflect.Method;

/**
 * 给业务测试类共用的基类：启动 / 关闭 mock server，
 * 并在所有用例之前把 baseUrl 注入到 ConfigManager。
 */
public abstract class MockBackedApiTest extends com.gsb.apitest.junit.AbstractApiTest {

    private static MockServer server;

    @BeforeAll
    static void __startMockServer() throws IOException {
        server = new MockServer();
        System.setProperty("apitest.baseUrl", server.baseUrl());
        // 强制重新加载配置以读取系统属性
        try {
            Method m = ConfigManager.class.getDeclaredMethod("resetForTesting");
            m.setAccessible(true);
            m.invoke(null);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @AfterAll
    static void __stopMockServer() {
        if (server != null) {
            server.close();
            server = null;
        }
    }

    public static String mockBaseUrl() {
        return server.baseUrl();
    }
}
