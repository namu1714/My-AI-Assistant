package com.acme.assistant.memory;

import com.acme.assistant.llm.ChatMessage;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class PersistentMemoryTest {

    @Test
    void 메시지_추가_시_자동으로_저장한다() {
        var repo = new InMemoryConversationRepository();
        var memory = new PersistentMemory(
                new MessageWindowMemory(10), repo, "test");

        memory.addMessage(ChatMessage.ofUser("안녕"));

        var saved = repo.load("test");
        assertThat(saved).isPresent();
        assertThat(saved.get()).hasSize(1);
    }

    @Test
    void 생성_시_기존_대화를_자동_복원한다() {
        var repo = new InMemoryConversationRepository();
        repo.save("test", List.of(
                ChatMessage.ofUser("이전 대화"),
                ChatMessage.ofAssistant("이전 응답")
        ));

        var memory = new PersistentMemory(
                new MessageWindowMemory(10), repo, "test");

        assertThat(memory.messageCount()).isEqualTo(2);
    }

    @Test
    void delegate_전략이_적용된다() {
        var repo = new InMemoryConversationRepository();
        var memory = new PersistentMemory(
                new MessageWindowMemory(2), repo, "test");

        memory.addMessage(ChatMessage.ofUser("1"));
        memory.addMessage(ChatMessage.ofAssistant("응답 1"));
        memory.addMessage(ChatMessage.ofUser("2"));

        // delegate 의 윈도우 전략이 적용됨
        assertThat(memory.messageCount()).isEqualTo(2);
    }

    @Test
    void clear하면_저장소에서도_삭제된다() {
        var repo = new InMemoryConversationRepository();
        var memory = new PersistentMemory(
                new MessageWindowMemory(10), repo, "test");

        memory.addMessage(ChatMessage.ofUser("안녕"));
        memory.clear();

        assertThat(memory.messageCount()).isEqualTo(0);
        assertThat(repo.load("test")).isEmpty();
    }
}
