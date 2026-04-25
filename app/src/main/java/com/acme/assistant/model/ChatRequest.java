package com.acme.assistant.model;

import com.acme.assistant.model.tool.FunctionTool;

import java.util.List;

public record ChatRequest(
        String model,
        List<Message> messages,
        Double temperature,
        Integer maxTokens,
        Boolean stream,
        ResponseFormat responseFormat,
        List<FunctionTool> tools,
        String toolChoice
) {
    public ChatRequest(String model, List<Message> messages) {
        this(model, messages, null, null, null, null, null, null);
    }
}
