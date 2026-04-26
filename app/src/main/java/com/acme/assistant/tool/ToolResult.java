package com.acme.assistant.tool;

public record ToolResult(String content, boolean isError) {

    public static ToolResult success(String content) {
        return new ToolResult(content, false);
    }

    public static ToolResult error(String message) {
        return new ToolResult(message, true);
    }
}
