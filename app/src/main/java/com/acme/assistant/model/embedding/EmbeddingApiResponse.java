package com.acme.assistant.model.embedding;

import com.acme.assistant.model.Usage;

import java.util.List;

public record EmbeddingApiResponse(
        List<EmbeddingData> data,
        Usage usage
) {
    public record EmbeddingData(
            int index,
            float[] embedding
    ) {}
}
