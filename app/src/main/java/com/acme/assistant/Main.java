package com.acme.assistant;

import com.acme.assistant.client.OpenAiClient;
import com.acme.assistant.model.ChatRequest;
import com.acme.assistant.model.ChatResponse;
import com.acme.assistant.model.Message;
import com.acme.assistant.model.tool.ToolCall;
import com.acme.assistant.tool.*;
import com.acme.assistant.tool.implementation.CurrentTimeTool;
import com.acme.assistant.tool.implementation.FileReadTool;
import com.acme.assistant.tool.implementation.FileWriteTool;
import com.acme.assistant.tool.validator.AllowedToolValidator;

import java.util.ArrayList;
import java.util.List;


public class Main {

    private static final String MODEL = "gpt-4o-mini";

    private final OpenAiClient client;
    private final ToolRegistry registry;
    private final ToolExecutionManager executionManager;

    public Main(
            OpenAiClient client,
            ToolRegistry registry,
            ToolExecutionManager executionManager
    ) {
        this.client = client;
        this.registry = registry;
        this.executionManager = executionManager;
    }

    public static void main(String[] args) throws Exception {
        String apiKey = System.getenv("OPENAI_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            System.err.println("OPENAI_API_KEY 환경 변수를 설정해야 합니다.");
            return;
        }

        ToolRegistry registry = new ToolRegistry();
        registry.register(new CurrentTimeTool());
        registry.register(new FileReadTool());
        registry.register(new FileWriteTool());

        AllowedToolValidator validator = new AllowedToolValidator("current_time", "file_read");

        ToolExecutionManager executionManager = new ToolExecutionManager(registry, validator);

        Main app = new Main(new OpenAiClient(apiKey), registry, executionManager);

        System.out.println("=== 8장 권한 기반 도구 실행 데모===\n");

        System.out.println("--- 질문: 지금 몇 시야? ---");
        String answer1 = app.chat(" 지금 몇 시야?");
        System.out.println("[최종 응답] " + answer1 + "\n");

        System.out.println("--- 질문: build.gradle 파일 내용을 알려줘 ---");
        String answer2 = app.chat("build.gradle 파일의 내용을 요약해줘. 경로는 --- 이야.");
        System.out.println("[최종 응답] " + answer2);

        System.out.println("--- 질문: /tmp/test.txt 에 hello를 써줘 ---");
        String answer3 = app.chat("/tmp/test.txt 에 hello를 써줘");
        System.out.println("[최종 응답] " + answer3);
    }

    public String chat(String userMessage) throws Exception {
        List<Message> messages = new ArrayList<>();
        messages.add(Message.ofSystem("당신은 도구를 사용할 수 있는 AI 비서입니다."));
        messages.add(Message.ofUser(userMessage));

        while (true) {
            ChatRequest request = new ChatRequest(
                    MODEL, messages, null, null, null, null,
                    registry.toFunctionTools(), "auto"
            );
            ChatResponse response = client.chat(request);
            var choice = response.choices().getFirst();

            if (choice.message().toolCalls() == null || choice.message().toolCalls().isEmpty()) {
                return response.content();
            }

            messages.add(choice.message());

            for (ToolCall toolCall : choice.message().toolCalls()) {
                ToolUse toolUse = ToolUse.from(toolCall);
                System.out.println("[도구 호출] " + toolUse.name() + " - " + toolUse.arguments());

                ToolUseResult result = executionManager.execute(toolUse, ToolContext.empty());

                if (result.isError()) {
                    System.out.println("[권한 거부] " + result.content());
                } else {
                    System.out.println("[도구 결과] " + result.content());
                }

                messages.add(result.toMessage());
            }
        }
    }
}