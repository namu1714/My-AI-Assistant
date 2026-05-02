package com.acme.assistant.llm;

import java.util.List;

public record ChatMessage(
        Role role,
        String content,
        List<LlmToolCall> toolCalls,
        String toolCallId
) {
    public ChatMessage(Role role, String content) {
        this(role, content, null, null);
    }

    public static ChatMessage ofSystem(String content) {
        return new ChatMessage(Role.SYSTEM, content);
    }

    public static ChatMessage ofUser(String content) {
        return new ChatMessage(Role.USER, content);
    }

    public static ChatMessage ofAssistant(String content) {
        return new ChatMessage(Role.ASSISTANT, content);
    }

    public static ChatMessage ofAssistant(String content, List<LlmToolCall> toolCalls) {
        return new ChatMessage(Role.ASSISTANT, content, toolCalls, null);
    }

    public static ChatMessage ofTool(String toolCallId, String content) {
        return new ChatMessage(Role.TOOL, content, null, toolCallId);
    }
}
