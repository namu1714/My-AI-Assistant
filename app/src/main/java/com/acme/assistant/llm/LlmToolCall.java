package com.acme.assistant.llm;

public record LlmToolCall(
        String id,
        String name,
        String arguments
) {}
