package com.acme.assistant.agent;

import com.acme.assistant.llm.LlmModel;
import com.acme.assistant.llm.client.MockLlmClient;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class AgentRegistryTest {

    private Agent createAgent(String name) {
        return DefaultAgent.builder()
                .name(name)
                .llmClient(new MockLlmClient())
                .llmModel(new LlmModel("gpt-4o"))
                .build();
    }

    @Test
    void 에이전트를_등록하고_조회한다() {
        var registry = new AgentRegistry();
        registry.register(createAgent("assistant"));

        assertThat(registry.getAgent("assistant"))
                .isPresent();
        assertThat(registry.size()).isEqualTo(1);
    }

    @Test
    void 존재하지_않는_에이전트는_empty를_반환한다() {
        var registry = new AgentRegistry();

        assertThat(registry.getAgent("unknown"))
                .isEmpty();
    }

    @Test
    void 중복_등록은_예외가_발생한다() {
        var registry = new AgentRegistry();
        registry.register(createAgent("assistant"));

        assertThatThrownBy(() ->
                registry.register(createAgent("assistant")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already registered");
    }

    @Test
    void 여러_에이전트를_등록할_수_있다() {
        var registry = new AgentRegistry();
        registry.register(createAgent("agent-1"));
        registry.register(createAgent("agent-2"));
        registry.register(createAgent("agent-3"));

        assertThat(registry.listAgents()).hasSize(3);
    }
}
