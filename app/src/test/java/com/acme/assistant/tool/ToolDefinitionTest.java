package com.acme.assistant.tool;

import com.acme.assistant.model.tool.FunctionTool;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class ToolDefinitionTest {

    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
                .setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    @Test
    void toFunctionTool로_API_형식으로_변환한다() throws Exception {
        Map<String, Object> params = JsonSchemaBuilder.objectSchema()
                .property("query", "string", "검색어")
                .required("query")
                .build();

        ToolDefinition definition = new ToolDefinition(
                "search", "웹 검색을 수행한다", params
        );

        FunctionTool functionTool = definition.toFunctionTool();

        assertThat(functionTool.type()).isEqualTo("function");
        assertThat(functionTool.function().name()).isEqualTo("search");

        String json = mapper.writeValueAsString(functionTool);
        assertThat(json).contains("\"type\":\"function\"");
        assertThat(json).contains("\"name\":\"search\"");
    }
}
