package com.acme.assistant.tool.validator;

import com.acme.assistant.tool.ToolContext;

public interface ToolPermissionValidator {

    boolean isAllowed(String toolName, ToolContext context);
}
