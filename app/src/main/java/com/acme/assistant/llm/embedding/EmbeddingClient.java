package com.acme.assistant.llm.embedding;

import java.util.List;

public interface EmbeddingClient {

    EmbeddingResponse embed(EmbeddingModel model, List<String> texts);

    default EmbeddingResponse embed(EmbeddingModel model, String text) {
        return embed(model, List.of(text));
    }
}
