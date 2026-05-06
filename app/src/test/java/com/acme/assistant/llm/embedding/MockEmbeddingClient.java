package com.acme.assistant.llm.embedding;

import com.acme.assistant.exception.LlmException;
import com.acme.assistant.llm.TokenUsage;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;

public class MockEmbeddingClient implements EmbeddingClient {

    private final Queue<EmbeddingResponse> responses = new ArrayDeque<>();

    public void enqueue(EmbeddingResponse response) {
        responses.add(response);
    }

    public void enqueue(float[]... embeddings) {
        enqueue(new EmbeddingResponse(
                List.of(embeddings), TokenUsage.EMPTY));
    }

    @Override
    public EmbeddingResponse embed(EmbeddingModel model, List<String> texts) {
        if (responses.isEmpty()) {
            throw new LlmException("mock", "응답 큐가 비어 있습니다. enqueue()로 응답을 추가하세요.");
        }
        return responses.poll();
    }
}
