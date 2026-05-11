package com.acme.assistant;

import com.acme.assistant.llm.*;
import com.acme.assistant.llm.client.LlmClient;
import com.acme.assistant.llm.client.LlmClientFactory;
import com.acme.assistant.memory.ConversationMemory;
import com.acme.assistant.memory.FileConversationRepository;
import com.acme.assistant.memory.PersistentMemory;
import com.acme.assistant.memory.TokenWindowMemory;
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
        TokenTracker tokenTracker = new TokenTracker();

        System.out.println("=== 13장 대화 메모리 AI 비서 ===");
        System.out.println("[LLM 제공자] " + provider.value());

        // 메모리 구성: 토큰 윈도우 + 파일 영속화
        ConversationMemory memory = new PersistentMemory(
                new TokenWindowMemory(4000),
                new FileConversationRepository(Path.of("conversations")),
                "default"
        );

        // 시스템 메시지 설정
        memory.setSystemMessage(ChatMessage.ofSystem(
                "당신은 친절한 AI 비서입니다. " +
                "이전 대화 맥락을 기억하며 답변합니다. "));

        System.out.println("[메모리] TokenWindowMemory(4000)" + " + FileConversationRepository");
        System.out.println("[저장된 메시지] " + memory.messageCount() + " 개\n");

        // 대화 루프
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("사용자 > ");
            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("quit") || input.equalsIgnoreCase("exit")) {
                break;
            }

            if (input.equalsIgnoreCase("clear")) {
                memory.clear();
                System.out.println("[메모리 초기화 완료]\n");
                continue;
            }

            if (input.isEmpty()) {
                continue;
            }

            // 사용자 메시지 추가
            memory.addMessage(ChatMessage.ofUser(input));

            // LLM 호출
            LlmResponse response = llmClient.chat(llmModel, memory.getMessages());

            // 응답 메시지 추가
            memory.addMessage(response.toAssistantMessage());

            System.out.println("\n[비서] " + response.content());
            System.out.println("[메시지 수] " + memory.messageCount() + " 개");
        }

        System.out.println("종료합니다.");
    }
}