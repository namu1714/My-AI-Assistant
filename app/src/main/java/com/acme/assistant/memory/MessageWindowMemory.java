package com.acme.assistant.memory;

import com.acme.assistant.llm.ChatMessage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class MessageWindowMemory implements ConversationMemory {

    private final List<ChatMessage> messages = new ArrayList<>();
    private final int maxMessages;
    private ChatMessage systemMessage;

    public MessageWindowMemory(int maxMessages) {
        this.maxMessages = maxMessages;
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

    private void trimIfNeeded() {
        if (maxMessages <= 0) {
            return;
        }
        while (messages.size() > maxMessages) {
            messages.removeFirst();
        }
    }
}
