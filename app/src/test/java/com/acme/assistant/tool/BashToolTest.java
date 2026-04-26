package com.acme.assistant.tool.implementation;

import com.acme.assistant.tool.ToolContext;
import com.acme.assistant.tool.ToolInput;
import com.acme.assistant.tool.ToolResult;
import com.acme.assistant.tool.bash.BashTool;
import com.acme.assistant.tool.bash.CommandValidator;
import com.acme.assistant.tool.file.PathValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

public class BashToolTest {

    @TempDir
    Path tempDir;

    @Test
    void 명령을_실행하고_결과를_반환한다() {
        BashTool tool = new BashTool(
                new CommandValidator(), new PathValidator(tempDir)
        );
        ToolInput input = ToolInput.parse(
                "{\"command\":\"echo hello\"}"
        );

        ToolResult result = tool.execute(input, ToolContext.empty());

        assertThat(result.isError()).isFalse();
        assertThat(result.content()).contains("hello");
        assertThat(result.content()).contains("exit_code: 0");
    }

    @Test
    void 위험한_명령은_차단된다() {
        BashTool tool = new BashTool(
                new CommandValidator(), new PathValidator(tempDir)
        );
        ToolInput input = ToolInput.parse(
                "{\"command\":\"rm -rf /\"}"
        );
        ToolResult result = tool.execute(input, ToolContext.empty());

        assertThat(result.isError()).isTrue();
        assertThat(result.content()).contains(" 명령 차단");
    }

    @Test
    void 타임아웃이_적용된다() {
        BashTool tool = new BashTool(
                new CommandValidator(), new PathValidator(tempDir)
        );
        ToolInput input = ToolInput.parse(
                "{\"command\":\"sleep 10\", \"timeout\":1}"
        );

        ToolResult result = tool.execute(input, ToolContext.empty());
        assertThat(result.isError()).isTrue();
        assertThat(result.content()).contains("타임아웃");
    }
}
