package com.acme.assistant.agent;

import com.acme.assistant.llm.LlmModel;
import com.acme.assistant.llm.client.MockLlmClient;
import com.acme.assistant.tool.ToolRegistry;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class AgentContentTest {

    @Test
    void 필수_필드만으로_생성한다() {
        var content = new AgentContent(
                new MockLlmClient(),
                new LlmModel("gpt-4o"),
                null, null, null);

        assertThat(content.llmClient()).isNotNull();
        assertThat(content.llmModel()).isNotNull();
        assertThat(content.toolRegistry()).isNull();
    }

    @Test
    void 모든_필드를_지정할_수_있다() {
        var content = new AgentContent(
                new MockLlmClient(),
                new LlmModel("gpt-4o"),
                new ToolRegistry(),
                null,
                "당신은 AI 비서입니다.");

        assertThat(content.systemPrompt())
                .isEqualTo("당신은 AI 비서입니다.");
    }

    @Test
    void llmClient가_null이면_예외가_발생한다() {
        assertThatThrownBy(() ->
                new AgentContent(
                        null, new LlmModel("gpt-4o"),
                        null, null, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void llmModel이_null이면_예외가_발생한다() {
        assertThatThrownBy(() ->
                new AgentContent(
                        new MockLlmClient(), null,
                        null, null, null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
