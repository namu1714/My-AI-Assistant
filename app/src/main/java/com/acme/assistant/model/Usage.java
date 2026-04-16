package com.acme.assistant.model;

public record Usage(
        int promptTokens,
        int completionTokens,
        int totalTokens
) {}
