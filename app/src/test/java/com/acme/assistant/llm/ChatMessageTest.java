package com.acme.assistant.llm;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ChatMessageTest {

    @Test
    void ofSystem_createSystemMessage() {
        ChatMessage msg = ChatMessage.ofSystem("시스템 프롬프트");

        assertThat(msg.role()).isEqualTo(Role.SYSTEM);
        assertThat(msg.content()).isEqualTo("시스템 프롬프트");
        assertThat(msg.toolCalls()).isNull();
        assertThat(msg.toolCallId()).isNull();
    }

    @Test
    void ofUser_createUserMessage() {
        ChatMessage msg = ChatMessage.ofUser("안녕하세요");

        assertThat(msg.role()).isEqualTo(Role.USER);
        assertThat(msg.content()).isEqualTo("안녕하세요");
    }

    @Test
    void ofAssistant_createsAssistantMessage() {
        ChatMessage msg = ChatMessage.ofAssistant("답변입니다");

        assertThat(msg.role()).isEqualTo(Role.ASSISTANT);
        assertThat(msg.content()).isEqualTo("답변입니다");
        assertThat(msg.toolCalls()).isNull();
    }

    @Test
    void ofAssistant_withToolCalls_createsAssistantMessage() {
        var toolCalls = List.of(
                new LlmToolCall("call_1", "file_read", "{\"path\":\"a.txt\"}")
        );

        ChatMessage msg = ChatMessage.ofAssistant("", toolCalls);

        assertThat(msg.role()).isEqualTo(Role.ASSISTANT);
        assertThat(msg.toolCalls()).hasSize(1);
        assertThat(msg.toolCalls().getFirst().name()).isEqualTo("file_read");
    }

    @Test
    void ofTool_createsToolMessage() {
        ChatMessage msg = ChatMessage.ofTool("call_1", "파일 내용");

        assertThat(msg.role()).isEqualTo(Role.TOOL);
        assertThat(msg.content()).isEqualTo("파일 내용");
        assertThat(msg.toolCallId()).isEqualTo("call_1");
    }
}
