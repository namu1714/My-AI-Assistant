package com.acme.assistant.llm.embedding;

import com.acme.assistant.client.OpenAiClient;
import com.acme.assistant.exception.LlmException;
import com.acme.assistant.llm.TokenUsage;
import com.acme.assistant.model.Usage;
import com.acme.assistant.model.embedding.EmbeddingApiResponse;
import com.acme.assistant.model.embedding.EmbeddingRequest;

import java.util.Comparator;
import java.util.List;

public class OpenAiEmbeddingClient implements EmbeddingClient {

    private final OpenAiClient openAiClient;

    public OpenAiEmbeddingClient(OpenAiClient openAiClient) {
        this.openAiClient = openAiClient;
    }

    @Override
    public EmbeddingResponse embed(EmbeddingModel model, List<String> texts) {
        EmbeddingRequest request = toRequest(model, texts);
        try {
            EmbeddingApiResponse response = openAiClient.embeddings(request);
            return toEmbeddingResponse(response);
        } catch (Exception e) {
            throw new LlmException("openai", e.getMessage(), e);
        }
    }

    EmbeddingRequest toRequest(EmbeddingModel model, List<String> texts) {
        Integer dimensions = model.dimensions() > 0
                ? model.dimensions() : null;
        return new EmbeddingRequest(model.name(), texts, dimensions);
    }

    EmbeddingResponse toEmbeddingResponse(EmbeddingApiResponse response) {
        List<float[]> embeddings = response.data().stream()
                .sorted(Comparator.comparingInt(EmbeddingApiResponse.EmbeddingData::index))
                .map(EmbeddingApiResponse.EmbeddingData::embedding)
                .toList();

        TokenUsage tokenUsage = toTokenUsage(response.usage());
        return new EmbeddingResponse(embeddings, tokenUsage);
    }

    TokenUsage toTokenUsage(Usage usage) {
        if (usage == null) {
            return TokenUsage.EMPTY;
        }
        return new TokenUsage(
                usage.promptTokens(), 0, usage.totalTokens()
        );
    }
}
