package com.acme.assistant.model;

import java.util.List;

public record Message(
        String role,
        Object content
) {
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
}