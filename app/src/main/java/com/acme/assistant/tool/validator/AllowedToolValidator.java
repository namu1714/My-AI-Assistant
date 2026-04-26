package com.acme.assistant.tool.validator;

import com.acme.assistant.tool.ToolContext;

import java.util.Set;

public class AllowedToolValidator implements ToolPermissionValidator {

    private final Set<String> allowedTools;

    public AllowedToolValidator(String... toolNames) {
        this.allowedTools = Set.of(toolNames);
    }

    @Override
    public boolean isAllowed(String toolName, ToolContext context) {
        return allowedTools.contains(toolName);
    }
}
