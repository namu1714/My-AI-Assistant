package com.acme.assistant.agent;

import com.acme.assistant.llm.TokenUsage;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class AgentResponseTest {

    @Test
    void 성공_응답을_생성한다() {
        var response = AgentResponse.success(
                "분석 결과입니다.", 3, TokenUsage.EMPTY);

        assertThat(response.content())
                .isEqualTo("분석 결과입니다.");
        assertThat(response.finalState())
                .isEqualTo(AgentState.FINISHED);
        assertThat(response.iterationsUsed()).isEqualTo(3);
        assertThat(response.isSuccess()).isTrue();
    }

    @Test
    void 에러_응답을_생성한다() {
        var response = AgentResponse.error(
                "LLM 호출 실패",1, TokenUsage.EMPTY);

        assertThat(response.finalState())
                .isEqualTo(AgentState.ERROR);
        assertThat(response.isSuccess()).isFalse();
    }
}
