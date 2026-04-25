package com.acme.assistant.model;

import com.acme.assistant.model.tool.ToolCall;

import java.util.List;

public record Message(
        String role,
        Object content,
        List<ToolCall> toolCalls,
        String toolCallId
) {
    public Message(String role, Object content) {
        this(role, content, null, null);
    }

    public static Message ofSystem(String content) {
        return new Message("system", content);
    }

    public static Message ofUser(String content) {
        return new Message("user", content);
    }

    public static Message ofUser(List<ContentPart> parts) {
        return new Message("user", parts);
    }

    public static Message ofAssistant(String content) {
        return new Message("assistant", content);
    }

    public static Message ofAssistant(String content, List<ToolCall> toolCalls) {
        return new Message("assistant", content, toolCalls, null);
    }

    public static Message ofTool(String toolCallId, String content) {
        return new Message("tool", content, null, toolCallId);
    }
}