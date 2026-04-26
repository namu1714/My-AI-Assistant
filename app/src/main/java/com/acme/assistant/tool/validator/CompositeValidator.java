package com.acme.assistant.tool.validator;

import com.acme.assistant.tool.ToolContext;

import java.util.List;

public class CompositeValidator implements ToolPermissionValidator {

    private final List<ToolPermissionValidator> validators;

    public CompositeValidator(ToolPermissionValidator... validators) {
        this.validators = List.of(validators);
    }

    @Override
    public boolean isAllowed(String toolName, ToolContext context) {
        return validators.stream()
                .allMatch(v -> v.isAllowed(toolName, context));
    }
}
