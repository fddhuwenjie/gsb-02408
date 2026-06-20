package com.gsb.apitest;

import com.gsb.apitest.config.TestConfiguration;
import com.gsb.apitest.context.ExecutionContext;
import com.gsb.apitest.dsl.StepRecorder;
import com.gsb.apitest.extension.ApiTestExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ExtendWith(ApiTestExtension.class)
public abstract class ApiTestBase {
    protected final Logger log = LoggerFactory.getLogger(getClass());

    protected StepRecorder steps() {
        return new StepRecorder(ExecutionContext.getCurrent());
    }

    protected ExecutionContext context() {
        return ExecutionContext.getCurrent();
    }

    protected TestConfiguration config() {
        return TestConfiguration.getInstance();
    }

    protected void setBaseUrl(String baseUrl) {
        TestConfiguration.getInstance().setBaseUrl(baseUrl);
    }

    protected void setGlobalHeader(String key, String value) {
        TestConfiguration.getInstance().addDefaultHeader(key, value);
    }

    protected void setGlobalVariable(String key, Object value) {
        context().set(key, value);
    }

    protected Object var(String key) {
        return context().get(key);
    }

    protected String varStr(String key) {
        return context().getString(key);
    }
}
