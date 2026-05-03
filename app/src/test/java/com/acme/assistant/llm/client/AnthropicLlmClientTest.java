package com.acme.assistant.llm.client;

import com.acme.assistant.llm.*;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class AnthropicLlmClientTest {

    private static final AnthropicLlmClient client = new AnthropicLlmClient("test-api-key");
    private static final ObjectMapper mapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
            .setSerializationInclusion(JsonInclude.Include.NON_NULL);

    @Test
    void parseResponse_textOnly_extractsContent() throws Exception {
        String json =
                """
                {
                    "id": "msg_01",
                    "type": "message",
                    "role": "assistant",
                    "content": [
                        {"type": "text", "text": "안녕하세요!"}
                    ],
                    "usage": {"input_tokens": 100, "output_tokens": 30}
                }
                """;

        JsonNode responseJson = mapper.readTree(json);
        LlmResponse response = client.parseResponse(responseJson);

        assertThat(response.content()).isEqualTo("안녕하세요!");
        assertThat(response.hasToolCalls()).isFalse();
        assertThat(response.tokenUsage().inputTokens()).isEqualTo(100);
        assertThat(response.tokenUsage().outputTokens()).isEqualTo(30);
    }

    @Test
    void parseResponse_withToolUse_extractsToolCalls() throws Exception {
        String json =
                """
                {
                    "id": "msg_02",
                    "type": "message",
                    "role": "assistant",
                    "content": [
                        {"type": "text", "text": "파일을 읽겠습니다."},
                        {"type": "tool_use", "id": "toolu_01",
                        "name": "file_read",
                        "input": {"path": "README.md"}}
                    ],
                    "usage": {"input_tokens": 200, "output_tokens": 50}
                }
                """;

        JsonNode responseJson = mapper.readTree(json);
        LlmResponse response = client.parseResponse(responseJson);

        assertThat(response.content()).isEqualTo("파일을 읽겠습니다.");
        assertThat(response.hasToolCalls()).isTrue();
        assertThat(response.toolCalls()).hasSize(1);

        LlmToolCall toolCall = response.toolCalls().getFirst();
        assertThat(toolCall.id()).isEqualTo("toolu_01");
        assertThat(toolCall.name()).isEqualTo("file_read");
    }

    @Test
    void toAnthropicMessages_separatesSystemMessage() {
        List<ChatMessage> messages = List.of(
                ChatMessage.ofSystem("시스템 프롬프트"),
                ChatMessage.ofUser("안녕하세요")
        );

        String system = client.extractSystemPrompt(messages);
        assertThat(system).isEqualTo("시스템 프롬프트");

        var result = client.toAnthropicMessages(messages);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).get("role").asText()).isEqualTo("user");
    }

    @Test
    void toTokenUsage_nullUsage_returnsEmpty() {
        TokenUsage usage = client.toTokenUsage(null);
        assertThat(usage).isEqualTo(TokenUsage.EMPTY);
    }
}
