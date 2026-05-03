package com.acme.assistant.llm.client;

import com.acme.assistant.llm.*;
import com.acme.assistant.tool.ToolDefinition;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.net.http.HttpClient;
import java.util.ArrayList;
import java.util.List;

public class GeminiLlmClient implements LlmClient {

    private static final String BASE_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;

    public GeminiLlmClient(String apiKey) {
        this(apiKey, HttpClient.newHttpClient());
    }

    public GeminiLlmClient(String apiKey, HttpClient httpClient) {
        this.apiKey = apiKey;
        this.httpClient = httpClient;
        this.objectMapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
                .setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    @Override
    public LlmResponse chat(LlmModel model, java.util.List<ChatMessage> messages) {
        // TODO
        return null;
    }

    @Override
    public LlmResponse chat(LlmModel model, List<ChatMessage> messages, List<ToolDefinition> tools) {
        // TODO
        return null;
    }

    ArrayNode toGeminiContents(List<ChatMessage> messages) throws Exception {
        ArrayNode contents = objectMapper.createArrayNode();

        List<ChatMessage> nonSystem = messages.stream()
                .filter(m -> m.role() != Role.SYSTEM)
                .toList();

        for (ChatMessage message : nonSystem) {
            ObjectNode content = objectMapper.createObjectNode();
            content.put("role", toGeminiRole(message.role()));

            ArrayNode parts = objectMapper.createArrayNode();

            if (message.role() == Role.TOOL) {
                ObjectNode functionResponse = objectMapper.createObjectNode();
                functionResponse.put("name", message.toolCallId());

                ObjectNode responseData = objectMapper.createObjectNode();
                responseData.put("result", message.content());
                functionResponse.set("response", responseData);

                ObjectNode part = objectMapper.createObjectNode();
                part.set("functionResponse", functionResponse);
                parts.add(part);

            } else if (message.toolCalls() != null && !message.toolCalls().isEmpty()) {
                for (LlmToolCall toolCall : message.toolCalls()) {
                    ObjectNode functionCall = objectMapper.createObjectNode();
                    functionCall.put("name", toolCall.name());
                    functionCall.set("args", objectMapper.readTree(toolCall.arguments()));

                    ObjectNode part = objectMapper.createObjectNode();
                    part.set("functionCall", functionCall);
                    parts.add(part);
                }
            } else {
                ObjectNode part = objectMapper.createObjectNode();
                part.put("text", message.content());
                parts.add(part);
            }

            content.set("parts", parts);
            contents.add(content);
        }
        return contents;
    }

    String toGeminiRole(Role role) {
        return switch (role) {
            case USER, TOOL -> "user";
            case ASSISTANT -> "model";
            case SYSTEM -> throw new IllegalArgumentException("시스템 메시지는 systemInstruction으로 처리합니다");
        };
    }

    ArrayNode toGeminiTools(List<ToolDefinition> tools) {
        ArrayNode toolsArray = objectMapper.createArrayNode();
        ObjectNode toolWrapper = objectMapper.createObjectNode();
        ArrayNode declarations = objectMapper.createArrayNode();

        for (ToolDefinition tool : tools) {
            ObjectNode decl = objectMapper.createObjectNode();
            decl.put("name", tool.name());
            decl.put("description", tool.description());
            decl.set("parameters",
                    objectMapper.valueToTree(tool.parameters()));
            declarations.add(decl);
        }
        toolWrapper.set("functionDeclarations", declarations);
        toolsArray.add(toolWrapper);

        return toolsArray;
    }

    LlmResponse parseResponse(JsonNode responseJson) {
        JsonNode candidates = responseJson.get("candidates");
        if (candidates == null || candidates.isEmpty()) {
            return new LlmResponse("", List.of(), TokenUsage.EMPTY);
        }

        JsonNode firstCandidate = candidates.get(0);
        JsonNode content = firstCandidate.get("content");
        JsonNode parts = content != null ? content.get("parts") : null;

        String text = extractText(parts);
        List<LlmToolCall> toolCalls = extractToolCalls(parts);
        TokenUsage tokenUsage = toTokenUsage(responseJson.get("usageMetadata"));

        return new LlmResponse(text, toolCalls, tokenUsage);
    }

    String extractText(JsonNode parts) {
        if (parts == null || !parts.isArray()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (JsonNode part : parts) {
            if (part.has("text")) {
                JsonNode textNode = part.get("text");
                if (!sb.isEmpty()) {
                    sb.append("\n");
                }
                sb.append(textNode.asText());
            }
        }
        return sb.toString();
    }

    List<LlmToolCall> extractToolCalls(JsonNode parts) {
        if (parts == null || !parts.isArray()) {
            return List.of();
        }
        List<LlmToolCall> toolCalls = new ArrayList<>();
        int index = 0;
        for (JsonNode part : parts) {
            if (part.has("functionCall")) {
                JsonNode fc = part.get("functionCall");
                String name = fc.get("name").asText();
                String arguments = fc.has("args")
                        ? fc.get("args").toString() : "{}";
                String id = "call_" + index;
                toolCalls.add(new LlmToolCall(id, name, arguments));
                index++;
            }
        }
        return toolCalls;
    }

    TokenUsage toTokenUsage(JsonNode usageMetadata) {
        if (usageMetadata == null) {
            return TokenUsage.EMPTY;
        }
        int inputTokens = usageMetadata.path("promptTokenCount").asInt(0);
        int outputTokens =
                usageMetadata.path("candidatesTokenCount").asInt(0);
        return new TokenUsage(inputTokens, outputTokens);
    }
}
