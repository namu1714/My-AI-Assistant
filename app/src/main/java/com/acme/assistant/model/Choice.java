package com.acme.assistant.model;

public record Choice(
        int index,
        Message message,
        String finishReason
) {}
