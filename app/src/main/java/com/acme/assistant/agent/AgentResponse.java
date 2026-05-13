package com.acme.assistant.agent;

import com.acme.assistant.llm.TokenUsage;

public record AgentResponse(
        String content,
        AgentState finalState,
        int iterationsUsed,
        TokenUsage tokenUsage
) {

    public static AgentResponse success(String content,
                                        int iterations,
                                        TokenUsage tokenUsage) {
        return new AgentResponse(content, AgentState.FINISHED, iterations, tokenUsage);
    }

    public static AgentResponse error(String errorMessage,
                                      int iterations,
                                      TokenUsage usage) {
        return new AgentResponse(errorMessage, AgentState.ERROR, iterations, usage);
    }

    public boolean isSuccess() {
        return finalState == AgentState.FINISHED;
    }
}
