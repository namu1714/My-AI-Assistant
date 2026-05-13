package com.acme.assistant.agent;

public record AgentMetadata(
        String name,
        String description,
        String version
) {
    public AgentMetadata(String name, String description) {
        this(name, description, "1.0.0");
    }

    public AgentMetadata {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
    }
}
