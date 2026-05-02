package com.acme.assistant.llm;

import com.acme.assistant.tool.ToolDefinition;

import java.util.List;

public interface LlmClient {

    LlmResponse chat(LlmModel model, List<ChatMessage> messages);

    LlmResponse chat(LlmModel model, List<ChatMessage> messages, List<ToolDefinition> tools);
}
