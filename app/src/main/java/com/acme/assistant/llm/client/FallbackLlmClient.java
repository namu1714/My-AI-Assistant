package com.acme.assistant.llm.client;

import com.acme.assistant.exception.LlmException;
import com.acme.assistant.llm.ChatMessage;
import com.acme.assistant.llm.LlmModel;
import com.acme.assistant.llm.LlmResponse;
import com.acme.assistant.tool.ToolDefinition;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class FallbackLlmClient implements LlmClient {

    private final List<LlmClient> clients;

    public FallbackLlmClient(List<LlmClient> clients) {
        if (clients == null || clients.isEmpty()) {
            throw new IllegalArgumentException("최소 하나의 LlmClient가 필요합니다");
        }
        this.clients = List.copyOf(clients);
    }

    public FallbackLlmClient(LlmClient primary,
                             LlmClient... fallbacks) {
        List<LlmClient> all = new ArrayList<>();
        all.add(primary);
        all.addAll(List.of(fallbacks));
        this.clients = List.copyOf(all);
    }

    @Override
    public LlmResponse chat(LlmModel model, List<ChatMessage> messages) {
        return tryClients(client -> client.chat(model, messages));
    }

    @Override
    public LlmResponse chat(LlmModel model, List<ChatMessage> messages, List<ToolDefinition> tools) {
        return tryClients(client -> client.chat(model, messages, tools));
    }

    private LlmResponse tryClients(Function<LlmClient, LlmResponse> action) {
        RuntimeException lastException = null;

        for (LlmClient client : clients) {
            try {
                return action.apply(client);
            } catch (RuntimeException e) {
                lastException = e;
            }
        }
        throw new LlmException("fallback", "모든 LLM 제공자가 실패했습니다", lastException);
    }
}
