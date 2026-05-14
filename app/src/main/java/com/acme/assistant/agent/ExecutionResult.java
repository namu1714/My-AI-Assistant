package com.acme.assistant.agent;

import com.acme.assistant.llm.LlmResponse;
import com.acme.assistant.tool.ToolUseResult;

import java.time.Duration;
import java.util.List;

public record ExecutionResult(
        int iteration,
        AgentState state,
        LlmResponse llmResponse,
        List<ToolUseResult> toolResults,
        Duration duration
) {
    public ExecutionResult {
        if (toolResults == null) {
            toolResults = List.of();
        }
    }

    public boolean hasToolCalls() {
        return !toolResults.isEmpty();
    }
}
