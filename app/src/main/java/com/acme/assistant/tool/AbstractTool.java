package com.acme.assistant.tool;

import java.util.Map;

public abstract class AbstractTool implements Tool {

    private final ToolDefinition definition;

    protected AbstractTool(ToolDefinition definition) {
        this.definition = definition;
    }

    @Override
    public String name() {
        return definition.name();
    }
    @Override
    public String description() {
        return definition.description();
    }
    @Override
    public Map<String, Object> parameterSchema() {
        return definition.parameters();
    }

    public ToolDefinition definition() {
        return definition;
    }
}
