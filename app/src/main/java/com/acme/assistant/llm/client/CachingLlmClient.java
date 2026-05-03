package com.acme.assistant.llm.client;

import com.acme.assistant.llm.ChatMessage;
import com.acme.assistant.llm.LlmModel;
import com.acme.assistant.llm.LlmResponse;
import com.acme.assistant.tool.ToolDefinition;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class CachingLlmClient implements LlmClient {

    private final LlmClient delegate;
    private final ConcurrentHashMap<String, LlmResponse> cache = new ConcurrentHashMap<>();

    public CachingLlmClient(LlmClient delegate) {
        this.delegate = delegate;
    }

    @Override
    public LlmResponse chat(LlmModel model, List<ChatMessage> messages) {
        String key = cacheKey(model, messages);
        return cache.computeIfAbsent(key, k -> delegate.chat(model, messages));
    }

    @Override
    public LlmResponse chat(LlmModel model, List<ChatMessage> messages, List<ToolDefinition> tools) {
        // 도구 호출이 가능한 요청은 캐시하지 않는다
        return delegate.chat(model, messages, tools);
    }

    String cacheKey(LlmModel model, List<ChatMessage> messages) {
        int hash = Objects.hash(model.name(), model.temperature(), model.maxTokens());
        for (ChatMessage message : messages) {
            hash = 31 * hash + Objects.hash(message.role(), message.content());
        }
        return String.valueOf(hash);
    }

    public int cacheSize() {
        return cache.size();
    }

    public void clearCache() {
        cache.clear();
    }
}
