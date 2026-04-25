package com.acme.assistant.tool;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class JsonSchemaBuilderTest {

    @Test
    @SuppressWarnings("unchecked")
    void 프로퍼티를_추가한다() {
        Map<String, Object> schema = JsonSchemaBuilder.objectSchema()
                .property("name", "string", "사용자 이름")
                .property("age", "integer", "사용자 나이")
                .build();

        Map<String, Object> properties =
                (Map<String, Object>) schema.get("properties");
        assertThat(properties).containsKeys("name", "age");

        Map<String, Object> nameProp = (Map<String, Object>) properties.get("name");
        assertThat(nameProp.get("type")).isEqualTo("string");
        assertThat(nameProp.get("description")).isEqualTo("사용자 이름");
    }

    @Test
    @SuppressWarnings("unchecked")
    void enum_프로퍼티를_추가한다() {
        Map<String, Object> schema = JsonSchemaBuilder.objectSchema()
                .enumProperty("unit", List.of("celsius", "fahrenheit"), "온도 단위")
                .required("unit")
                .build();

        Map<String, Object> properties =
                (Map<String, Object>) schema.get("properties");
        Map<String, Object> unitProp =
                (Map<String, Object>) properties.get("unit");
        assertThat(unitProp.get("enum"))
                .isEqualTo(List.of("celsius", "fahrenheit"));
    }
}
