package com.acme.assistant.model;

import java.util.List;

public record ChatRequest(
        String model,
        List<Message> messages,
        Double temperature,
        Integer maxTokens
) {
    public ChatRequest(String model, List<Message> messages) {
        this(model, messages, null, null);
    }
}
