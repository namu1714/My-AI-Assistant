package com.acme.assistant.memory;

import com.acme.assistant.llm.ChatMessage;
import com.acme.assistant.llm.LlmModel;
import com.acme.assistant.llm.client.MockLlmClient;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SummaryMemoryTest {

    private final LlmModel model = new LlmModel("test-model");

    @Test
    void 임계값_미만이면_요약하지_않는다() {
        var mockClient = new MockLlmClient();
        var memory = new SummaryMemory(mockClient, model, 4);

        memory.addMessage(ChatMessage.ofUser(" 안녕"));
        memory.addMessage(ChatMessage.ofAssistant(" 안녕하세요!"));

        assertThat(memory.messageCount()).isEqualTo(2);
        assertThat(memory.getSummary()).isNull();
        assertThat(mockClient.callCount()).isEqualTo(0);
    }

    @Test
    void 임계값_초과_시_오래된_메시지를_요약한다() {
        var mockClient = new MockLlmClient();
        mockClient.enqueue(" 사용자가 인사를 했고 AI가 응답했습니다.");

        var memory = new SummaryMemory(mockClient, model, 4);
        memory.addMessage(ChatMessage.ofUser("안녕"));
        memory.addMessage(ChatMessage.ofAssistant("안녕하세요!"));
        memory.addMessage(ChatMessage.ofUser("날씨 어때?"));
        memory.addMessage(ChatMessage.ofAssistant("좋은 날씨입니다."));

        // 5번째 메시지에서 임계값 초과→ 요약 발생
        memory.addMessage(ChatMessage.ofUser(" 고마워"));

        assertThat(mockClient.callCount()).isEqualTo(1);
        assertThat(memory.getSummary()).isNotNull();
        assertThat(memory.messageCount()).isLessThan(5);
    }

    @Test
    void 요약이_getMessages에_포함된다() {
        var mockClient = new MockLlmClient();
        mockClient.enqueue("이전 대화에서 인사를 나눴습니다.");

        var memory = new SummaryMemory(mockClient, model, 2);
        memory.setSystemMessage(
                ChatMessage.ofSystem("당신은 AI 비서입니다."));

        memory.addMessage(ChatMessage.ofUser("안녕"));
        memory.addMessage(ChatMessage.ofAssistant("안녕하세요!"));
        memory.addMessage(ChatMessage.ofUser("오늘 할 일 뭐야?"));

        var messages = memory.getMessages();
        assertThat(messages.getFirst().content())
                .contains("이전 대화 요약:");
    }

    @Test
    void clear하면_요약과_메시지가_모두_삭제된다() {
        var mockClient = new MockLlmClient();
        mockClient.enqueue("요약");

        var memory = new SummaryMemory(mockClient, model, 2);
        memory.addMessage(ChatMessage.ofUser("안녕"));
        memory.addMessage(ChatMessage.ofAssistant("안녕하세요!"));
        memory.addMessage(ChatMessage.ofUser("다음"));

        memory.clear();

        assertThat(memory.messageCount()).isEqualTo(0);
        assertThat(memory.getSummary()).isNull();
    }
}
