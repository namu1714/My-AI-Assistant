package com.acme.assistant.llm;

import java.util.List;

public record LlmResponse(
        String content,
        List<LlmToolCall> toolCalls,
        TokenUsage tokenUsage
) {
    public LlmResponse(String content, TokenUsage tokenUsage) {
        this(content, List.of(), tokenUsage);
    }

    public boolean hasToolCalls() {
        return toolCalls != null && !toolCalls.isEmpty();
    }

    public ChatMessage toAssistantMessage() {
        if(hasToolCalls()) {
            return ChatMessage.ofAssistant(content, toolCalls);
        } else {
            return ChatMessage.ofAssistant(content);
        }
    }
}
