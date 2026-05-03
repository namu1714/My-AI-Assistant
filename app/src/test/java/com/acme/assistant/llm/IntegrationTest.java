package com.acme.assistant.llm;

import com.acme.assistant.exception.LlmException;
import com.acme.assistant.llm.client.MockLlmClient;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class IntegrationTest {

    @Test
    void agentLoop_toolCallThenTextResponse() {
        MockLlmClient client = new MockLlmClient();

        // 1차: 도구 호출 응답
        var toolCalls = List.of(
                new LlmToolCall("call_1", "current_time", "{}")
        );
        client.enqueue(
                new LlmResponse("", toolCalls, new TokenUsage(100, 20)));

        // 2차: 최종 텍스트 응답
        client.enqueue(
                new LlmResponse("현재 시간은 오후 3시입니다.", new TokenUsage(150, 30)));

        LlmModel model = new LlmModel("test");
        var messages = List.of(ChatMessage.ofUser("지금 몇 시야?"));

        // 1차 호출
        LlmResponse first = client.chat(model, messages);
        assertThat(first.hasToolCalls()).isTrue();

        // 2차 호출
        LlmResponse second = client.chat(model, messages);

        assertThat(second.hasToolCalls()).isFalse();
        assertThat(second.content()).contains("오후 3시");
        assertThat(client.callCount()).isEqualTo(2);
    }

    @Test
    void tokenTracker_accumulatesAcrossMultipleCalls() {
        MockLlmClient client = new MockLlmClient();
        TokenTracker tracker = new TokenTracker();

        client.enqueue(new LlmResponse("첫 번째", new TokenUsage(100, 50)));
        client.enqueue(new LlmResponse("두 번째", new TokenUsage(200, 80)));

        LlmModel model = new LlmModel("test");

        LlmResponse r1 = client.chat(model, List.of(ChatMessage.ofUser(" 질문 1")));
        tracker.add(r1.tokenUsage());

        LlmResponse r2 = client.chat(model, List.of(ChatMessage.ofUser(" 질문 2")));
        tracker.add(r2.tokenUsage());

        assertThat(tracker.callCount()).isEqualTo(2);
        assertThat(tracker.cumulative().inputTokens()).isEqualTo(300);
        assertThat(tracker.cumulative().outputTokens()).isEqualTo(130);
    }

    @Test
    void enqueue_and_chat_dequeuesInOrder() {
        MockLlmClient client = new MockLlmClient();

        client.enqueue("첫 번째 응답");
        client.enqueue("두 번째 응답");

        LlmModel model = new LlmModel("test");
        var messages = List.of(ChatMessage.ofUser("질문 1"));

        assertThat(client.chat(model, messages).content())
                .isEqualTo("첫 번째 응답");
        assertThat(client.chat(model, messages).content())
                .isEqualTo("두 번째 응답");
    }

    @Test
    void chat_emptyQueue_throwsLlmException() {
        MockLlmClient client = new MockLlmClient();
        LlmModel model = new LlmModel("test");
        var messages = List.of(ChatMessage.ofUser(" 질문"));

        assertThatThrownBy(() -> client.chat(model, messages))
                .isInstanceOf(LlmException.class)
                .hasMessageContaining("응답 큐가 비어 있습니다");
    }

    @Test
    void receivedMessages_recordsAllCalls() {
        MockLlmClient client = new MockLlmClient();
        client.enqueue("응답 1");
        client.enqueue("응답 2");

        LlmModel model = new LlmModel("test");
        client.chat(model, List.of(ChatMessage.ofUser("첫 질문")));
        client.chat(model, List.of(
                ChatMessage.ofUser("첫 질문"),
                ChatMessage.ofAssistant("응답 1"),
                ChatMessage.ofUser("두 번째 질문")
        ));

        assertThat(client.receivedMessages()).hasSize(2);
        assertThat(client.receivedMessages().get(0)).hasSize(1);
        assertThat(client.receivedMessages().get(1)).hasSize(3);
    }

    @Test
    void callCount_tracksNumberOfCalls() {
        MockLlmClient client = new MockLlmClient();
        client.enqueue("응답 1");
        client.enqueue("응답 2");

        LlmModel model = new LlmModel("test");
        client.chat(model, List.of(ChatMessage.ofUser("질문 1")));
        client.chat(model, List.of(ChatMessage.ofUser("질문 2")));

        assertThat(client.callCount()).isEqualTo(2);
    }
}
