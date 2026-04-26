package com.acme.assistant.tool.validator;

import com.acme.assistant.tool.ToolContext;

public class AllowAllValidator implements ToolPermissionValidator {

    @Override
    public boolean isAllowed(String toolName, ToolContext context) {
        return true;
    }
}
