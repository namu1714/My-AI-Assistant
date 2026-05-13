package com.acme.assistant.agent;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class StepTest {

    @Test
    void 단계를_생성한다() {
        var step = new Step(1, "파일 읽기", "file_read");

        assertThat(step.order()).isEqualTo(1);
        assertThat(step.description()).isEqualTo("파일 읽기");
        assertThat(step.toolName()).isEqualTo("file_read");
    }

    @Test
    void 도구_없는_단계를_생성할_수_있다() {
        var step = new Step(1, "결과 종합",null);
        assertThat(step.toolName()).isNull();
    }

    @Test
    void 순서가_0이면_예외가_발생한다() {
        assertThatThrownBy(() ->
                new Step(0, "설명","tool"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 설명이_없으면_예외가_발생한다() {
        assertThatThrownBy(() ->
                new Step(1, "", "tool"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
