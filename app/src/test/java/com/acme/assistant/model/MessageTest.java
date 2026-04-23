package com.acme.assistant.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.map;

public class MessageTest {

    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
                .setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    @Test
    void 텍스트_메시지가_문자열로_직렬화된다() throws Exception {
        Message message = Message.ofUser("안녕하세요");

        String json = mapper.writeValueAsString(message);

        assertThat(json).contains("\"role\":\"user\"");
        assertThat(json).contains("\"content\":\"안녕하세요\"");
    }

    @Test
    void 멀티모달_메시지가_배열로_직렬화된다() throws Exception {
        Message message = Message.ofUser(List.of(
                new ContentPart.TextPart("이 이미지에 무엇이 보이나요?"),
                ContentPart.ImagePart.ofUrl("https://example.com/photo.jpg")
        ));

        String json = mapper.writeValueAsString(message);

        assertThat(json).contains("\"role\":\"user\"");
        assertThat(json).contains("\"type\":\"text\"");
        assertThat(json).contains("\"type\":\"image_url\"");
        assertThat(json).contains("\"url\":\"https://example.com/photo.jpg\"");
    }

    @Test
    void 응답_매시지를_역직렬화한다() throws Exception {
        String json = """
                {
                    "role": "assistant",
                    "content": "이미지에 고양이가 보입니다."
                }
                """;

        Message message = mapper.readValue(json, Message.class);

        assertThat(message.role()).isEqualTo("assistant");
        assertThat((String) message.content()).isEqualTo("이미지에 고양이가 보입니다.");
    }
}
