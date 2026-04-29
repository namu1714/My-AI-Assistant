package com.acme.assistant.tool.file;

import com.acme.assistant.tool.*;

import java.util.List;

public class FileTrackerTool extends AbstractTool {

    private final FileTracker fileTracker;

    public FileTrackerTool(FileTracker fileTracker) {
        super(new ToolDefinition(
                "file_tracker",
                "지금까지 작업한 파일 목록과 요약을 조회한다.",
                JsonSchemaBuilder.objectSchema()
                        .enumProperty("action",
                                List.of("summary", "list"),
                                "summary: 요약 보기, list: 전체 기록 보기")
                        .required("action")
                        .build()
        ));
        this.fileTracker = fileTracker;
    }

    @Override
    public ToolResult execute(ToolInput input, ToolContext context) {
        try {
            String action = input.requireString("action");

            return switch (action) {
                case "summary" -> ToolResult.success(fileTracker.summary());
                case "list" -> listRecords();
                default -> ToolResult.error("알 수 없는 동작: " + action);
            };
        } catch (Exception e) {
            return ToolResult.error("파일 추적 오류: " + e.getMessage());
        }
    }

    private ToolResult listRecords() {
        var records = fileTracker.getRecords();
        if (records.isEmpty()) {
            return ToolResult.success("파일 작업 기록이 없습니다.");
        }
        var sb = new StringBuilder();
        for (FileRecord record : records) {
            if (!sb.isEmpty()) sb.append("\n");
            sb.append(record);
        }
        return ToolResult.success(sb.toString());
    }
}
