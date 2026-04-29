package com.acme.assistant.tool;

import com.acme.assistant.tool.file.FileReadTool;
import com.acme.assistant.tool.validator.AllowedToolValidator;
import com.acme.assistant.tool.file.PathValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

public class ToolExecutionManagerTest {

    private final ToolRegistry registry = new ToolRegistry();
    private final ToolExecutionManager manager = new ToolExecutionManager(registry);
    private final PathValidator validator = new PathValidator(Path.of("temp/"));

    @BeforeEach
    void setUp() {
        registry.register(new CurrentTimeTool());
        registry.register(new FileReadTool(validator));
    }

    @Test
    void 도구를_정상적으로_실행한다() {
        ToolUse toolUse = new ToolUse("call_1", "current_time", "{}");

        ToolUseResult result = manager.execute(toolUse, ToolContext.empty());

        assertThat(result.isError()).isFalse();
        assertThat(result.toolCallId()).isEqualTo("call_1");
    }

    @Test
    void 존재하지_않는_도구를_호출하면_에러를_반환한다() {
        ToolUse toolUse = new ToolUse("call_2", "unknown_tool", "{}");

        ToolUseResult result = manager.execute(toolUse, ToolContext.empty());

        assertThat(result.isError()).isTrue();
        assertThat(result.content()).contains("알 수 없는 도구: unknown_tool");
    }

    @Test
    void 잘못된_JSON_입력에_에러를_반환한다() {
        ToolUse toolUse = new ToolUse("call_3", "current_time", "not json");

        ToolUseResult result = manager.execute(toolUse, ToolContext.empty());

        assertThat(result.isError()).isTrue();
        assertThat(result.content()).contains("입력 파싱 오류");
    }

    @Test
    void 도구_실행_중_예외가_발생하면_에러_결과를_반환한다() {
        Tool failingTool = new AbstractTool(
                new ToolDefinition("failing", "항상 실패하는 도구", JsonSchemaBuilder.objectSchema().build())
        ) {
            @Override
            public ToolResult execute(ToolInput input, ToolContext context) {
                throw new RuntimeException("예상치 못한 오류");
            }
        };
        registry.register(failingTool);

        ToolUse toolUse = new ToolUse("can_fail", "failing", "{}");

        ToolUseResult result = manager.execute(toolUse, ToolContext.empty());

        assertThat(result.isError()).isTrue();
        assertThat(result.content()).contains("도구 실행 오류");
        assertThat(result.content()).contains("예상치 못한 오류");
    }

    @Test
    void 권한이_거부되면_에러를_반환한다() {
        AllowedToolValidator validator = new AllowedToolValidator("current_time");
        ToolExecutionManager restrictedManager = new ToolExecutionManager(registry, validator);

        ToolUse toolUse = new ToolUse("call_4", "file_read", "{}");

        ToolUseResult result = restrictedManager.execute(toolUse, ToolContext.empty());

        assertThat(result.isError()).isTrue();
        assertThat(result.content()).contains("실행이 거부되었습니다");
    }
}
