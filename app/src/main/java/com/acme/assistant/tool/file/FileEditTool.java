package com.acme.assistant.tool.file;

import com.acme.assistant.tool.*;

import java.nio.file.Files;
import java.nio.file.Path;

public class FileEditTool extends AbstractTool {

    private final PathValidator pathValidator;

    public FileEditTool(PathValidator pathValidator) {
        super(new ToolDefinition(
                "file_edit",
                "파일에서 지정한 문자열을 찾아 새 문자열로 교체한다. "
                        + "전체 파일을 다시 쓰지 않고 정확한 부분만 수정한다.",
                JsonSchemaBuilder.objectSchema()
                        .property("path", "string", " 수정할 파일의 경로")
                        .property("old_text", "string", " 교체할 기존 문자열")
                        .property("new_text", "string", " 새로 넣을 문자열")
                        .required("path", "old_text", "new_text")
                        .build()
        ));
        this.pathValidator = pathValidator;
    }

    @Override
    public ToolResult execute(ToolInput input, ToolContext context) {
        try {
            String pathStr = input.requireString("path");
            String oldText = input.requireString("old_text");
            String newText = input.requireString("new_text");

            Path resolved = pathValidator.validate(pathStr);

            if (!Files.exists(resolved)) {
                return ToolResult.error("파일이 존재하지 않습니다: " + pathStr);
            }

            var content = Files.readString(resolved);

            if (!content.contains(oldText)) {
                return ToolResult.error("old_text 를 파일에서 찾을 수 없습니다");
            }

            int count = countOccurrences(content, oldText);
            if (count > 1) {
                return ToolResult.error(
                        "old_text가 " + count + "번 발견되었습니다. "
                                + "더 긴 문자열로 고유하게 지정해 주세요.");
            }

            String updated = content.replace(oldText, newText);
            Files.writeString(resolved, updated);

            context.getMetadata("fileTracker")
                    .filter(obj -> obj instanceof FileTracker)
                    .map(obj -> (FileTracker) obj)
                    .ifPresent(tracker ->
                            tracker.record(pathStr, FileOperation.EDIT));

            return ToolResult.success("파일 수정 완료: " + pathStr);
        } catch (SecurityException e) {
            return ToolResult.error("경로 접근 거부: " + e.getMessage());
        } catch (Exception e) {
            return ToolResult.error("파일 수정 오류: " + e.getMessage());
        }
    }

    private int countOccurrences(String text, String target) {
        int count = 0;
        int index = 0;
        while ((index = text.indexOf(target, index)) != -1) {
            count++;
            index += target.length();
        }
        return count;
    }
}
