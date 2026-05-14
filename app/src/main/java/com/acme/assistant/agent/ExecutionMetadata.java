package com.acme.assistant.agent;

import com.acme.assistant.llm.TokenUsage;

import java.time.Duration;
import java.util.List;

public record ExecutionMetadata(
        List<ExecutionResult> results,
        Duration totalDuration,
        TokenUsage totalTokenUsage
) {

    public ExecutionMetadata {
        if (results == null) {
            results = List.of();
        }
    }

    public int iterationCount() {
        return results.size();
    }

    public long toolCallCount() {
        return results.stream()
                .mapToLong(r -> r.toolResults().size())
                .sum();
    }
}
