package com.acme.assistant.memory;

import com.acme.assistant.llm.ChatMessage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class TokenWindowMemory implements ConversationMemory {

    private final List<ChatMessage> messages = new ArrayList<>();
    private final int maxTokens;
    private ChatMessage systemMessage;

    public TokenWindowMemory(int maxTokens) {
        if (maxTokens <= 0) {
            throw new IllegalArgumentException("maxTokens는 양수여야 합니다: " + maxTokens);
        }
        this.maxTokens = maxTokens;
    }

    @Override
    public void addMessage(ChatMessage message) {
        messages.add(message);
        trimIfNeeded();
    }

    @Override
    public List<ChatMessage> getMessages() {
        List<ChatMessage> result = new ArrayList<>();
        if (systemMessage != null) {
            result.add(systemMessage);
        }
        result.addAll(messages);
        return Collections.unmodifiableList(result);
    }

    @Override
    public Optional<ChatMessage> getSystemMessage() {
        return Optional.ofNullable(systemMessage);
    }

    @Override
    public void setSystemMessage(ChatMessage systemMessage) {
        this.systemMessage = systemMessage;
    }

    @Override
    public void clear() {
        messages.clear();
    }

    @Override
    public int messageCount() {
        return messages.size();
    }

    public int estimatedTokens() {
        int total = 0;
        if (systemMessage != null) {
            total += TokenEstimator.estimate(systemMessage);
        }
        total += TokenEstimator.estimate(messages);
        return total;
    }

    private void trimIfNeeded() {
        int systemTokens = systemMessage != null
                ? TokenEstimator.estimate(systemMessage) : 0;
        int budget = maxTokens - systemTokens;

        while (!messages.isEmpty()
                && TokenEstimator.estimate(messages) > budget) {
            messages.removeFirst();
        }
    }
}
