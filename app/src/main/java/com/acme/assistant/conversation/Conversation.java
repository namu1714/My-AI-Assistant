package com.acme.assistant.conversation;

import com.acme.assistant.model.Message;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Conversation {

    private final List<Message> messages = new ArrayList<>();
    private final String systemPrompt;
    private final int maxMessages;

    public Conversation() {
        this(null);
    }

    public Conversation(String systemPrompt) {
        this(systemPrompt, 0);
    }

    public Conversation(String systemPrompt, int maxMessages) {
        this.systemPrompt = systemPrompt;
        this.maxMessages = maxMessages;
        if (systemPrompt != null && !systemPrompt.isBlank()) {
            messages.add(Message.ofSystem(systemPrompt));
        }
    }

    public void addUserMessage(String content) {
        messages.add(Message.ofUser(content));
        trimIfNeeded();
    }

    public void addAssistantMessage(String content) {
        messages.add(Message.ofAssistant(content));
    }

    public List<Message> getMessages() {
        return Collections.unmodifiableList(messages);
    }

    private void trimIfNeeded() {
        if (maxMessages <= 0) {
            return;
        }

        int systemOffset = hasSystemPrompt() ? 1 : 0;
        int userAssistantCount = messages.size() - systemOffset;

        while (userAssistantCount > maxMessages) {
            messages.remove(systemOffset); // 시스템 프롬프트 다음 메시지부터 제거
            userAssistantCount--;
        }
    }

    private boolean hasSystemPrompt() {
        return systemPrompt != null && !systemPrompt.isBlank();
    }
}
