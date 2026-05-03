package com.acme.assistant.llm.client;

import com.acme.assistant.exception.LlmException;
import com.acme.assistant.llm.ChatMessage;
import com.acme.assistant.llm.LlmModel;
import com.acme.assistant.llm.LlmResponse;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class FallbackLlmClientTest {

    @Test
    void chat_primaryFails_returnsFallbackResponse() {
        MockLlmClient primary = new MockLlmClient();
        MockLlmClient fallback = new MockLlmClient();

        LlmModel model = new LlmModel("test-model");

        // primary 에 응답을 넣지 않으면 LlmException 발생
        fallback.enqueue("대체 응답");

        FallbackLlmClient client = new FallbackLlmClient(primary, fallback);
        LlmResponse response = client.chat(model, List.of(ChatMessage.ofUser("질문")));

        assertThat(response.content()).isEqualTo("대체 응답");
    }

    @Test
    void chat_allFail_throwsException() {
        MockLlmClient primary = new MockLlmClient();
        MockLlmClient fallback = new MockLlmClient();

        LlmModel model = new LlmModel("test-model");

        FallbackLlmClient client = new FallbackLlmClient(primary, fallback);

        assertThatThrownBy(() -> client.chat(model, List.of(ChatMessage.ofUser("질문"))))
                .isInstanceOf(LlmException.class)
                .hasMessageContaining("모든 LLM 제공자가 실패했습니다");
    }
}
