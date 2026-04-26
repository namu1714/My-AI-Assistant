package com.acme.assistant.tool;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class AbstractToolTest {

    @Test
    void 구체_도구를_생성하고_실행한다() {
        Map<String, Object> params = JsonSchemaBuilder.objectSchema()
                .property("input", "string", "입력값")
                .build();

        ToolDefinition definition = new ToolDefinition(
                "echo",
                "입력값을 그대로 반환한다",
                params
        );

        Tool tool = new AbstractTool(definition) {
            @Override
            public ToolResult execute(ToolInput input, ToolContext context) {
                return ToolResult.success("echo: " + input.requireString("input"));
            }
        };

        ToolInput input = ToolInput.parse("{\"input\": \"hello\"}");
        ToolContext context = ToolContext.empty();

        assertThat(tool.name()).isEqualTo("echo");
        assertThat(tool.description()).isEqualTo("입력값을 그대로 반환한다");
        assertThat(tool.execute(input, context).content())
                .isEqualTo("echo: hello");
    }
}
