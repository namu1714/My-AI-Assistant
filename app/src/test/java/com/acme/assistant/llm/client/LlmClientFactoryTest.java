package com.acme.assistant.llm.client;

import com.acme.assistant.llm.LlmProvider;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class LlmClientFactoryTest {

    @Test
    void create_openai_returnsOpenAiLlmClient() {
        LlmClient client = LlmClientFactory.create(
                LlmProvider.OPENAI, "test-key");
        assertThat(client).isInstanceOf(OpenAiLlmClient.class);
    }

    @Test
    void create_anthropic_returnsAnthropicLlmClient() {
        LlmClient client = LlmClientFactory.create(
                LlmProvider.ANTHROPIC, "test-key");
        assertThat(client).isInstanceOf(AnthropicLlmClient.class);
    }
    @Test
    void create_gemini_returnsGeminiLlmClient() {
        LlmClient client = LlmClientFactory.create(
                LlmProvider.GEMINI, "test-key");
        assertThat(client).isInstanceOf(GeminiLlmClient.class);
    }
    @Test
    void create_ollama_returnsOllamaLlmClient() {
        LlmClient client = LlmClientFactory.create(
                LlmProvider.OLLAMA, "test-key");
        assertThat(client).isInstanceOf(OllamaLlmClient.class);
    }

    @Test
    void defaultModel_returnsCorrectModels() {
        assertThat(LlmClientFactory.defaultModel(LlmProvider.OPENAI))
                .isEqualTo("gpt-4o-mini");
        assertThat(LlmClientFactory.defaultModel(LlmProvider.ANTHROPIC))
                .isEqualTo("claude-sonnet-4-20250514");
        assertThat(LlmClientFactory.defaultModel(LlmProvider.GEMINI))
                .isEqualTo("gemini-2.0-flash");
        assertThat(LlmClientFactory.defaultModel(LlmProvider.OLLAMA))
                .isEqualTo("llama3.2");
    }
}
