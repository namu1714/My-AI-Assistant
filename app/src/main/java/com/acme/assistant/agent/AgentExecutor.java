package com.acme.assistant.agent;

public interface AgentExecutor {

    AgentResponse execute(Agent agent, AgentRequest request);

    AgentResponse execute(Agent agent, AgentRequest request, ExecutionContext context);
}
