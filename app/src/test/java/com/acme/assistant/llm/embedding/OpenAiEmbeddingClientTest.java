package com.acme.assistant.llm.embedding;

import com.acme.assistant.client.OpenAiClient;
import com.acme.assistant.model.Usage;
import com.acme.assistant.model.embedding.EmbeddingApiResponse;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class OpenAiEmbeddingClientTest {

    @Test
    void toEmbeddingResponse_변환_검증() {
        var client = new OpenAiEmbeddingClient(
                new OpenAiClient("test-key"));

        var apiResponse = new EmbeddingApiResponse(
                List.of(
                        new EmbeddingApiResponse.EmbeddingData(
                                0, new float[]{0.1f, 0.2f, 0.3f}),
                        new EmbeddingApiResponse.EmbeddingData(
                                1, new float[]{0.4f, 0.5f, 0.6f})
                ),
                new Usage(10, 0, 10)
        );
        EmbeddingResponse response = client.toEmbeddingResponse(apiResponse);

        assertThat(response.embeddings()).hasSize(2);
        assertThat(response.embeddings().get(0))
                .containsExactly(0.1f, 0.2f, 0.3f);
        assertThat(response.embeddings().get(1))
                .containsExactly(0.4f, 0.5f, 0.6f);
        assertThat(response.tokenUsage().inputTokens()).isEqualTo(10);
    }

    @Test
    void toRequest_dimensions_설정() {
        var client = new OpenAiEmbeddingClient(
                new OpenAiClient("test-key"));

        var request = client.toRequest(
                new EmbeddingModel("text-embedding-3-small", 512),
                List.of("hello"));

        assertThat(request.model()).isEqualTo("text-embedding-3-small");
        assertThat(request.input()).containsExactly("hello");
        assertThat(request.dimensions()).isEqualTo(512);
    }

    @Test
    void toRequest_기본_dimensions() {
        var client = new OpenAiEmbeddingClient(
                new OpenAiClient("test-key"));

        var request = client.toRequest(
                new EmbeddingModel("text-embedding-3-small"),
                List.of("hello"));

        assertThat(request.dimensions()).isNull();
    }
}
