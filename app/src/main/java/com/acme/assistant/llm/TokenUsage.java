package com.acme.assistant.llm;

public record TokenUsage(
        int inputTokens,
        int outputTokens,
        int totalTokens
) {
    public static final TokenUsage EMPTY = new TokenUsage(0, 0, 0);

    public TokenUsage(int inputTokens, int outputTokens) {
        this(inputTokens, outputTokens, inputTokens + outputTokens);
    }

    public TokenUsage add(TokenUsage other) {
        return new TokenUsage(
                this.inputTokens + other.inputTokens,
                this.outputTokens + other.outputTokens,
                this.totalTokens + other.totalTokens
        );
    }
}
