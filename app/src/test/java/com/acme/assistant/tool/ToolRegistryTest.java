package com.acme.assistant.tool;

import com.acme.assistant.model.tool.FunctionTool;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class ToolRegistryTest {

    @Test
    void 도구를_등록하고_조회한다() {
        ToolRegistry registry = new ToolRegistry();
        Tool tool = createTool("test_tool", "테스트 도구");
        registry.register(tool);

        Optional<Tool> found = registry.getTool("test_tool");

        assertThat(found).isPresent();
        assertThat(found.get().name()).isEqualTo("test_tool");
    }

    @Test
    void 미등록_도구를_조회하면_빈_Optional을_반환한다() {
        ToolRegistry registry = new ToolRegistry();

        Optional<Tool> found = registry.getTool("nonexistent");

        assertThat(found).isEmpty();
    }

    @Test
    void toFunctionTools로_API_형식_목록을_반환한다() {
        ToolRegistry registry = new ToolRegistry();
        registry.register(createTool("tool_a", "도구 A"));
        registry.register(createTool("tool_b", "도구 B"));

        List<FunctionTool> functionTools = registry.toFunctionTools();

        assertThat(functionTools).hasSize(2);
        assertThat(functionTools.get(0).type()).isEqualTo("function");
        assertThat(functionTools.get(0).function().name()).isEqualTo("tool_a");
        assertThat(functionTools.get(1).function().name()).isEqualTo("tool_b");
    }

    private Tool createTool(String name, String description) {
        return new AbstractTool(
                new ToolDefinition(name, description, JsonSchemaBuilder.objectSchema().build())
        ) {
            @Override
            public ToolResult execute(ToolInput input, ToolContext context) {
                return ToolResult.success("executed");
            }
        };
    }
}
