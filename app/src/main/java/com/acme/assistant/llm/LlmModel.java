package com.acme.assistant.llm;

public record LlmModel(
        String name,
        Double temperature,
        Integer maxTokens
) {
    public LlmModel(String name) {
        this(name, null, null);
    }

    public LlmModel withTemperature(double temperature) {
        return new LlmModel(name, temperature, maxTokens);
    }

    public LlmModel withMaxTokens(int maxTokens) {
        return new LlmModel(name, temperature, maxTokens);
    }
}
