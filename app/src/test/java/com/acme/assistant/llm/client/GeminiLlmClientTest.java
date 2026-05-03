package com.acme.assistant.llm.client;

import com.acme.assistant.llm.LlmResponse;
import com.acme.assistant.llm.Role;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class GeminiLlmClientTest {

    private static final GeminiLlmClient client = new GeminiLlmClient("test-api-key");
    private static final ObjectMapper mapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
            .setSerializationInclusion(JsonInclude.Include.NON_NULL);

    @Test
    void parseResponse_textOnly_extractsContent() throws Exception {
        String json = """
                {
                    "candidates": [{
                        "content": {
                            "parts": [{"text": " 안녕하세요!"}],
                            "role": "model"
                        }
                    }],
                    "usageMetadata": {
                        "promptTokenCount": 50,
                        "candidatesTokenCount": 20,
                        "totalTokenCount": 70
                    }
                }
                """;

        JsonNode responseJson = mapper.readTree(json);
        LlmResponse response = client.parseResponse(responseJson);

        assertThat(response.content()).isEqualTo(" 안녕하세요!");
        assertThat(response.hasToolCalls()).isFalse();
        assertThat(response.tokenUsage().inputTokens()).isEqualTo(50);
        assertThat(response.tokenUsage().outputTokens()).isEqualTo(20);
    }

    @Test
    void toGeminiRole_convertsCorrectly() {
        assertThat(client.toGeminiRole(Role.USER)).isEqualTo("user");
        assertThat(client.toGeminiRole(Role.ASSISTANT)).isEqualTo("model");
        assertThat(client.toGeminiRole(Role.TOOL)).isEqualTo("user");
    }
}