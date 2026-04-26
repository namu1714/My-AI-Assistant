package com.acme.assistant.tool.implementation;

import com.acme.assistant.tool.ToolContext;
import com.acme.assistant.tool.ToolInput;
import com.acme.assistant.tool.ToolResult;
import com.acme.assistant.tool.file.FileEditTool;
import com.acme.assistant.tool.file.FileReadTool;
import com.acme.assistant.tool.file.GrepTool;
import com.acme.assistant.tool.file.PathValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class FileToolTest {

    @TempDir
    Path tempDir;

    @Test
    void 상대_경로를_기본_디렉터리_기준으로_해석한다() {
        PathValidator validator = new PathValidator(tempDir);

        Path result = validator.validate("test.txt");

        assertThat(result).isEqualTo(
                tempDir.resolve("test.txt").normalize());
    }

    @Test
    void 디렉터리_트래버설을_차단한다() {
        PathValidator validator = new PathValidator(tempDir);

        assertThatThrownBy(() ->
                validator.validate("../../etc/passwd"))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("기본 디렉터리 바깥 접근 차단");
    }

    @Test
    void offset과_limit으로_부분_읽기를_한다() throws Exception {
        Path file = tempDir.resolve("lines.txt");
        Files.writeString(file, "line1\nline2\nline3\nline4\nline5");

        FileReadTool tool = new FileReadTool(new PathValidator(tempDir));
        ToolInput input = ToolInput.parse(
                "{\"path\":\"lines.txt\",\"offset\":1,\"limit\":2}"
        );

        ToolResult result = tool.execute(input, ToolContext.empty());

        assertThat(result.isError()).isFalse();
        assertThat(result.content()).isEqualTo("line2\nline3");
    }

    @Test
    void 문자열을_교체한다() throws Exception {
        Path file = tempDir.resolve("test.txt");
        Files.writeString(file, "Hello World!");

        FileEditTool tool = new FileEditTool(new PathValidator(tempDir));
        ToolInput input = ToolInput.parse(
                "{\"path\":\"test.txt\","
                        + "\"old_text\":\"World\","
                        + "\"new_text\":\"Java\"}");

        ToolResult result = tool.execute(input, ToolContext.empty());

        assertThat(result.isError()).isFalse();
        assertThat(Files.readString(file)).isEqualTo("Hello Java!");
    }

    @Test
    void old_text가_여러_번_나타나면_에러를_반환한다() throws Exception {
        Path file = tempDir.resolve("test.txt");
        Files.writeString(file, "aaa bbb aaa");

        FileEditTool tool = new FileEditTool(new PathValidator(tempDir));
        ToolInput input = ToolInput.parse(
                "{\"path\":\"test.txt\","
                        + "\"old_text\":\"aaa\","
                        + "\"new_text\":\"ccc\"}");

        ToolResult result = tool.execute(input, ToolContext.empty());

        assertThat(result.isError()).isTrue();
        assertThat(result.content()).contains("2번 발견");
    }

    @Test
    void 패턴과_일치하는_줄을_찾는다() throws Exception {
        Files.writeString(tempDir.resolve("Hello.java"), """
                public class Hello {
                    // TODO: implement hello
                }
                """);
        Files.writeString(tempDir.resolve("World.java"), """
                public class World {
                    // TODO: implement world
                }
                """);

        GrepTool tool = new GrepTool(new PathValidator(tempDir));
        ToolInput input = ToolInput.parse("{\"pattern\":\"TODO\"}");

        ToolResult result = tool.execute(input, ToolContext.empty());
        assertThat(result.isError()).isFalse();
        assertThat(result.content()).contains("TODO");
    }
}
