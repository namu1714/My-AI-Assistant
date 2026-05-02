package com.acme.assistant.llm;

import com.acme.assistant.exception.LlmException;
import com.acme.assistant.tool.ToolDefinition;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.*;

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

    @Test
    void chatAsync_success_returnsFuture() throws Exception {
        MockLlmClient client = new MockLlmClient();
        client.enqueue("비동기 응답");

        LlmModel model = new LlmModel("test-model");
        List<ChatMessage> messages = List.of(ChatMessage.ofUser("테스트"));

        CompletableFuture<LlmResponse> future = client.chatAsync(model, messages);
        LlmResponse response = future.get();

        assertThat(response.content()).isEqualTo("비동기 응답");
    }

    @Test
    void chatAsync_error_completesExceptionally() {
        MockLlmClient client = new MockLlmClient();

        // 큐가 비어 있어서 예외 발생
        LlmModel model = new LlmModel("test-model");
        List<ChatMessage> messages = List.of(
                ChatMessage.ofUser(" 테스트")
        );

        CompletableFuture<LlmResponse> future = client.chatAsync(model, messages);

        assertThatThrownBy(future::get)
                .isInstanceOf(ExecutionException.class)
                .hasCauseInstanceOf(LlmException.class);
    }
}
