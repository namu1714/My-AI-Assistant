package com.acme.assistant.conversation;

import com.acme.assistant.model.Message;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ConversationTest {

    @Test
    void 사용자_메시지를_추가한다() {
        Conversation conversation = new Conversation();
        conversation.addUserMessage("안녕하세요!");

        List<Message> messages = conversation.getMessages();
        assertThat(messages).hasSize(1);
        assertThat(messages.getFirst().role()).isEqualTo("user");
        assertThat(messages.getFirst().content()).isEqualTo("안녕하세요!");
    }

    @Test
    void 대화_히스토리가_순서대로_누적된다() {
        Conversation conversation = new Conversation();

        conversation.addUserMessage("안녕");
        conversation.addAssistantMessage("안녕하세요!");
        conversation.addUserMessage("오늘 날씨 어때?");

        List<Message> messages = conversation.getMessages();
        assertThat(messages).hasSize(3);
        assertThat(messages.get(0).role()).isEqualTo("user");
        assertThat(messages.get(1).role()).isEqualTo("assistant");
        assertThat(messages.get(2).role()).isEqualTo("user");
    }

    @Test
    void 시스템_프롬프트가_첫_번째_메시지로_추가된다() {
        Conversation conversation = new Conversation("당신은 친절한 AI 비서입니다.");

        conversation.addUserMessage("안녕하세요");

        List<Message> messages = conversation.getMessages();
        assertThat(messages).hasSize(2);
        assertThat(messages.get(0).role()).isEqualTo("system");
        assertThat(messages.get(0).content()).isEqualTo("당신은 친절한 AI 비서입니다.");
        assertThat(messages.get(1).role()).isEqualTo("user");
    }

    @Test
    void 시스템_프롬프트_없이_생성할_수_있다() {
        Conversation conversation = new Conversation();

        conversation.addUserMessage("안녕하세요");

        List<Message> messages = conversation.getMessages();
        assertThat(messages).hasSize(1);
        assertThat(messages.getFirst().role()).isEqualTo("user");
    }

    @Test
    void 메시지_목록은_수정할_수_없다() {
        Conversation conversation = new Conversation();
        conversation.addUserMessage("테스트");

        List<Message> messages = conversation.getMessages();

        assertThatThrownBy(() -> messages.add(Message.ofUser("추가")))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void 메시지_수_제한이_동작한다() {
        Conversation conversation = new Conversation(null, 4);

        conversation.addUserMessage("첫 번째 질문");
        conversation.addAssistantMessage("첫 번째 답변");
        conversation.addUserMessage("두 번째 질문");
        conversation.addAssistantMessage("두 번째 답변");
        conversation.addUserMessage("세 번째 질문"); // 5번째 메시지 -> 트리밍

        List<Message> messages = conversation.getMessages();
        assertThat(messages).hasSize(4);
        assertThat(messages.getFirst().content()).isEqualTo("첫 번째 답변");
        assertThat(messages.getLast().content()).isEqualTo("세 번째 질문");
    }

    @Test
    void 시스템_프롬프트는_트리밍에서_보존된다() {
        Conversation conversation = new Conversation("AI 비서입니다.", 4);

        conversation.addUserMessage("첫 번째 질문");
        conversation.addAssistantMessage("첫 번째 답변");
        conversation.addUserMessage("두 번째 질문");
        conversation.addAssistantMessage("두 번째 답변");
        conversation.addUserMessage("세 번째 질문");

        List<Message> messages = conversation.getMessages();
        assertThat(messages).hasSize(5);
        assertThat(messages.get(0).role()).isEqualTo("system");
        assertThat(messages.get(0).content()).isEqualTo("AI 비서입니다.");
        assertThat(messages.get(1).content()).isEqualTo("첫 번째 답변");
        assertThat(messages.getLast().content()).isEqualTo("세 번째 질문");
    }
}
