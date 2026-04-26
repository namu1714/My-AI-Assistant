package com.acme.assistant.tool.implementation;

import com.acme.assistant.tool.ToolContext;
import com.acme.assistant.tool.ToolInput;
import com.acme.assistant.tool.ToolResult;
import com.acme.assistant.tool.todo.TodoItem;
import com.acme.assistant.tool.todo.TodoStore;
import com.acme.assistant.tool.todo.TodoTool;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TodoToolTest {

    @Test
    void 작업을_추가한다() {
        TodoTool tool = new TodoTool(new TodoStore());
        ToolInput input = ToolInput.parse(
                "{\"action\":\"add\",\"title\":\"테스트 작성\"}");

        ToolResult result = tool.execute(
                input, ToolContext.empty());

        assertThat(result.isError()).isFalse();
        assertThat(result.content()).contains("작업 추가됨");
        assertThat(result.content()).contains("테스트 작성");
    }

    @Test
    void 빈_목록을_조회한다() {
        TodoTool tool = new TodoTool(new TodoStore());
        ToolInput input = ToolInput.parse(
                "{\"action\":\"list\"}");

        ToolResult result = tool.execute(
                input, ToolContext.empty());

        assertThat(result.isError()).isFalse();
        assertThat(result.content()).contains("비어 있습니다");
    }

    @Test
    void 작업을_완료한다() {
        TodoStore store = new TodoStore();
        TodoItem item = store.add("완료할 작업");
        TodoTool tool = new TodoTool(store);

        ToolInput input = ToolInput.parse(
                "{\"action\":\"complete\",\"id\":" + item.id() + "}");

        ToolResult result = tool.execute(
                input, ToolContext.empty());

        assertThat(result.isError()).isFalse();
        assertThat(result.content()).contains("작업 완료");
    }
}
