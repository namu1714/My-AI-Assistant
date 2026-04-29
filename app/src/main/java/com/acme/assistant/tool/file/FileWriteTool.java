package com.acme.assistant.tool.file;

import com.acme.assistant.tool.*;

import java.nio.file.Files;
import java.nio.file.Path;

public class FileWriteTool extends AbstractTool {

    private final PathValidator pathValidator;

    public FileWriteTool(PathValidator pathValidator) {
        super(new ToolDefinition(
                "file_write",
                "지정한 경로에 파일을 작성한다. "
                        + "부모 디렉터리가 없으면 자동으로 생성한다. ",
                JsonSchemaBuilder.objectSchema()
                        .property("path", "string", "작성할 파일의 경로")
                        .property("content", "string", "파일에 작성할 내용")
                        .required("path", "content")
                        .build()
        ));
        this.pathValidator = pathValidator;
    }

    @Override
    public ToolResult execute(ToolInput input, ToolContext context) {
        try {
            String pathStr = input.requireString("path");
            String content = input.requireString("content");

            Path resolved = pathValidator.validate(pathStr);

            Path parent = resolved.getParent();
            if (parent != null && !Files.exists(parent)) {
                Files.createDirectories(parent);
            }
            Files.writeString(resolved, content);

            context.getMetadata("fileTracker")
                    .filter(obj -> obj instanceof FileTracker)
                    .map(obj -> (FileTracker) obj)
                    .ifPresent(tracker ->
                            tracker.record(pathStr, FileOperation.WRITE));

            return ToolResult.success("파일 작성 완료: " + pathStr);
        } catch (SecurityException e) {
            return ToolResult.error("경로 접근 거부: " + e.getMessage());
        } catch (Exception e) {
            return ToolResult.error("파일 작성 오류: " + e.getMessage());
        }
    }
}
