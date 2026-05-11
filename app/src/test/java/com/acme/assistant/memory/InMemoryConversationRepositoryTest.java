package com.acme.assistant.memory;

import com.acme.assistant.llm.ChatMessage;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class InMemoryConversationRepositoryTest {

    @Test
    void 대화를_저장하고_로드한다() {
        var repo = new InMemoryConversationRepository();
        var messages = List.of(
                ChatMessage.ofUser("안녕"),
                ChatMessage.ofAssistant("안녕하세요!")
        );

        repo.save("conv-1", messages);

        var loaded = repo.load("conv-1");
        assertThat(loaded).isPresent();
        assertThat(loaded.get()).hasSize(2);
    }

    @Test
    void 존재하지_않는_대화는_빈_Optional을_반환한다() {
        var repo = new InMemoryConversationRepository();
        assertThat(repo.load("없는대화")).isEmpty();
    }

    @Test
    void 대화_목록을_조회한다() {
        var repo = new InMemoryConversationRepository();
        repo.save("conv-1", List.of(ChatMessage.ofUser("1")));
        repo.save("conv-2", List.of(ChatMessage.ofUser("2")));

        assertThat(repo.list()).hasSize(2);
        assertThat(repo.list()).contains("conv-1", "conv-2");
    }

    @Test
    void 대화를_삭제한다() {
        var repo = new InMemoryConversationRepository();
        repo.save("conv-1", List.of(ChatMessage.ofUser("안녕")));

        repo.delete("conv-1");

        assertThat(repo.load("conv-1")).isEmpty();
    }
}
