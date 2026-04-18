package com.acme.assistant.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ChatResponseTest {

    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
    }

    @Test
    void API_응답_JSON을_역직렬화한다() throws Exception {
        String json = """
                {
                    "id": "chatcmpl-abc123",
                    "object": "chat.completion",
                    "model": "gpt-4o-mini",
                    "choices": [
                        {
                            "index": 0,
                            "message": {
                                "role": "assistant",
                                "content": " 안녕하세요! 무엇을 도와드릴까요?"
                            },
                            "finish_reason": "stop"
                        }
                    ],
                    "usage": {
                        "prompt_tokens": 10,
                        "completion_tokens": 15,
                        "total_tokens": 25
                    }
                }
                """;

        ChatResponse response = mapper.readValue(json, ChatResponse.class);

        assertThat(response.id()).isEqualTo("chatcmpl-abc123");
        assertThat(response.model()).isEqualTo("gpt-4o-mini");
        assertThat(response.content()).isEqualTo(" 안녕하세요! 무엇을 도와드릴까요?");
        assertThat(response.choices()).hasSize(1);
        assertThat(response.choices().getFirst().finishReason()).isEqualTo("stop");
        assertThat(response.usage().promptTokens()).isEqualTo(10);
        assertThat(response.usage().completionTokens()).isEqualTo(15);
        assertThat(response.usage().totalTokens()).isEqualTo(25);
    }

    @Test
    void 알_수_없는_필드가_있어도_역직렬화에_성공한다() throws Exception {
        String json = """
                {
                    "id": "chatcmpl-abc123",
                    "object": "chat.completion",
                    "model": "gpt-4o-mini",
                    "system_fingerprint": "fp_abc123",
                    "choices": [
                        {
                            "index": 0,
                            "message": {"role": "assistant", "content": " 응답"},
                            "finish_reason": "stop",
                            "logprobs": null
                        }
                    ],
                    "usage": {
                        "prompt_tokens": 5,
                        "completion_tokens": 3,
                        "total_tokens": 8
                    }
                }
                """;

        ChatResponse response = mapper.readValue(json, ChatResponse.class);

        assertThat(response.content()).isEqualTo(" 응답");
    }
}
