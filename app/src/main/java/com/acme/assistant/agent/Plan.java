package com.acme.assistant.agent;

import java.util.List;

public record Plan(
        String goal,
        List<Step> steps
) {
    public boolean isEmpty() {
        return steps == null || steps.isEmpty();
    }

    public int size() {
        return steps == null ? 0 : steps.size();
    }
}
