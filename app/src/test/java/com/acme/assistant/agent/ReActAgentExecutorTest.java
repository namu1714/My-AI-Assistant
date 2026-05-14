package com.acme.assistant.agent;

import com.acme.assistant.llm.LlmModel;
import com.acme.assistant.llm.LlmResponse;
import com.acme.assistant.llm.LlmToolCall;
import com.acme.assistant.llm.TokenUsage;
import com.acme.assistant.llm.client.MockLlmClient;
import com.acme.assistant.tool.CurrentTimeTool;
import com.acme.assistant.tool.ToolRegistry;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ReActAgentExecutorTest {

    private final LlmModel model = new LlmModel("gpt-4o");

    private Agent createAgent(MockLlmClient mockClient) {
        return DefaultAgent.builder()
                .name("test-agent")
                .llmClient(mockClient)
                .llmModel(model)
                .build();
    }

    @Test
    void 도구_없이_텍스트_응답을_반환한다() {
        var mockClient = new MockLlmClient();
        mockClient.enqueue("안녕하세요!");

        var executor = new ReActAgentExecutor();
        var response = executor.execute(
                createAgent(mockClient),
                new AgentRequest("안녕"));

        assertThat(response.content()).isEqualTo("안녕하세요!");
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.iterationsUsed()).isEqualTo(1);
    }

    @Test
    void 기본_컨텍스트로_실행한다() {
        var mockClient = new MockLlmClient();
        mockClient.enqueue("응답");

        var executor = new ReActAgentExecutor();
        var response = executor.execute(
                createAgent(mockClient),
                new AgentRequest("질문"));

        assertThat(response.isSuccess()).isTrue();
    }

    @Test
    void 도구_1회_사용_후_최종_응답을_반환한다() {
        var mockClient = new MockLlmClient();

        // 1회: 도구 호출 응답
        var toolCallResponse = new LlmResponse(
                null,
                List.of(new LlmToolCall(
                        "call_1", "current_time", "{}")),
                new TokenUsage(10, 5));
        mockClient.enqueue(toolCallResponse);

        // 2회: 최종 텍스트 응답
        mockClient.enqueue(new LlmResponse(
                "현재 시간은 14:30 입니다.",
                new TokenUsage(20, 10)));

        var registry = new ToolRegistry();
        registry.register(new CurrentTimeTool());

        var agent = DefaultAgent.builder()
                .name("test-agent")
                .llmClient(mockClient)
                .llmModel(new LlmModel("gpt-4o"))
                .toolRegistry(registry)
                .systemPrompt(" 당신은 AI 비서입니다.")
                .build();

        var executor = new ReActAgentExecutor();
        var response = executor.execute(
                agent, new AgentRequest("지금 몇 시야?"));

        assertThat(response.content()).contains("14:30");
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.iterationsUsed()).isEqualTo(2);
    }

    @Test
    void 존재하지_않는_도구_호출은_에러_결과를_반환한다() {
        var mockClient = new MockLlmClient();

        // 존재하지 않는 도구 호출
        mockClient.enqueue(new LlmResponse(
                null,
                List.of(new LlmToolCall(
                        "call_1", "unknown_tool", "{}")),
                new TokenUsage(10, 5)));

        // 최종 응답
        mockClient.enqueue(new LlmResponse(
                "도구를 찾지 못했습니다.",
                new TokenUsage(15, 8)));

        var registry = new ToolRegistry();
        registry.register(new CurrentTimeTool());

        var agent = DefaultAgent.builder()
                .name("test-agent")
                .llmClient(mockClient)
                .llmModel(new LlmModel("gpt-4o"))
                .toolRegistry(registry)
                .systemPrompt(" 당신은 AI 비서입니다.")
                .build();

        var executor = new ReActAgentExecutor();
        var response = executor.execute(
                agent, new AgentRequest("미지의 도구 사용"));

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.iterationsUsed()).isEqualTo(2);
    }

    @Test
    void 실행_메타데이터를_추적한다() {
        var mockClient = new MockLlmClient();

        // 1회: 도구 호출
        mockClient.enqueue(new LlmResponse(
                null,
                List.of(new LlmToolCall(
                        "call_1", "current_time", "{}")),
                new TokenUsage(10, 5)));

        // 2회: 최종 응답
        mockClient.enqueue(new LlmResponse(
                "완료", new TokenUsage(15, 8)));

        var registry = new ToolRegistry();
        registry.register(new CurrentTimeTool());

        var agent = DefaultAgent.builder()
                .name("test-agent")
                .llmClient(mockClient)
                .llmModel(new LlmModel("gpt-4o"))
                .toolRegistry(registry)
                .systemPrompt("AI 비서")
                .build();

        var executor = new ReActAgentExecutor();
        executor.execute(
                agent, new AgentRequest("시간?"));

        var metadata = executor.getLastExecutionMetadata();
        assertThat(metadata).isNotNull();
        assertThat(metadata.iterationCount()).isEqualTo(2);
        assertThat(metadata.toolCallCount()).isEqualTo(1);
        assertThat(metadata.totalTokenUsage().totalTokens())
                .isEqualTo(38); // (10+5) + (15+8) = 38
    }

    @Test
    void LLM_호출_실패_시_ERROR_상태로_종료한다() {
        var mockClient = new MockLlmClient();

        // 큐가 비어 있으면 LlmException 발생
        var executor = new ReActAgentExecutor();
        var response = executor.execute(
                createAgent(mockClient),
                new AgentRequest(" 안녕"));

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.finalState())
                .isEqualTo(AgentState.ERROR);
        assertThat(response.content())
                .contains("LLM 호출 실패");
    }

    @Test
    void 최대_반복_도달_시_FINISHED_상태로_종료한다() {
        var mockClient = new MockLlmClient();

        // 계속 도구를 호출하는 응답 3 개
        for (int i = 0; i < 3; i++) {
            mockClient.enqueue(new LlmResponse(
                    "생각 중...",
                    List.of(new LlmToolCall(
                            "call_" + i, "current_time", "{}")),
                    new TokenUsage(5, 3)));
        }

        var registry = new ToolRegistry();
        registry.register(new CurrentTimeTool());

        var agent = DefaultAgent.builder()
                .name("test-agent")
                .llmClient(mockClient)
                .llmModel(new LlmModel("gpt-4o"))
                .toolRegistry(registry)
                .systemPrompt("AI 비서")
                .build();

        var executor = new ReActAgentExecutor();
        var context = new ExecutionContext(
                "test", null, 3, null);
        var response = executor.execute(
                agent, new AgentRequest("시간 알려줘"),
                context);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.iterationsUsed()).isEqualTo(3);
    }
}
