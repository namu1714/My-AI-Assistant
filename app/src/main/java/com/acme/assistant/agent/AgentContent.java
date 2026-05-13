package com.acme.assistant.agent;

import com.acme.assistant.llm.LlmModel;
import com.acme.assistant.llm.client.LlmClient;
import com.acme.assistant.memory.ConversationMemory;
import com.acme.assistant.tool.ToolRegistry;

public record AgentContent(
        LlmClient llmClient,
        LlmModel llmModel,
        ToolRegistry toolRegistry,
        ConversationMemory memory,
        String systemPrompt
) {
    public AgentContent {
        if (llmClient == null) {
            throw new IllegalArgumentException("llmClient must not be null");
        }
        if (llmModel == null) {
            throw new IllegalArgumentException("llmModel must not be null");
        }
    }
}
