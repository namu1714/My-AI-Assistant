package com.acme.assistant.model.embedding;

import com.acme.assistant.llm.embedding.EmbeddingResponse;

import java.util.List;

public record EmbeddingRequest(
        String model,
        List<String> input,
        Integer dimensions
) {
    public EmbeddingRequest(String model, List<String> input) {
        this(model, input, null);
    }
}
