package com.acme.assistant.tool.todo;

import com.acme.assistant.tool.*;

import java.util.List;

public class TodoTool extends AbstractTool {

    private final TodoStore store;

    public TodoTool(TodoStore store) {
        super(new ToolDefinition(
                "todo",
                " 작업 목록을 관리한다. "
                        + "add, list, complete, delete 동작을 지원한다.",
                JsonSchemaBuilder.objectSchema()
                        .enumProperty("action",
                                List.of("add", "list", "complete", "delete"),
                                "수행할 동작")
                        .property("title", "string",
                                "작업 제목 (add시 필수)")
                        .property("id", "integer",
                                "작업 ID (complete, delete시 필수)")
                        .required("action")
                        .build()
        ));
        this.store = store;
    }

    @Override
    public ToolResult execute(ToolInput input, ToolContext context) {
        try {
            String action = input.requireString("action");

            return switch (action) {
                case "add" -> add(input);
                case "list" -> list();
                case "complete" -> complete(input);
                case "delete" -> delete(input);
                default -> ToolResult.error("알 수 없는 동작: " + action);
            };
        } catch (Exception e) {
            return ToolResult.error("작업 관리 오류: " + e.getMessage());
        }
    }

    private ToolResult add(ToolInput input) {
        String title = input.requireString("title");
        TodoItem item = store.add(title);
        return ToolResult.success("작업 추가됨: " + item);
    }

    private ToolResult list() {
        List<TodoItem> items = store.list();
        if (items.isEmpty()) {
            return ToolResult.success(
                    " 작업 목록이 비어 있습니다.");
        }
        var sb = new StringBuilder();
        for (TodoItem item : items) {
            if (!sb.isEmpty()) sb.append("\n");
            sb.append(item);
        }
        return ToolResult.success(sb.toString());
    }

    private ToolResult complete(ToolInput input) {
        int id = input.requireInt("id");
        return store.complete(id)
                .map(item ->
                        ToolResult.success("작업 완료: " + item))
                .orElse(ToolResult.error(
                        "작업을 찾을 수 없습니다: #" + id));
    }

    private ToolResult delete(ToolInput input) {
        int id = input.requireInt("id");
        if (store.delete(id)) {
            return ToolResult.success(" 작업 삭제됨: #" + id);
        }
        return ToolResult.error(
                " 작업을 찾을 수 없습니다: #" + id);
    }
}
