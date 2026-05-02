package com.acme.assistant.llm;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class LlmModelTest {

    @Test
    void constructor_nameOnly_setsDefaults() {
        LlmModel model = new LlmModel("gpt-4o-mini");

        assertThat(model.name()).isEqualTo("gpt-4o-mini");
        assertThat(model.temperature()).isNull();
        assertThat(model.maxTokens()).isNull();
    }

    @Test
    void withTemperature_returnsNewInstance() {
        LlmModel original = new LlmModel("gpt-4o");
        LlmModel modified = original.withTemperature(0.7);

        assertThat(modified.name()).isEqualTo("gpt-4o");
        assertThat(modified.temperature()).isEqualTo(0.7);
        assertThat(modified.maxTokens()).isNull();

        assertThat(original.temperature()).isNull();
    }

    @Test
    void withMaxTokens_returnsNewInstance() {
        LlmModel original = new LlmModel("gpt-4o");
        LlmModel modified = original.withMaxTokens(1024);

        assertThat(modified.maxTokens()).isEqualTo(1024);
        assertThat(original.maxTokens()).isNull();
    }

    @Test
    void chaining_worksCorrectly() {
        LlmModel model = new LlmModel("claude-3-5-sonnet")
                .withTemperature(0.5)
                .withMaxTokens(2048);

        assertThat(model.name()).isEqualTo("claude-3-5-sonnet");
        assertThat(model.temperature()).isEqualTo(0.5);
        assertThat(model.maxTokens()).isEqualTo(2048);
    }
}
