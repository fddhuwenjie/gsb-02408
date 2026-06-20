package com.gsb.apitest.template;

import com.gsb.apitest.context.TestContext;
import com.gsb.apitest.util.Base64Utils;
import com.gsb.apitest.util.FileUtils;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class FileToBase64Function implements TemplateFunction {

    @Override
    public String name() {
        return "fileToBase64";
    }

    @Override
    public String execute(List<String> args, TestContext context) {
        if (args == null || args.isEmpty()) {
            throw new IllegalArgumentException("fileToBase64() 需要一个文件路径参数");
        }
        String filePath = args.get(0).trim();
        if (filePath.startsWith("'") && filePath.endsWith("'")) {
            filePath = filePath.substring(1, filePath.length() - 1);
        } else if (filePath.startsWith("\"") && filePath.endsWith("\"")) {
            filePath = filePath.substring(1, filePath.length() - 1);
        }

        try {
            if (FileUtils.resourceExists(filePath)) {
                return Base64Utils.encodeFromResource(filePath);
            }
            if (Files.exists(Paths.get(filePath))) {
                return Base64Utils.encode(FileUtils.readFileBytes(filePath));
            }
            return Base64Utils.encodeFromResource(filePath);
        } catch (Exception e) {
            throw new RuntimeException("fileToBase64 读取文件失败: " + filePath, e);
        }
    }
}
