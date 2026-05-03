package com.acme.assistant.llm.client;

import com.acme.assistant.client.OpenAiClient;
import com.acme.assistant.exception.LlmException;
import com.acme.assistant.llm.LlmProvider;

public class LlmClientFactory {

    private LlmClientFactory() {
        // private constructor to prevent instantiation
    }

    public static LlmClient create(LlmProvider provider, String apiKey) {
        return switch (provider) {
            case OPENAI -> new OpenAiLlmClient(new OpenAiClient(apiKey));
            case ANTHROPIC -> new AnthropicLlmClient(apiKey);
            case GEMINI -> new GeminiLlmClient(apiKey);
            case OLLAMA -> new OllamaLlmClient();
        };
    }

    public static LlmClient fromEnvironment() {
        String providerName = System.getenv("LLM_PROVIDER");
        if (providerName == null || providerName.isBlank()) {
            providerName = "openai";
        }

        LlmProvider provider = LlmProvider.from(providerName);

        String apiKey = switch (provider) {
            case OPENAI -> System.getenv("OPENAI_API_KEY");
            case ANTHROPIC -> System.getenv("ANTHROPIC_API_KEY");
            case GEMINI -> System.getenv("GEMINI_API_KEY");
            case OLLAMA -> "ollama";
        };

        if (apiKey == null || apiKey.isBlank()) {
            throw new LlmException(provider.value(), provider.name() + " API 키가 환경 변수에 설정되어 있지 않습니다.");
        }

        return create(provider, apiKey);
    }

    public static String defaultModel(LlmProvider provider) {
        return switch (provider) {
            case OPENAI -> "gpt-4o-mini";
            case ANTHROPIC -> "claude-sonnet-4-20250514";
            case GEMINI -> "gemini-2.0-flash";
            case OLLAMA -> "llama3.2";
        };
    }
}
