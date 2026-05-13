package com.acme.assistant.agent;

import com.acme.assistant.tool.ToolDefinition;

import java.util.List;

public interface Planner {

    Plan createPlan(String task, List<ToolDefinition> availableTools);
}
