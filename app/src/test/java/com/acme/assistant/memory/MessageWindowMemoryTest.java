package com.acme.assistant.memory;

import com.acme.assistant.llm.ChatMessage;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class MessageWindowMemoryTest {

    @Test
    void 메시지를_추가하고_조회한다() {
        var memory = new MessageWindowMemory(10);

        memory.addMessage(ChatMessage.ofUser(" 안녕하세요"));
        memory.addMessage(ChatMessage.ofAssistant(" 안녕하세요!"));

        assertThat(memory.messageCount()).isEqualTo(2);
        assertThat(memory.getMessages()).hasSize(2);
    }

    @Test
    void 최대_메시지_수를_초과하면_오래된_메시지를_제거한다() {
        var memory = new MessageWindowMemory(4);

        memory.addMessage(ChatMessage.ofUser("1 번"));
        memory.addMessage(ChatMessage.ofAssistant("응답 1"));
        memory.addMessage(ChatMessage.ofUser("2 번"));
        memory.addMessage(ChatMessage.ofAssistant("응답 2"));
        memory.addMessage(ChatMessage.ofUser("3 번"));

        assertThat(memory.messageCount()).isEqualTo(4);
        assertThat(memory.getMessages().getFirst().content())
                .isEqualTo("응답 1");
    }

    @Test
    void 시스템_메시지는_트리밍에서_보존된다() {
        var memory = new MessageWindowMemory(2);
        memory.setSystemMessage(
                ChatMessage.ofSystem("나는 AI 비서입니다."));

        memory.addMessage(ChatMessage.ofUser("1 번"));
        memory.addMessage(ChatMessage.ofAssistant("응답 1"));
        memory.addMessage(ChatMessage.ofUser("2 번"));

        // 시스템 메시지 + 대화 메시지 2 개
        assertThat(memory.getMessages()).hasSize(3);
        assertThat(memory.getMessages().getFirst().content())
                .isEqualTo("나는 AI 비서입니다.");
        assertThat(memory.messageCount()).isEqualTo(2);
    }

    @Test
    void clear하면_메시지가_모두_삭제된다() {
        var memory = new MessageWindowMemory(10);
        memory.addMessage(ChatMessage.ofUser(" 안녕"));
        memory.clear();

        assertThat(memory.messageCount()).isEqualTo(0);
        assertThat(memory.getMessages()).isEmpty();
    }
    @Test
    void 최대_메시지가_0이면_제한_없이_저장한다() {
        var memory = new MessageWindowMemory(0);
        for (int i = 0; i < 100; i++) {
            memory.addMessage(ChatMessage.ofUser(" 메시지" + i));
        }
        
        assertThat(memory.messageCount()).isEqualTo(100);
    }
}
