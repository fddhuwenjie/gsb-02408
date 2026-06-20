package com.gsb.apitest.examples;

import com.gsb.apitest.context.TestContext;
import com.gsb.apitest.http.HttpResponse;
import com.gsb.apitest.support.MockBackedApiTest;
import com.gsb.apitest.template.TemplateRenderer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * 示例 3：fileToBase64 + 文件模板组合用法。
 * 业务侧只需要把请求 JSON 模板放进 src/test/resources/templates，
 * 框架会自动把样本文件读进来 base64 编码再注入到请求体里。
 */
@DisplayName("示例3：从模板加载请求 + fileToBase64 替换")
class FileBase64ExampleTest extends MockBackedApiTest {

    @Override
    protected void prepareContext(TestContext ctx) {
        ctx.setVar("fileName", "report.txt");
        ctx.setVar("userId", "u-100");
    }

    @Test
    @DisplayName("upload 接口会收到 base64 后的文件内容")
    void uploadTemplateShouldEncodeFileToBase64() {
        // 先算出预期 base64，便于后续断言
        String expectedBase64 = TemplateRenderer.fileToBase64("samples/sample-payload.txt");
        assertNotNull(expectedBase64);

        HttpResponse response = step("upload")
                .post("/upload")
                .bodyFromTemplate("templates/upload_request.json")
                .assertThat(ra -> ra
                        .statusCode(200)
                        .jsonPathEquals("$.name", "report.txt")
                        .jsonPathEquals("$.contentLength", expectedBase64.length()))
                .run();

        assertEquals(200, response.getStatusCode());
    }
}
