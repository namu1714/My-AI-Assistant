package com.acme.assistant.agent;

import com.acme.assistant.llm.LlmModel;
import com.acme.assistant.llm.client.MockLlmClient;
import com.acme.assistant.memory.TokenWindowMemory;
import com.acme.assistant.tool.ToolRegistry;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class DefaultAgentTest {

    @Test
    void 빌더로_에이전트를_생성한다() {
        Agent agent = DefaultAgent.builder()
                .name("test-agent")
                .description("테스트 에이전트")
                .llmClient(new MockLlmClient())
                .llmModel(new LlmModel("gpt-4o"))
                .build();

        assertThat(agent.metadata().name())
                .isEqualTo("test-agent");
        assertThat(agent.metadata().description())
                .isEqualTo("테스트 에이전트");
        assertThat(agent.metadata().version())
                .isEqualTo("1.0.0");
    }

    @Test
    void 기본값이_적용된다() {
        Agent agent = DefaultAgent.builder()
                .name("test-agent")
                .llmClient(new MockLlmClient())
                .llmModel(new LlmModel("gpt-4o"))
                .build();
        // toolRegistry: 빈 레지스트리
        assertThat(agent.content().toolRegistry())
                .isNotNull();
        // memory: MessageWindowMemory(100)
        assertThat(agent.content().memory())
                .isNotNull();
    }

    @Test
    void 모든_구성_요소를_지정할_수_있다() {
        var toolRegistry = new ToolRegistry();
        var memory = new TokenWindowMemory(4000);

        Agent agent = DefaultAgent.builder()
                .name("full-agent")
                .description("모든 구성 요소를 갖춘 에이전트")
                .version("2.0.0")
                .llmClient(new MockLlmClient())
                .llmModel(new LlmModel("gpt-4o"))
                .toolRegistry(toolRegistry)
                .memory(memory)
                .systemPrompt("시스템 프롬프트")
                .build();

        assertThat(agent.content().toolRegistry())
                .isSameAs(toolRegistry);
        assertThat(agent.content().memory())
                .isSameAs(memory);
        assertThat(agent.content().systemPrompt())
                .isEqualTo("시스템 프롬프트");
    }

    @Test
    void 이름이_없으면_예외가_발생한다() {
        assertThatThrownBy(() ->
                DefaultAgent.builder()
                        .llmClient(new MockLlmClient())
                        .llmModel(new LlmModel("gpt-4o"))
                        .build())
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("name");
    }

    @Test
    void llmClient가_없으면_예외가_발생한다() {
        assertThatThrownBy(() ->
                DefaultAgent.builder()
                        .name("test")
                        .llmModel(new LlmModel("gpt-4o"))
                        .build())
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("llmClient");
    }

    @Test
    void llmModel이_없으면_예외가_발생한다() {
        assertThatThrownBy(() ->
                DefaultAgent.builder()
                        .name("test")
                        .llmClient(new MockLlmClient())
                        .build())
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("llmModel");
    }
}
