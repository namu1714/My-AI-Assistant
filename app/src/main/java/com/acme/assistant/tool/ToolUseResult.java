package com.acme.assistant.tool;

import com.acme.assistant.model.Message;

public record ToolUseResult(
        String toolCallId,
        String content,
        boolean isError
) {
    public static ToolUseResult success(String toolCallId, String content) {
        return new ToolUseResult(toolCallId, content, false);
    }

    public static ToolUseResult error(String toolCallId, String content) {
        return new ToolUseResult(toolCallId, content, true);
    }

    public Message toMessage() {
        return Message.ofTool(toolCallId, content);
    }
}
