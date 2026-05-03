package com.acme.assistant.llm.client;

import com.acme.assistant.client.OpenAiClient;
import com.acme.assistant.exception.LlmException;
import com.acme.assistant.llm.*;
import com.acme.assistant.model.ChatRequest;
import com.acme.assistant.model.ChatResponse;
import com.acme.assistant.model.Message;
import com.acme.assistant.model.Usage;
import com.acme.assistant.model.tool.FunctionCall;
import com.acme.assistant.model.tool.FunctionTool;
import com.acme.assistant.model.tool.ToolCall;
import com.acme.assistant.tool.ToolDefinition;

import java.util.List;

public class OpenAiLlmClient implements LlmClient {

    private final OpenAiClient openAiClient;

    public OpenAiLlmClient(OpenAiClient openAiClient) {
        this.openAiClient = openAiClient;
    }

    @Override
    public LlmResponse chat(LlmModel model, List<ChatMessage> messages) {
        ChatRequest request = new ChatRequest(
                model.name(),
                toOpenAiMessages(messages),
                model.temperature(),
                model.maxTokens(),
                null, null, null, null
        );
        return execute(request);
    }

    @Override
    public LlmResponse chat(LlmModel model, List<ChatMessage> messages, List<ToolDefinition> tools) {
        List<FunctionTool> functionTools = tools.stream()
                .map(td -> FunctionTool.of(td.name(), td.description(), td.parameters()))
                .toList();

        ChatRequest request = new ChatRequest(
                model.name(),
                toOpenAiMessages(messages),
                model.temperature(),
                model.maxTokens(),
                null, null,
                functionTools, "auto"
        );
        return execute(request);
    }

    private LlmResponse execute(ChatRequest request) {
        try {
            ChatResponse response = openAiClient.chat(request);
            return toLlmResponse(response);
        } catch (Exception e) {
            throw new LlmException("openai: ", e.getMessage(), e);
        }
    }

    List<Message> toOpenAiMessages(List<ChatMessage> messages) {
        return messages.stream()
                .map(this::toOpenAiMessage)
                .toList();
    }

    Message toOpenAiMessage(ChatMessage chatMessage) {
        return switch (chatMessage.role()) {
            case SYSTEM -> Message.ofSystem(chatMessage.content());
            case USER -> Message.ofUser(chatMessage.content());
            case ASSISTANT -> {
                if (chatMessage.toolCalls() != null && !chatMessage.toolCalls().isEmpty()) {
                    yield Message.ofAssistant(
                            chatMessage.content(),
                            toToolCalls(chatMessage.toolCalls())
                    );
                }
                yield Message.ofAssistant(chatMessage.content());
            }
            case TOOL -> Message.ofTool(chatMessage.toolCallId(), chatMessage.content());
        };
    }

    LlmResponse toLlmResponse(ChatResponse response) {
        var choice = response.choices().getFirst();
        String content = choice.message().content() != null
                ? choice.message().content().toString() : "";

        List<LlmToolCall> toolCalls = List.of();
        if (choice.message().toolCalls() != null) {
            toolCalls = toLlmToolCalls(choice.message().toolCalls());
        }

        TokenUsage tokenUsage = toTokenUsage(response.usage());

        return new LlmResponse(content, toolCalls, tokenUsage);
    }

    TokenUsage toTokenUsage(Usage usage) {
        if (usage == null) {
            return TokenUsage.EMPTY;
        }
        return new TokenUsage(
                usage.promptTokens(),
                usage.completionTokens(),
                usage.totalTokens()
        );
    }

    List<LlmToolCall> toLlmToolCalls(List<ToolCall> toolCalls) {
        return toolCalls.stream()
                .map(tc -> new LlmToolCall(
                        tc.id(),
                        tc.function().name(),
                        tc.function().arguments()
                ))
                .toList();
    }

    List<ToolCall> toToolCalls(List<LlmToolCall> llmToolCalls) {
        return llmToolCalls.stream()
                .map(ltc -> new ToolCall(
                        ltc.id(),
                        "function",
                        new FunctionCall(ltc.name(), ltc.arguments())
                ))
                .toList();
    }
}
