package com.acme.assistant.tool;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ToolInputTest {

    @Test
    void JSON_문자열을_파싱한다() {
        ToolInput input = ToolInput.parse("{\"path\":\"/tmp/test.txt\"}");

        assertThat(input.requireString("path")).isEqualTo("/tmp/test.txt");
    }

    @Test
    void 필수_파라미터가_없으면_예외가_발생한다() {
        ToolInput input = ToolInput.parse("{}");

        assertThatThrownBy(() -> input.requireString("path"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("필수 파라미터 누락: path");
    }

    @Test
    void 선택적_파라미터를_가져온다() {
        ToolInput input = ToolInput.parse("{\"encoding\":\"UTF-8\"}");

        assertThat(input.optionalString("encoding")).contains("UTF-8");
        assertThat(input.optionalString("missing")).isEmpty();
    }
}
