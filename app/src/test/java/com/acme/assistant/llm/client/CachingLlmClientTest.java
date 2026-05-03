package com.acme.assistant.llm.client;

import com.acme.assistant.llm.ChatMessage;
import com.acme.assistant.llm.LlmModel;
import com.acme.assistant.llm.LlmResponse;
import com.acme.assistant.tool.ToolDefinition;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class CachingLlmClientTest {

    @Test
    void chat_cachesResponse_onSecondCall() {
        MockLlmClient mockClient = new MockLlmClient();
        CachingLlmClient cachingClient = new CachingLlmClient(mockClient);

        LlmModel model = new LlmModel("test-model", 0.5, 100);

        mockClient.enqueue(" 첫 번째 응답");

        List<ChatMessage> messages = List.of(ChatMessage.ofUser("안녕하세요"));

        LlmResponse first = cachingClient.chat(model, messages);
        LlmResponse second = cachingClient.chat(model, messages);

        assertThat(first.content()).isEqualTo(" 첫 번째 응답");
        assertThat(second.content()).isEqualTo(" 첫 번째 응답");

        // MockLlmClient 는 한 번만 호출됨
        assertThat(mockClient.callCount()).isEqualTo(1);
    }

    @Test
    void chatWithTools_doesNotCache() {
        MockLlmClient mockClient = new MockLlmClient();
        CachingLlmClient cachingClient = new CachingLlmClient(mockClient);

        LlmModel model = new LlmModel("test-model", 0.5, 100);
        ChatMessage message1 = ChatMessage.ofUser("content 1");
        ChatMessage message2 = ChatMessage.ofUser("content 2");

        mockClient.enqueue("도구 응답 1");
        mockClient.enqueue("도구 응답 2");

        List<ToolDefinition> tools = List.of(
                new ToolDefinition("file_read", "Read a file", Map.of("type", "object"))
        );

        LlmResponse first = cachingClient.chat(model, List.of(message1), tools);
        LlmResponse second = cachingClient.chat(model, List.of(message2), tools);

        // 도구 호출 가능 요청은 캐시하지 않음
        assertThat(mockClient.callCount()).isEqualTo(2);
    }
}
