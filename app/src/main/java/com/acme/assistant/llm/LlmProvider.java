package com.acme.assistant.llm;

public enum LlmProvider {

    OPENAI("openai"),
    ANTHROPIC("anthropic"),
    GEMINI("gemini"),
    OLLAMA("ollama");

    private final String value;

    LlmProvider(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    public static LlmProvider from(String value) {
        for (LlmProvider provider : values()) {
            if (provider.value.equalsIgnoreCase(value) || provider.name().equalsIgnoreCase(value)) {
                return provider;
            }
        }
        throw new IllegalArgumentException("알 수 없는 LLM 제공자: " + value);
    }
}
