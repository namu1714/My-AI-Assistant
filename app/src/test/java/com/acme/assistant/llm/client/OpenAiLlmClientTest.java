package com.acme.assistant.llm.client;

import com.acme.assistant.client.OpenAiClient;
import com.acme.assistant.llm.*;
import com.acme.assistant.model.ChatResponse;
import com.acme.assistant.model.Choice;
import com.acme.assistant.model.Message;
import com.acme.assistant.model.Usage;
import com.acme.assistant.model.tool.FunctionCall;
import com.acme.assistant.model.tool.ToolCall;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class OpenAiLlmClientTest {

    OpenAiLlmClient client = new OpenAiLlmClient(
            new OpenAiClient("test-key")
    );

    @Test
    void toLlmResponse_textOnly_convertsCorrectly() {
        Message message = Message.ofAssistant(" 안녕하세요");

        ChatResponse response = new ChatResponse(
                "resp-1", "gpt-4o-mini",
                List.of(new Choice(0, message, "stop")),
                new Usage(100, 50, 150)
        );

        LlmResponse result = client.toLlmResponse(response);

        assertThat(result.content()).isEqualTo(" 안녕하세요");
        assertThat(result.hasToolCalls()).isFalse();
        assertThat(result.tokenUsage().inputTokens()).isEqualTo(100);
        assertThat(result.tokenUsage().outputTokens()).isEqualTo(50);
    }

    @Test
    void toLlmResponse_withToolCalls_convertsCorrectly() {
        var toolCalls = List.of(
                new ToolCall("call_1", "function",
                        new FunctionCall("file_read", "{\"path\":\"a.txt\"}"))
        );

        Message message = Message.ofAssistant("", toolCalls);

        ChatResponse response = new ChatResponse(
                "resp-2", "gpt-4o-mini",
                List.of(new Choice(0, message, "tool_calls")),
                new Usage(200, 30, 230)
        );

        LlmResponse result = client.toLlmResponse(response);

        assertThat(result.hasToolCalls()).isTrue();
        assertThat(result.toolCalls()).hasSize(1);
        assertThat(result.toolCalls().getFirst().name()).isEqualTo("file_read");
    }

    @Test
    void toOpenAiMessage_convertsAllRoles() {
        Message system = client.toOpenAiMessage(
                ChatMessage.ofSystem("시스템"));
        assertThat(system.role()).isEqualTo("system");

        Message user = client.toOpenAiMessage(
                ChatMessage.ofUser("사용자"));
        assertThat(user.role()).isEqualTo("user");

        Message assistant = client.toOpenAiMessage(
                ChatMessage.ofAssistant("어시스턴트"));
        assertThat(assistant.role()).isEqualTo("assistant");

        Message tool = client.toOpenAiMessage(
                ChatMessage.ofTool("call_1", "결과"));
        assertThat(tool.role()).isEqualTo("tool");
        assertThat(tool.toolCallId()).isEqualTo("call_1");
    }

    @Test
    void toTokenUsage_nullUsage_returnsEmpty() {
        TokenUsage result = client.toTokenUsage(null);
        assertThat(result).isEqualTo(TokenUsage.EMPTY);
    }

    @Test
    void toLlmToolCalls_convertsCorrectly() {
        var toolCalls = List.of(
                new ToolCall("call_1", "function",
                        new FunctionCall("grep", "{\"pattern\":\"TODO\"}"))
        );
        List<LlmToolCall> result = client.toLlmToolCalls(toolCalls);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().id()).isEqualTo("call_1");
        assertThat(result.getFirst().name()).isEqualTo("grep");
        assertThat(result.getFirst().arguments())
                .isEqualTo("{\"pattern\":\"TODO\"}");
    }
}
