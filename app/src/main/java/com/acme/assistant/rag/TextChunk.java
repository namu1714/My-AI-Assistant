package com.acme.assistant.rag;

import java.util.Map;

public record TextChunk(
        String id,
        String documentId,
        String content,
        Map<String, String> metadata
) {
    public TextChunk(String id, String documentId, String content) {
        this(id, documentId, content, Map.of());
    }
}
