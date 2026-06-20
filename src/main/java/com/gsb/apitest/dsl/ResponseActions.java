package com.gsb.apitest.dsl;

import com.gsb.apitest.asserts.AssertionEngine;
import com.gsb.apitest.context.ExecutionContext;
import com.gsb.apitest.context.VariableScope;
import com.gsb.apitest.extract.JsonPathExtractor;
import com.gsb.apitest.model.ApiResponse;

public class ResponseActions {
    private final ApiResponse response;
    private final JsonPathExtractor extractor;
    private final ExecutionContext context;
    private final AssertionEngine assertions;

    public ResponseActions(ApiResponse response, JsonPathExtractor extractor, ExecutionContext context) {
        this.response = response;
        this.extractor = extractor;
        this.context = context;
        this.assertions = new AssertionEngine(response);
    }

    public AssertionEngine then() {
        return assertions;
    }

    public ResponseActions saveAs(String variableName) {
        context.setVariable(variableName, response.getBody());
        return this;
    }

    public ResponseActions statusCode(int code) {
        assertions.statusCodeIs(code);
        return this;
    }

    public ResponseActions extract(String jsonPath, String variableName) {
        return extract(jsonPath, variableName, VariableScope.TEST);
    }

    public ResponseActions extract(String jsonPath, String variableName, VariableScope scope) {
        Object value = extractor.read(jsonPath);
        context.setVariable(scope, variableName, value);
        return this;
    }

    public ResponseActions extractHeader(String headerName, String variableName) {
        String value = response.getHeader(headerName);
        context.setVariable(variableName, value);
        return this;
    }

    public ResponseActions extractToStep(String jsonPath, String variableName) {
        return extract(jsonPath, variableName, VariableScope.STEP);
    }

    public ResponseActions extractToGlobal(String jsonPath, String variableName) {
        return extract(jsonPath, variableName, VariableScope.GLOBAL);
    }

    public ResponseActions extractToSuite(String jsonPath, String variableName) {
        return extract(jsonPath, variableName, VariableScope.SUITE);
    }

    public <T> T jsonPath(String jsonPath) {
        return extractor.read(jsonPath);
    }

    public String jsonPathString(String jsonPath) {
        return extractor.readString(jsonPath);
    }

    public ApiResponse getResponse() {
        return response;
    }
}
