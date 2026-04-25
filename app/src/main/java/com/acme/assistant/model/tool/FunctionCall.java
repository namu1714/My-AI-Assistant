package com.acme.assistant.model.tool;

public record FunctionCall(
        String name,
        String arguments
) { }
