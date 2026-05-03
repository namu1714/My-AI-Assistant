package com.acme.assistant.llm.client;

import com.acme.assistant.client.OpenAiClient;
import com.acme.assistant.llm.ChatMessage;
import com.acme.assistant.llm.LlmModel;
import com.acme.assistant.llm.LlmResponse;
import com.acme.assistant.tool.ToolDefinition;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class OllamaLlmClient implements LlmClient {

    private final OpenAiLlmClient delegate;

    public OllamaLlmClient(String host, int port) {
        String baseUrl = "http://" + host + ":" + port;
        OpenAiClient openAiClient = new OpenAiClient("ollama", baseUrl);
        this.delegate = new OpenAiLlmClient(openAiClient);
    }

    public OllamaLlmClient() {
        this("localhost", 11434);
    }

    @Override
    public LlmResponse chat(LlmModel model, List<ChatMessage> messages) {
        return delegate.chat(model, messages);
    }

    @Override
    public LlmResponse chat(LlmModel model, List<ChatMessage> messages, List<ToolDefinition> tools) {
        return delegate.chat(model, messages, tools);
    }

    @Override
    public CompletableFuture<LlmResponse> chatAsync(
            LlmModel model, List<ChatMessage> messages) {
        return delegate.chatAsync(model, messages);
    }

    @Override
    public CompletableFuture<LlmResponse> chatAsync(
            LlmModel model, List<ChatMessage> messages, List<ToolDefinition> tools) {
        return delegate.chatAsync(model, messages, tools);
    }
}
