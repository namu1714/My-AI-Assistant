package com.acme.assistant.tool;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Optional;

public class ToolInput {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final ObjectNode root;

    private ToolInput(ObjectNode root) {
        this.root = root;
    }

    public static ToolInput parse(String json) {
        try {
            return new ToolInput((ObjectNode) MAPPER.readTree(json));
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(
                    "JSON 파일 오류: " + e.getMessage(), e);
        }
    }

    public static ToolInput empty() {
        return new ToolInput(MAPPER.createObjectNode());
    }

    public String requireString(String name) {
        var node = root.get(name);
        if (node == null || node.isNull()) {
            throw new IllegalArgumentException("필수 파라미터 누락: " + name);
        }
        return node.asText();
    }

    public int requireInt(String name) {
        var node = root.get(name);
        if (node == null || node.isNull()) {
            throw new IllegalArgumentException("필수 파라미터 누락: " + name);
        }
        if (!node.isNumber()) {
            throw new IllegalArgumentException("파라미터 타입 오류: " + name + " (정수 필요)");
        }
        return node.asInt();
    }

    public Optional<String> optionalString(String name) {
        var node = root.get(name);
        if (node == null || node.isNull()) {
            return Optional.empty();
        }
        return Optional.of(node.asText());
    }

    public boolean optionalBoolean(String name, boolean defaultValue) {
        var node = root.get(name);
        if (node == null || node.isNull()) {
            return defaultValue;
        }
        return node.asBoolean();
    }

    public boolean has(String name) {
        return root.has(name) && !root.get(name).isNull();
    }
}
