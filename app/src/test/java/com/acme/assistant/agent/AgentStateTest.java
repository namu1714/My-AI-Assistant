package com.acme.assistant.agent;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class AgentStateTest {

    @Test
    void 각_상태의_설명을_확인한다() {
        assertThat(AgentState.IDLE.description())
                .isEqualTo("대기");
        assertThat(AgentState.THINKING.description())
                .isEqualTo("추론 중");
        assertThat(AgentState.ACTING.description())
                .isEqualTo("도구 실행 중");
        assertThat(AgentState.FINISHED.description())
                .isEqualTo("완료");
        assertThat(AgentState.ERROR.description())
                .isEqualTo("오류");
    }
}
