package com.acme.assistant.tool;

import com.acme.assistant.llm.LlmToolCall;
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

    public static ToolUse from(LlmToolCall llmToolCall) {
        return new ToolUse(
                llmToolCall.id(),
                llmToolCall.name(),
                llmToolCall.arguments()
        );
    }
}
