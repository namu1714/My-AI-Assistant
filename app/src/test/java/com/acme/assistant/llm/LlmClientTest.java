package com.acme.assistant.llm;

import com.acme.assistant.tool.ToolDefinition;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class LlmClientTest {

    @Test
    void interface_canBeImplemented() {
        LlmClient client = new LlmClient() {
            @Override
            public LlmResponse chat(LlmModel model, List<ChatMessage> messages) {
                return new LlmResponse("테스트", TokenUsage.EMPTY);
            }

            @Override
            public LlmResponse chat(LlmModel model, List<ChatMessage> messages, List<ToolDefinition> tools) {
                return new LlmResponse("테스트", TokenUsage.EMPTY);
            }
        };

        LlmResponse response = client.chat(
                new LlmModel("test"), List.of(ChatMessage.ofUser("안녕"))
        );

        assertThat(response.content()).isEqualTo("테스트");
    }
}
