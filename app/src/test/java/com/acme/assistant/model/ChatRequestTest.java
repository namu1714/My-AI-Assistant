package com.acme.assistant.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ChatRequestTest {

    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
                .setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    @Test
    void 필수_필드만으로_직렬화한다() throws Exception {
        ChatRequest request = new ChatRequest(
                "gpt-4o-mini",
                List.of(Message.ofUser("안녕하세요"))
        );

        String json = mapper.writeValueAsString(request);

        assertThat(json).contains("\"model\":\"gpt-4o-mini\"");
        assertThat(json).contains("\"role\":\"user\"");
        assertThat(json).contains("\"content\":\"안녕하세요\"");
        assertThat(json).doesNotContain("temperature");
        assertThat(json).doesNotContain("max_tokens");
    }

    @Test
    void 선택_필드를_포함하여_직렬화한다() throws Exception {
        ChatRequest request = new ChatRequest(
                "gpt-4o-mini",
                List.of(Message.ofUser("안녕하세요")),
                0.7,
                100
        );

        String json = mapper.writeValueAsString(request);

        assertThat(json).contains("\"temperature\":0.7");
        assertThat(json).contains("\"max_tokens\":100");
    }
}
