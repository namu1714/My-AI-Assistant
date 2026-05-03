package com.acme.assistant.llm.client;

import com.acme.assistant.exception.LlmException;
import com.acme.assistant.llm.*;
import com.acme.assistant.tool.ToolDefinition;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class AnthropicLlmClient implements LlmClient {

    private static final String BASE_URL = "https://api.anthropic.com";
    private static final String API_VERSION = "2023-06-01";
    private static final int DEFAULT_MAX_TOKENS = 1024;

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;

    public AnthropicLlmClient(String apiKey) {
        this(apiKey, HttpClient.newHttpClient());
    }

    public AnthropicLlmClient(String apiKey, HttpClient httpClient) {
        this.apiKey = apiKey;
        this.httpClient = httpClient;
        this.objectMapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
                .setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    @Override
    public LlmResponse chat(LlmModel model, List<ChatMessage> messages) {
        return doChat(model, messages, null);
    }

    @Override
    public LlmResponse chat(LlmModel model, List<ChatMessage> messages, List<ToolDefinition> tools) {
        return doChat(model, messages, tools);
    }

    private LlmResponse doChat(LlmModel model, List<ChatMessage> messages, List<ToolDefinition> tools) {
        try {
            ObjectNode requestBody = buildRequestBody(model, messages, tools);
            String json = objectMapper.writeValueAsString(requestBody);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/v1/messages"))
                    .header("Content-Type", "application/json")
                    .header("x-api-key", apiKey)
                    .header("anthropic-version", API_VERSION)
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new LlmException("anthropic", "API 오류 (HTTP " + response.statusCode() + "): " + response.body());
            }

            JsonNode responseJson = objectMapper.readTree(response.body());
            return parseResponse(responseJson);

        } catch (LlmException e) {
            throw e;
        } catch (Exception e) {
            throw new LlmException("anthropic", e.getMessage(), e);
        }
    }

    ObjectNode buildRequestBody(LlmModel model, List<ChatMessage> messages, List<ToolDefinition> tools) {
        // TODO
        return objectMapper.createObjectNode();
    }

    LlmResponse parseResponse(JsonNode responseJson) {
        String text = extractText(responseJson);
        List<LlmToolCall> toolCalls = extractToolCalls(responseJson);
        TokenUsage tokenUsage = toTokenUsage(responseJson.get("usage"));

        return new LlmResponse(text, toolCalls, tokenUsage);
    }

    String extractText(JsonNode responseJson) {
        JsonNode content = responseJson.get("content");
        if (content == null || !content.isArray()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (JsonNode block : content) {
            if ("text".equals(block.path("type").asText())) {
                if (!sb.isEmpty()) {
                    sb.append("\n");
                }
                sb.append(block.path("text").asText(""));
            }
        }
        return sb.toString();
    }

    List<LlmToolCall> extractToolCalls(JsonNode responseJson) {
        JsonNode content = responseJson.get("content");
        if (content == null || !content.isArray()) {
            return List.of();
        }

        List<LlmToolCall> toolCalls = new ArrayList<>();

        for (JsonNode block : content) {
            if ("tool_use".equals(block.path("type").asText())) {
                String id = block.path("id").asText();
                String name = block.path("name").asText();
                String arguments = block.path("input").toString();

                toolCalls.add(new LlmToolCall(id, name, arguments));
            }
        }
        return toolCalls;
    }

    TokenUsage toTokenUsage(JsonNode usage) {
        if (usage == null) {
            return TokenUsage.EMPTY;
        }
        int inputTokens = usage.path("input_tokens").asInt(0);
        int outputTokens = usage.path("output_tokens").asInt(0);

        return new TokenUsage(inputTokens, outputTokens);
    }

    ArrayNode toAnthropicMessages(List<ChatMessage> messages) {
        ArrayNode array = objectMapper.createArrayNode();

        List<ChatMessage> nonSystem = messages.stream()
                .filter(m -> m.role() != Role.SYSTEM)
                .toList();

        // 연속된 같은 역할 메시지 병합
        List<ObjectNode> merged = mergeConsecutiveRoles(nonSystem);
        merged.forEach(array::add);

        return array;
    }

    ArrayNode toAnthropicTools(List<ToolDefinition> tools) {
        ArrayNode array = objectMapper.createArrayNode();

        for (ToolDefinition tool : tools) {
            ObjectNode toolNode = objectMapper.createObjectNode();
            toolNode.put("name", tool.name());
            toolNode.put("description", tool.description());
            toolNode.set("input_schema", objectMapper.valueToTree(tool.parameters()));

            array.add(toolNode);
        }
        return array;
    }

    String extractSystemPrompt(List<ChatMessage> messages) {
        return messages.stream()
                .filter(m -> m.role() == Role.SYSTEM)
                .map(ChatMessage::content)
                .findFirst()
                .orElse(null);
    }

    String toAnthropicRole(Role role) {
        return switch (role) {
            case USER, TOOL -> "user";
            case ASSISTANT -> "assistant";
            case SYSTEM -> throw new IllegalArgumentException("시스템 메시지는 별도 필드로 처리합니다");
        };
    }

    private List<ObjectNode> mergeConsecutiveRoles(List<ChatMessage> messages) {
        // 이전 메시지와 현재 메시지의 역할이 같으면 content 배열을 합친다.
        // TODO: implement this method

        return messages.stream()
                .map(m -> {
                    ObjectNode node = objectMapper.createObjectNode();
                    node.put("role", toAnthropicRole(m.role()));
                    node.put("type", "text");
                    node.put("text", m.content());
                    return node;
                })
                .toList();
    }
}
