package com.acme.assistant.model.tool;

public record ToolCall(
        String id,
        String type,
        FunctionCall function
) { }
