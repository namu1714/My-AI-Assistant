package com.acme.assistant;

import com.acme.assistant.agent.*;
import com.acme.assistant.llm.*;
import com.acme.assistant.llm.client.LlmClient;
import com.acme.assistant.llm.client.LlmClientFactory;
import com.acme.assistant.memory.*;
import com.acme.assistant.rag.*;
import com.acme.assistant.tool.*;
import com.acme.assistant.tool.file.*;

import java.nio.file.Path;
import java.util.Scanner;


public class Main {

    public static void main(String[] args) throws Exception {
        String providerName = System.getenv("LLM_PROVIDER");
        if (providerName == null) {
            providerName = "openai";
        }
        LlmProvider provider = LlmProvider.from(providerName);

        // LlmClient 생성
        LlmClient llmClient = LlmClientFactory.fromEnvironment();
        LlmModel llmModel = new LlmModel(
                LlmClientFactory.defaultModel(provider));

        System.out.println("=== 15장 로컬 정보 검색 Agent ===");
        System.out.println("[LLM 제공자] " + provider.value());

        // 도구 등록
        var toolRegistry = new ToolRegistry();
        var pathValidator = new PathValidator(
                Path.of(".").toAbsolutePath().normalize());

        toolRegistry.register(new CurrentTimeTool());
        toolRegistry.register(new FileReadTool(pathValidator));
        toolRegistry.register(new GrepTool(pathValidator));

        // Agent 생성
        var agent = DefaultAgent.builder()
                .name("local-search-agent")
                .description("로컬 파일을 검색하고 분석하는 에이전트")
                .llmClient(llmClient)
                .llmModel(llmModel)
                .toolRegistry(toolRegistry)
                .memory(new MessageWindowMemory(50))
                .systemPrompt(
                        "당신은 로컬 파일 시스템을 탐색하여"
                        + " 사용자의 질문에 답하는 AI 비서입니다. "
                        + " 파일을 읽거나 검색하여 정확한 정보를"
                        + " 제공합니다.")
                .build();

        // ReActAgentExecutor 생성
        var executor = new ReActAgentExecutor();
        var context = new ExecutionContext("main-session");

        System.out.println("[도구] current_time, file_read, grep");
        System.out.println("[메모리] MessageWindowMemory(50)\n");

        // 대화 루프
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("사용자 > ");
            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("quit") || input.equalsIgnoreCase("exit")) {
                break;
            }

            if (input.isEmpty()) {
                continue;
            }

            // Agent 실행
            AgentResponse response = executor.execute(agent, new AgentRequest(input), context);

            System.out.println("\n[비서] " + response.content());

            // 실행 메타데이터 출력
            ExecutionMetadata metadata = executor.getLastExecutionMetadata();
            if (metadata != null) {
                System.out.println("[반복] " + metadata.iterationCount() + " 회, ");
                System.out.println("[도구 호출] " + metadata.toolCallCount() + " 회, ");
                System.out.println("[토큰] " + metadata.totalTokenUsage().totalTokens() + " 개");
            }
            System.out.println();
        }

        System.out.println("종료합니다.");
    }
}