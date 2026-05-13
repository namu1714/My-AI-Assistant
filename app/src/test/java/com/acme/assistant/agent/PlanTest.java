package com.acme.assistant.agent;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class PlanTest {

    @Test
    void 계획을_생성한다() {
        var plan = new Plan("파일 분석", List.of(
                new Step(1, "파일 목록 확인", "grep"),
                new Step(2, "파일 읽기", "file_read")
        ));
        assertThat(plan.goal()).isEqualTo("파일 분석");
        assertThat(plan.size()).isEqualTo(2);
        assertThat(plan.isEmpty()).isFalse();
    }

    @Test
    void 빈_계획을_확인한다() {
        var plan = new Plan("목표", List.of());

        assertThat(plan.isEmpty()).isTrue();
        assertThat(plan.size()).isEqualTo(0);
    }
    @Test
    void null_단계_목록은_빈_계획이다() {
        var plan = new Plan("목표", null);
        assertThat(plan.isEmpty()).isTrue();
    }
}
