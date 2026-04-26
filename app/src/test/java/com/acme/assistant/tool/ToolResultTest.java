package com.acme.assistant.tool;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ToolResultTest {

    @Test
    void 성공_결과를_생성한다() {
        ToolResult result = ToolResult.success("현재 시간: 2024-01-01");

        assertThat(result.content()).isEqualTo("현재 시간: 2024-01-01");
        assertThat(result.isError()).isFalse();
    }

    @Test
    void 에러_결과를_생성한다() {
        ToolResult result = ToolResult.error("파일을 찾을 수 없습니다.");

        assertThat(result.content()).isEqualTo("파일을 찾을 수 없습니다.");
        assertThat(result.isError()).isTrue();
    }

    @Test
    void ToolUseResult로_변환하다() {
        ToolResult toolResult = ToolResult.success("결과입니다.");

        ToolUseResult useResult = ToolUseResult.from("call_abc", toolResult);

        assertThat(useResult.toolCallId()).isEqualTo("call_abc");
        assertThat(useResult.content()).isEqualTo("결과입니다.");
        assertThat(useResult.isError()).isFalse();
    }
}
