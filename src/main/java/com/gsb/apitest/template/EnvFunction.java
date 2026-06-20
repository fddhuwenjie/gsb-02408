package com.gsb.apitest.template;

import com.gsb.apitest.context.TestContext;

import java.util.List;

public class EnvFunction implements TemplateFunction {

    @Override
    public String name() {
        return "env";
    }

    @Override
    public String execute(List<String> args, TestContext context) {
        if (args == null || args.isEmpty()) {
            throw new IllegalArgumentException("env() 需要一个环境变量名参数");
        }
        String varName = args.get(0).trim();
        if (varName.startsWith("'") && varName.endsWith("'")) {
            varName = varName.substring(1, varName.length() - 1);
        } else if (varName.startsWith("\"") && varName.endsWith("\"")) {
            varName = varName.substring(1, varName.length() - 1);
        }
        String value = System.getenv(varName);
        if (value == null) {
            value = System.getProperty(varName, "");
        }
        return value;
    }
}
