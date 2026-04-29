package com.acme.assistant.tool.file;

import com.acme.assistant.tool.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class FileReadTool extends AbstractTool {

    private final PathValidator pathValidator;

    public FileReadTool(PathValidator pathValidator) {
        super(new ToolDefinition(
                "file_read",
                "지정한 경로의 파일 내용을 읽어 반환한다. offset과 limit으로 읽을 범위를 지정할 수 있다.",
                JsonSchemaBuilder.objectSchema()
                        .property("path", "string", "읽을 파일의 경로")
                        .property("offset", "integer", "읽기 시작할 줄 번호 (0부터 시작, 기본값: 0)")
                        .property("limit", "integer", "읽을 최대 줄 수 (기본값 -1: 전체)")
                        .required("path")
                        .build()
        ));
        this.pathValidator = pathValidator;
    }

    @Override
    public ToolResult execute(ToolInput input, ToolContext context) {
        try {
            String pathStr = input.requireString("path");
            int offset = input.optionalInt("offset", 0);
            int limit = input.optionalInt("limit", -1);

            Path resolved = pathValidator.validate(pathStr);

            List<String> allLines = Files.readAllLines(resolved);

            if (offset >= allLines.size()) {
                return ToolResult.success("(offset이 파일 끝을 초과합니다)");
            }

            var stream = allLines.stream().skip(offset);
            if (limit >= 0) {
                stream = stream.limit(limit);
            }

            String content = String.join("\n", stream.toList());

            // 파일 추적기가 있으면 읽기 작업을 기록한다
            context.getMetadata("fileTracker")
                    .filter(obj -> obj instanceof FileTracker)
                    .map(obj -> (FileTracker) obj)
                    .ifPresent(tracker ->
                            tracker.record(pathStr, FileOperation.READ));

            return ToolResult.success(content);
        } catch (SecurityException e) {
            return ToolResult.error("경로 접근 거부: " + e.getMessage());
        } catch (Exception e) {
            return ToolResult.error("파일 읽기 오류: " + e.getMessage());
        }
    }
}
