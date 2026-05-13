package com.acme.assistant.agent;

import java.util.Map;

public record ExecutionContext(
        String conversationId,
        String userId,
        int maxIterations,
        Map<String, Object> metadata
) {
    public static final int DEFAULT_MAX_ITERATIONS = 10;

    public ExecutionContext(String conversationId) {
        this(conversationId, null, DEFAULT_MAX_ITERATIONS, Map.of());
    }

    public ExecutionContext {
        if (maxIterations <= 0) {
            throw new IllegalArgumentException("maxIterations must be positive");
        }
        if (metadata == null) {
            metadata = Map.of();
        }
    }
}
