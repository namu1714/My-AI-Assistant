package com.acme.assistant.memory;

import com.acme.assistant.llm.ChatMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class InMemoryConversationRepository implements ConversationRepository {

    private final ConcurrentMap<String, List<ChatMessage>> store = new ConcurrentHashMap<>();

    @Override
    public void save(String conversationId, List<ChatMessage> messages) {
        store.put(conversationId, List.copyOf(messages));
    }

    @Override
    public Optional<List<ChatMessage>> load(String conversationId) {
        List<ChatMessage> messages = store.get(conversationId);
        if (messages == null) {
            return Optional.empty();
        }
        return Optional.of(new ArrayList<>(messages));
    }

    @Override
    public List<String> list() {
        return List.copyOf(store.keySet());
    }

    @Override
    public void delete(String conversationId) {
        store.remove(conversationId);
    }
}
