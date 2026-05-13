package com.acme.assistant.agent;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class AgentRequestTest {

    @Test
    void 메시지로_요청을_생성한다() {
        var request = new AgentRequest("안녕하세요");
        assertThat(request.message()).isEqualTo("안녕하세요");
    }

    @Test
    void 빈_메시지는_예외가_발생한다() {
        assertThatThrownBy(() -> new AgentRequest(""))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void null_메시지는_예외가_발생한다() {
        assertThatThrownBy(() -> new AgentRequest(null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
