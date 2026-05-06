package com.acme.assistant.llm.embedding;

public record EmbeddingModel(
        String name,
        int dimensions
) {
    public EmbeddingModel(String name) {
        this(name, 0);
    }
}
