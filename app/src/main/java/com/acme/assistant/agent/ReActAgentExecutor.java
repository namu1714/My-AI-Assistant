package com.acme.assistant.agent;

import com.acme.assistant.exception.LlmException;
import com.acme.assistant.llm.ChatMessage;
import com.acme.assistant.llm.LlmResponse;
import com.acme.assistant.llm.TokenUsage;
import com.acme.assistant.memory.ConversationMemory;
import com.acme.assistant.memory.MessageWindowMemory;
import com.acme.assistant.tool.*;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class ReActAgentExecutor implements AgentExecutor {

    private ExecutionMetadata lastExecutionMetadata;

    @Override
    public AgentResponse execute(Agent agent, AgentRequest request) {
        return execute(agent, request, new ExecutionContext("default"));
    }

    @Override
    public AgentResponse execute(Agent agent, AgentRequest request, ExecutionContext context) {
        Instant executionStart = Instant.now();
        AgentContent content = agent.content();

        // 메모리 준비
        ConversationMemory memory = content.memory();
        if (memory == null) {
            memory = new MessageWindowMemory(100);
        }

        // 도구 준비
        ToolRegistry toolRegistry = content.toolRegistry();
        ToolExecutionManager toolManager = null;
        List<ToolDefinition> toolDefs = List.of();

        if (toolRegistry != null
                && !toolRegistry.getAllTools().isEmpty())  {
            toolManager = new ToolExecutionManager(toolRegistry);
            toolDefs = toolRegistry.toToolDefinitions();
        }

        // 시스템 프롬프트 설정
        String systemPrompt = SystemPromptBuilder.build(
                content.systemPrompt(), toolRegistry);

        if (!systemPrompt.isEmpty()) {
            memory.setSystemMessage(
                    ChatMessage.ofSystem(systemPrompt));
        }

        // 사용자 메시지 추가
        memory.addMessage(ChatMessage.ofUser(request.message()));

        // ReAct 루프
        TokenUsage totalUsage = TokenUsage.EMPTY;
        List<ExecutionResult> results = new ArrayList<>();
        int iteration = 0;
        String lastContent = null;

        while (iteration < context.maxIterations()) {
            iteration++;
            Instant iterStart = Instant.now();

            // THINKING: LLM 호출
            LlmResponse llmResponse;

            try {
                if (!toolDefs.isEmpty()) {
                    llmResponse = content.llmClient().chat(
                            content.llmModel(),
                            memory.getMessages(),
                            toolDefs);
                } else {
                    llmResponse = content.llmClient().chat(
                            content.llmModel(),
                            memory.getMessages());
                }
            } catch (LlmException e) {
                Duration iterDuration = Duration.between(iterStart, Instant.now());
                results.add(new ExecutionResult(
                        iteration, AgentState.ERROR,
                        null, List.of(), iterDuration));

                lastExecutionMetadata = new ExecutionMetadata(
                        results,
                        Duration.between(executionStart, Instant.now()),
                        totalUsage);

                return AgentResponse.error(
                        "LLM 호출 실패: " + e.getMessage(),
                        iteration, totalUsage);
            }

            totalUsage = totalUsage.add(llmResponse.tokenUsage());

            memory.addMessage(llmResponse.toAssistantMessage());
            lastContent = llmResponse.content();

            // 도구 호출이 없으면 최종 응답
            if (!llmResponse.hasToolCalls()) {
                Duration iterDuration = Duration.between(iterStart, Instant.now());
                results.add(new ExecutionResult(
                        iteration, AgentState.FINISHED,
                llmResponse, List.of(), iterDuration));

                lastExecutionMetadata = new ExecutionMetadata(
                        results,
                        Duration.between(executionStart, Instant.now()),
                        totalUsage
                );

                return AgentResponse.success(
                        llmResponse.content(), iteration, totalUsage);
            }

            // ACTING: 도구 실행
            List<ToolUseResult> toolResults = new ArrayList<>();
            ToolContext toolContext = ToolContext.of(
                    context.conversationId(),
                    context.userId());

            for (var toolCall : llmResponse.toolCalls()) {
                ToolUse toolUse = ToolUse.from(toolCall);

                ToolUseResult result;
                if (toolManager != null) {
                    result = toolManager.execute(toolUse, toolContext);
                } else {
                    result = ToolUseResult.error(
                            toolUse.id(),
                            "도구를 실행할 수 없습니다: " + toolUse.name()
                    );
                }
                // 도구 결과를 메모리에 추가
                toolResults.add(result);
                memory.addMessage(ChatMessage.ofTool(
                        toolUse.id(),
                        result.content()));
            }

            Duration iterDuration = Duration.between(iterStart, Instant.now());
            results.add(new ExecutionResult(
                    iteration, AgentState.ACTING,
                    llmResponse, toolResults, iterDuration
            ));
        }

        lastExecutionMetadata = new ExecutionMetadata(
                results,
                Duration.between(executionStart, Instant.now()),
                totalUsage
        );

        String finalContent = lastContent != null ? lastContent : "최대 반복 횟수에 도달했습니다.";
        return AgentResponse.success(finalContent, iteration, totalUsage);
    }

    public ExecutionMetadata getLastExecutionMetadata() {
        return lastExecutionMetadata;
    }
}
