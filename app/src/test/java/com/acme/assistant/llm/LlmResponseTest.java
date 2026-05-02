package com.acme.assistant.llm;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class LlmResponseTest {

    @Test
    void textResponse_hasNoToolCalls() {
        LlmResponse response = new LlmResponse("안녕하세요", TokenUsage.EMPTY);

        assertThat(response.content()).isEqualTo("안녕하세요");
        assertThat(response.hasToolCalls()).isFalse();
        assertThat(response.toolCalls()).isEmpty();
    }

    @Test
    void toolCallResponse_hasToolCalls() {
        var toolCalls = List.of(
                new LlmToolCall("call_1", "file_read", "{\"path\":\"a.txt\"}")
        );
        LlmResponse response = new LlmResponse(
                "", toolCalls, TokenUsage.EMPTY);

        assertThat(response.hasToolCalls()).isTrue();
        assertThat(response.toolCalls()).hasSize(1);
    }

    @Test
    void toAssistantMessage_textResponse_createsSimpleMessage() {
        LlmResponse response = new LlmResponse("답변입니다", TokenUsage.EMPTY);

        ChatMessage message = response.toAssistantMessage();

        assertThat(message.role()).isEqualTo(Role.ASSISTANT);
        assertThat(message.content()).isEqualTo("답변입니다");
        assertThat(message.toolCalls()).isNull();
    }

    @Test
    void toAssistantMessage_toolCallResponse_includesToolCalls() {
        var toolCalls = List.of(
                new LlmToolCall("call_1", "current_time", "{}")
        );
        LlmResponse response = new LlmResponse("", toolCalls, TokenUsage.EMPTY);

        ChatMessage message = response.toAssistantMessage();

        assertThat(message.role()).isEqualTo(Role.ASSISTANT);
        assertThat(message.toolCalls()).hasSize(1);
        assertThat(message.toolCalls().getFirst().name())
                .isEqualTo("current_time");
    }

    @Test
    void tokenUsage_isPreserved() {
        TokenUsage usage = new TokenUsage(100, 50);
        LlmResponse response = new LlmResponse("ok", usage);

        assertThat(response.tokenUsage().inputTokens()).isEqualTo(100);
        assertThat(response.tokenUsage().outputTokens()).isEqualTo(50);
        assertThat(response.tokenUsage().totalTokens()).isEqualTo(150);
    }
}
