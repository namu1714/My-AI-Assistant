package com.acme.assistant.rag;

import java.util.Map;

public record Document(
        String id,
        String content,
        Map<String, String> metadata
) {
    public Document(String id, String content) {
        this(id, content, Map.of());
    }
}
