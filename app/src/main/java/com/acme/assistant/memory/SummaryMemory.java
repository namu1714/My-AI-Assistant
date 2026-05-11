package com.acme.assistant.memory;

import com.acme.assistant.llm.ChatMessage;
import com.acme.assistant.llm.LlmModel;
import com.acme.assistant.llm.client.LlmClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class SummaryMemory implements ConversationMemory {

    private final List<ChatMessage> messages = new ArrayList<>();
    private final LlmClient llmClient;
    private final LlmModel llmModel;
    private final int maxMessages;
    private ChatMessage systemMessage;
    private String summary;

    public SummaryMemory(LlmClient llmClient, LlmModel llmModel, int maxMessages) {
        this.llmClient = llmClient;
        this.llmModel = llmModel;
        this.maxMessages = maxMessages;
    }

    @Override
    public void addMessage(ChatMessage message) {
        messages.add(message);
        if (messages.size() > maxMessages) {
            summarize();
        }
    }

    @Override
    public List<ChatMessage> getMessages() {
        List<ChatMessage> result = new ArrayList<>();
        if (systemMessage != null) {
            String systemContent = systemMessage.content();
            if (summary != null) {
                systemContent += "\n\n이전 대화 요약: " + summary;
            }
            result.add(ChatMessage.ofSystem(systemContent));
        } else if (summary != null) {
            result.add(ChatMessage.ofSystem("이전 대화 요약: " + summary));
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
        summary = null;
    }

    @Override
    public int messageCount() {
        return messages.size();
    }

    public String getSummary() {
        return summary;
    }

    private void summarize() {
        int halfSize = messages.size() / 2;
        List<ChatMessage> oldMessages = new ArrayList<>(messages.subList(0, halfSize));

        StringBuilder sb = new StringBuilder();
        if (summary != null) {
            sb.append("기존 대화 요약:\n").append(summary).append("\n\n");
        }
        sb.append("다음 대화 내용을 간결하게 요약해 주세요:\n\n");

        for (ChatMessage msg : oldMessages) {
            sb.append(msg.role().value()).append(": ")
                    .append(msg.content()).append("\n");
        }
        List<ChatMessage> summaryRequest = List.of(
                ChatMessage.ofUser(sb.toString()));

        var response = llmClient.chat(llmModel, summaryRequest);
        summary = response.content();

        messages.subList(0, halfSize).clear();
    }
}
