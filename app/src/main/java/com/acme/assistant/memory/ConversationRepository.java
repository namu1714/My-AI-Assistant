package com.acme.assistant.memory;

import com.acme.assistant.llm.ChatMessage;

import java.util.List;
import java.util.Optional;

public interface ConversationRepository {

    void save(String conversationId, List<ChatMessage> messages);

    Optional<List<ChatMessage>> load(String conversationId);

    List<String> list();

    void delete(String conversationId);
}
