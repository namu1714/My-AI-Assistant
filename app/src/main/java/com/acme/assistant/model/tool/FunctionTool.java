package com.acme.assistant.model.tool;

import java.util.Map;

/**
 * API function tool definition that can be serialized to JSON and sent to the assistant.
 */
public record FunctionTool(
        String type,
        Function function
) {
    public record Function(
            String name,
            String description,
            Map<String, Object> parameters
    ) {}

    public static FunctionTool of(
            String name,
            String description,
            Map<String, Object> parameters
    ) {
        return new FunctionTool("function", new Function(name, description, parameters));
    }
}
