package com.acme.assistant.tool;

import java.util.Map;

public interface Tool {

    String name();

    String description();

    Map<String, Object> parameterSchema();

    ToolResult execute(ToolInput input, ToolContext context);
}
