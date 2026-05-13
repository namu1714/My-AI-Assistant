package com.acme.assistant.agent;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class AgentMetadataTest {

    @Test
    void 이름과_설명으로_생성한다() {
        var metadata = new AgentMetadata(
                "assistant", "AI 비서");

        assertThat(metadata.name()).isEqualTo("assistant");
        assertThat(metadata.description()).isEqualTo("AI 비서");
        assertThat(metadata.version()).isEqualTo("1.0.0");
    }

    @Test
    void 버전을_지정할_수_있다() {
        var metadata = new AgentMetadata(
                "assistant", "AI 비서", "2.0.0");

        assertThat(metadata.version()).isEqualTo("2.0.0");
    }
    @Test
    void 이름이_없으면_예외가_발생한다() {
        assertThatThrownBy(() ->
                new AgentMetadata(null, "설명"))
                .isInstanceOf(IllegalArgumentException.class);
    }
    @Test
    void 이름이_공백이면_예외가_발생한다() {
        assertThatThrownBy(() ->
                new AgentMetadata(" ", "설명"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
