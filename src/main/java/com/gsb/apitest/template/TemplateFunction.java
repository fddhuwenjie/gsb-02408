package com.gsb.apitest.template;

import com.gsb.apitest.context.TestContext;

import java.util.List;

public interface TemplateFunction {

    String name();

    String execute(List<String> args, TestContext context);
}
