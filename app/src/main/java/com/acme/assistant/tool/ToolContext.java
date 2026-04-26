package com.acme.assistant.tool;

import java.util.Map;
import java.util.Optional;

public record ToolContext(
        String conversationId,
        String userId,
        Map<String, Object> metadata
) {
    public ToolContext {
        metadata = metadata != null ? Map.copyOf(metadata) : Map.of();
    }

    public static ToolContext empty() {
        return new ToolContext(null, null, Map.of());
    }

    public static ToolContext of(String conversationId, String userId) {
        return new ToolContext(conversationId, userId, Map.of());
    }

    public Optional<Object> getMetadata(String key) {
        return Optional.ofNullable(metadata.get(key));
    }
}
