package com.acme.assistant.agent;

import com.acme.assistant.tool.CurrentTimeTool;
import com.acme.assistant.tool.ToolRegistry;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SystemPromptBuilderTest {

    @Test
    void 프롬프트와_도구_모두_있으면_결합한다() {
        var registry = new ToolRegistry();
        registry.register(new CurrentTimeTool());

        String result = SystemPromptBuilder.build(
                "당신은 AI 비서입니다.", registry);

        assertThat(result)
                .startsWith("당신은 AI 비서입니다.")
                .contains("## 사용 가능한 도구")
                .contains("current_time");
    }

    @Test
    void 프롬프트만_있으면_프롬프트만_반환한다() {
        String result = SystemPromptBuilder.build(
                "당신은 AI 비서입니다.", null);

        assertThat(result)
                .isEqualTo("당신은 AI 비서입니다.");
    }

    @Test
    void 도구만_있으면_도구_목록을_반환한다() {
        var registry = new ToolRegistry();
        registry.register(new CurrentTimeTool());

        String result = SystemPromptBuilder.build(
                null, registry);

        assertThat(result)
                .startsWith("## 사용 가능한 도구")
                .contains("current_time");
    }

    @Test
    void 둘_다_없으면_빈_문자열을_반환한다() {
        String result = SystemPromptBuilder.build(

                null, null);
        assertThat(result).isEmpty();
    }

    @Test
    void 빈_레지스트리는_도구_없음으로_처리한다() {
        String result = SystemPromptBuilder.build(
                "프롬프트", new ToolRegistry());

        assertThat(result).isEqualTo("프롬프트");
    }
}
