package com.acme.assistant;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class JacksonTest {
    private final ObjectMapper mapper = new ObjectMapper();

    record Message(String role, String content) {}

    @Test
    void 직렬화_Java객체를_JSON으로_변환한다() throws Exception {
        Message message = new Message("user", "안녕하세요");

        String json = mapper.writeValueAsString(message);

        assertThat(json).contains("\"role\":\"user\"");
        assertThat(json).contains("\"content\":\"안녕하세요\"");
    }

    @Test
    void 역직렬화_JSON을_Java객체로_변환한다() throws Exception {
        String json = """
            {"role":"assistant","content":"반갑습니다"}
            """;

        Message message = mapper.readValue(json, Message.class);

        assertThat(message.role()).isEqualTo("assistant");
        assertThat(message.content()).isEqualTo("반갑습니다");
    }
}
