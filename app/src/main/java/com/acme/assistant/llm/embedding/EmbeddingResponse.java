package com.acme.assistant.llm.embedding;

import com.acme.assistant.llm.TokenUsage;

import java.util.List;

public record EmbeddingResponse(
        List<float[]> embeddings,
        TokenUsage tokenUsage
) {
    public float[] getFirst() {
        return embeddings.getFirst();
    }
}
