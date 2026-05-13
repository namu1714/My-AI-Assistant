package com.acme.assistant.agent;

public record Step(
        int order,
        String description,
        String toolName
) {
    public Step {
        if (order < 1) {
            throw new IllegalArgumentException("order must be positive");
        }
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("description must not be blank");
        }
    }
}
