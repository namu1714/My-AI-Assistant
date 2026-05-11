package com.acme.assistant.memory;

import com.acme.assistant.llm.ChatMessage;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class TokenWindowMemoryTest {

    @Test
    void 예산_내에서_메시지를_유지한다() {
        var memory = new TokenWindowMemory(100);

        memory.addMessage(ChatMessage.ofUser("Hello"));
        memory.addMessage(ChatMessage.ofAssistant("World"));

        assertThat(memory.messageCount()).isEqualTo(2);
        assertThat(memory.estimatedTokens())
                .isLessThanOrEqualTo(100);
    }

    @Test
    void 토큰_예산_초과_시_오래된_메시지를_제거한다() {
        // 각 메시지 약 5 토큰 (content 1 + overhead 4)
        // 예산 15 면 최대 3 개
        var memory = new TokenWindowMemory(15);

        memory.addMessage(ChatMessage.ofUser("A"));
        memory.addMessage(ChatMessage.ofAssistant("B"));
        memory.addMessage(ChatMessage.ofUser("C"));
        memory.addMessage(ChatMessage.ofAssistant("D"));

        assertThat(memory.estimatedTokens())
                .isLessThanOrEqualTo(15);
        assertThat(memory.messageCount()).isLessThanOrEqualTo(3);
    }

    @Test
    void 시스템_메시지_토큰이_예산에서_차감된다() {
        var memory = new TokenWindowMemory(30);

        memory.setSystemMessage(ChatMessage.ofSystem(
                "This is a long system prompt with many tokens"));

        memory.addMessage(ChatMessage.ofUser("Hello"));
        memory.addMessage(ChatMessage.ofAssistant("World"));
        memory.addMessage(ChatMessage.ofUser("Test"));

        assertThat(memory.estimatedTokens())
                .isLessThanOrEqualTo(30);
    }

    @Test
    void maxTokens가_0이하이면_예외를_던진다() {
        assertThatThrownBy(() -> new TokenWindowMemory(0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void estimatedTokens는_현재_토큰_수를_반환한다() {
        var memory = new TokenWindowMemory(1000);

        assertThat(memory.estimatedTokens()).isEqualTo(0);

        memory.addMessage(ChatMessage.ofUser("Hello"));
        assertThat(memory.estimatedTokens()).isGreaterThan(0);
    }
}
