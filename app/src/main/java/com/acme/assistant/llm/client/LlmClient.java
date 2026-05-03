package com.acme.assistant.llm.client;

import com.acme.assistant.llm.ChatMessage;
import com.acme.assistant.llm.LlmModel;
import com.acme.assistant.llm.LlmResponse;
import com.acme.assistant.tool.ToolDefinition;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface LlmClient {

    LlmResponse chat(LlmModel model, List<ChatMessage> messages);

    LlmResponse chat(LlmModel model, List<ChatMessage> messages, List<ToolDefinition> tools);

    default CompletableFuture<LlmResponse> chatAsync(
            LlmModel model, List<ChatMessage> messages) {
        return CompletableFuture.supplyAsync(() -> chat(model, messages));
    }

    default CompletableFuture<LlmResponse> chatAsync(
            LlmModel model, List<ChatMessage> messages, List<ToolDefinition> tools) {
        return CompletableFuture.supplyAsync(() -> chat(model, messages, tools));
    }
}
