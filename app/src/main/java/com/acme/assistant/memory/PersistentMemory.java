package com.acme.assistant.memory;

import com.acme.assistant.llm.ChatMessage;
import com.acme.assistant.llm.Role;

import java.util.List;
import java.util.Optional;

public class PersistentMemory implements ConversationMemory {

    private final ConversationMemory delegate;
    private final ConversationRepository repository;
    private final String conversationId;

    public PersistentMemory(ConversationMemory delegate,
                            ConversationRepository repository,
                            String conversationId) {
        this.delegate = delegate;
        this.repository = repository;
        this.conversationId = conversationId;
        restore();
    }

    @Override
    public void addMessage(ChatMessage message) {
        delegate.addMessage(message);
        save();
    }

    @Override
    public List<ChatMessage> getMessages() {
        return delegate.getMessages();
    }

    @Override
    public Optional<ChatMessage> getSystemMessage() {
        return delegate.getSystemMessage();
    }

    @Override
    public void setSystemMessage(ChatMessage systemMessage) {
        delegate.setSystemMessage(systemMessage);
    }

    @Override
    public void clear() {
        delegate.clear();
        repository.delete(conversationId);
    }

    @Override
    public int messageCount() {
        return delegate.messageCount();
    }

    private void restore() {
        repository.load(conversationId).ifPresent(messages -> {
            for (ChatMessage message : messages) {
                delegate.addMessage(message);
            }
        });
    }

    private void save() {
        List<ChatMessage> messages = delegate.getMessages().stream()
                .filter(m -> m.role() != Role.SYSTEM)
                .toList();
        repository.save(conversationId, delegate.getMessages());
    }
}
