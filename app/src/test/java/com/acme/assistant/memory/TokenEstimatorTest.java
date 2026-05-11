package com.acme.assistant.memory;

import com.acme.assistant.llm.ChatMessage;
import com.acme.assistant.llm.Role;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TokenEstimatorTest {

    @Test
    void 빈_문자열은_0을_반환한다() {
        assertThat(TokenEstimator.estimate("")).isEqualTo(0);
    }

    @Test
    void null은_0을_반환한다() {
        assertThat(TokenEstimator.estimate((String) null))
                .isEqualTo(0);
    }
    @Test
    void 영문_텍스트의_토큰을_추정한다() {
        assertThat(TokenEstimator.estimate("Hello World"))
                .isEqualTo(2);
    }
    @Test
    void 한글_텍스트의_토큰을_추정한다() {
        String korean = "안녕하세요";
        System.out.println("텍스트 길이: " + korean.length());
        assertThat(TokenEstimator.estimate(korean))
                .isEqualTo(korean.length() / 4);
    }

    @Test
    void 메시지의_토큰을_추정한다() {
        // content: "Hello" = 5/4 = 1, overhead = 4, total = 5
        ChatMessage message = ChatMessage.ofUser("Hello");

        assertThat(TokenEstimator.estimate(message)).isEqualTo(5);
    }
    @Test
    void content가_null인_메시지는_오버헤드만_반환한다() {
        ChatMessage message = new ChatMessage(Role.ASSISTANT, null);

        assertThat(TokenEstimator.estimate(message)).isEqualTo(4);
    }

    @Test
    void 메시지_목록의_토큰을_합산한다() {
        List<ChatMessage> messages = List.of(
                ChatMessage.ofUser("Hello"), // 1 + 4 = 5
                ChatMessage.ofAssistant("World") // 1 + 4 = 5
        );
        assertThat(TokenEstimator.estimate(messages)).isEqualTo(10);
    }
}
