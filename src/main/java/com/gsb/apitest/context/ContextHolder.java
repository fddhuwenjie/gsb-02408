package com.gsb.apitest.context;

import com.gsb.apitest.config.ConfigLoader;
import com.gsb.apitest.config.TestConfig;

public final class ContextHolder {

    private static final ThreadLocal<TestContext> CONTEXT = new ThreadLocal<>();

    private ContextHolder() {
    }

    public static void init(TestConfig config) {
        if (config == null) {
            config = ConfigLoader.load();
        }
        CONTEXT.set(new TestContext(config));
    }

    public static TestContext get() {
        TestContext ctx = CONTEXT.get();
        if (ctx == null) {
            ctx = new TestContext(ConfigLoader.load());
            CONTEXT.set(ctx);
        }
        return ctx;
    }

    public static boolean exists() {
        return CONTEXT.get() != null;
    }

    public static void clear() {
        TestContext ctx = CONTEXT.get();
        if (ctx != null) {
            ctx.exitStep();
            ctx.exitClass();
        }
        CONTEXT.remove();
    }
}
