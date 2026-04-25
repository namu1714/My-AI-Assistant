package com.acme.assistant.tool;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class JsonSchemaBuilder {

    private final Map<String, Object> properties = new LinkedHashMap<>();
    private final List<String> requiredFields = new ArrayList<>();

    public static JsonSchemaBuilder objectSchema() {
        return new JsonSchemaBuilder();
    }

    public JsonSchemaBuilder property(String name, String type, String description) {
        Map<String, Object> prop = new LinkedHashMap<>();
        prop.put("type", type);
        prop.put("description", description);
        properties.put(name, prop);
        return this;
    }

    public JsonSchemaBuilder enumProperty(String name, List<String> values, String description) {
        Map<String, Object> prop = new LinkedHashMap<>();
        prop.put("type", "string");
        prop.put("description", description);
        prop.put("enum", values);
        properties.put(name, prop);
        return this;
    }

    public JsonSchemaBuilder required(String... names) {
        requiredFields.addAll(List.of(names));
        return this;
    }

    public Map<String, Object> build() {
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");
        schema.put("properties", new LinkedHashMap<>(properties));
        if (!requiredFields.isEmpty()) {
            schema.put("required", new ArrayList<>(requiredFields));
        }
        return schema;
    }
}
