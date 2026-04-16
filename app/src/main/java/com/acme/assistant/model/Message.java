package com.acme.assistant.model;

public record Message(
        String role,
        String content
) {
    public static Message ofSystem(String content) {
        return new Message("system", content);
    }

    public static Message ofUser(String content) {
        return new Message("user", content);
    }

    public static Message ofAssistant(String content) {
        return new Message("assistant", content);
    }
}