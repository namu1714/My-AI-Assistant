package com.acme.assistant.tool;

import com.acme.assistant.model.tool.ToolCall;

public record ToolUse(
        String id,
        String name,
        String arguments
) {
    public static ToolUse from(ToolCall toolCall) {
        return new ToolUse(
                toolCall.id(),
                toolCall.function().name(),
                toolCall.function().arguments()
        );
    }
}
