package com.acme.assistant.memory;

import com.acme.assistant.llm.ChatMessage;

import java.util.List;
import java.util.Optional;

public interface ConversationMemory {

    void addMessage(ChatMessage message);

    List<ChatMessage> getMessages();

    Optional<ChatMessage> getSystemMessage();

    void setSystemMessage(ChatMessage systemMessage);

    void clear();

    int messageCount();
}
