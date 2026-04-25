package com.acme.assistant.tool;

import com.acme.assistant.model.tool.FunctionTool;

import java.util.*;

public class ToolRegistry {

    private final Map<String, Tool> tools = new LinkedHashMap<>();

    public void register(Tool tool) {
        tools.put(tool.name(), tool);
    }

    public Optional<Tool> getTool(String name) {
        return Optional.ofNullable(tools.get(name));
    }

    public Collection<Tool> getAllTools() {
        return tools.values();
    }

    public List<FunctionTool> toFunctionTools() {
        return tools.values().stream()
                .map(tool -> FunctionTool.of(
                        tool.name(),
                        tool.description(),
                        tool.parameterSchema()
                ))
                .toList();
    }
}
