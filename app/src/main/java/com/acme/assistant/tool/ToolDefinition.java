package com.acme.assistant.tool;

import com.acme.assistant.model.tool.FunctionTool;

import java.util.Map;

public record ToolDefinition(
        String name,
        String description,
        Map<String, Object> parameters
) {
    public FunctionTool toFunctionTool() {
        return FunctionTool.of(name, description, parameters);
    }
}
