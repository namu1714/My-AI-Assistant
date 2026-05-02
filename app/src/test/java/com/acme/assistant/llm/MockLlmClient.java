package com.acme.assistant.llm;

import com.acme.assistant.exception.LlmException;
import com.acme.assistant.tool.ToolDefinition;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class MockLlmClient implements LlmClient {

    private final Queue<LlmResponse> responses = new ArrayDeque<>();
    private final List<List<ChatMessage>> receivedMessages = new ArrayList<>();

    public void enqueue(LlmResponse response) {
        responses.add(response);
    }

    public void enqueue(String content) {
        enqueue(new LlmResponse(content, TokenUsage.EMPTY));
    }

    public List<List<ChatMessage>> receivedMessages() {
        return receivedMessages;
    }

    public int callCount() {
        return receivedMessages.size();
    }

    @Override
    public LlmResponse chat(LlmModel model, List<ChatMessage> messages) {
        return dequeue(messages);
    }

    @Override
    public LlmResponse chat(LlmModel model, List<ChatMessage> messages, List<ToolDefinition> tools) {
        return dequeue(messages);
    }

    private LlmResponse dequeue(List<ChatMessage> messages) {
        receivedMessages.add(messages);
        if (responses.isEmpty()) {
            throw new LlmException("mock", "응답 큐가 비어 있습니다. enqueue()로 응답을 추가하세요.");
        }
        return responses.poll();
    }
}
