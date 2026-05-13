package com.acme.assistant.agent;

public record AgentRequest(String message) {

    public AgentRequest {
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("message must not be blank");
        }
    }
}
