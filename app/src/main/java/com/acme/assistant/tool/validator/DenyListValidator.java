package com.acme.assistant.tool.validator;

import com.acme.assistant.tool.ToolContext;

import java.util.Set;

public class DenyListValidator implements ToolPermissionValidator {

    private final Set<String> deniedTools;

    public DenyListValidator(String... toolNames) {
        this.deniedTools = Set.of(toolNames);
    }

    @Override
    public boolean isAllowed(String toolName, ToolContext context) {
        return !deniedTools.contains(toolName);
    }
}
