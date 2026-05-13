package com.acme.assistant.agent;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ExecutionContextTest {

    @Test
    void 대화_ID로_기본_컨텍스트를_생성한다() {
        var context = new ExecutionContext("conv-1");

        assertThat(context.conversationId()).isEqualTo("conv-1");
        assertThat(context.maxIterations()).isEqualTo(10);
        assertThat(context.metadata()).isEmpty();
    }

    @Test
    void 모든_필드를_지정할_수_있다() {
        var context = new ExecutionContext(
                "conv-1", "user-1", 5,
                Map.of("key", "value"));

        assertThat(context.userId()).isEqualTo("user-1");
        assertThat(context.maxIterations()).isEqualTo(5);
        assertThat(context.metadata()).containsEntry("key", "value");
    }

    @Test
    void maxIterations가_0이면_예외가_발생한다() {
        assertThatThrownBy(() ->
                new ExecutionContext("conv-1", null, 0,
                        Map.of()))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
