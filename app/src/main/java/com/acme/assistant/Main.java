package com.acme.assistant;

import com.acme.assistant.client.OpenAiClient;
import com.acme.assistant.model.ChatRequest;
import com.acme.assistant.model.ChatResponse;
import com.acme.assistant.model.ContentPart;
import com.acme.assistant.model.Message;
import com.acme.assistant.model.tool.ToolCall;
import com.acme.assistant.prompt.PromptManager;
import com.acme.assistant.tool.*;
import com.acme.assistant.ui.ConsoleChatBot;
import com.fasterxml.jackson.databind.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class Main {

    private static final String MODEL = "gpt-4o-mini";

    private final OpenAiClient client;
    private final ToolRegistry registry;

    public Main(OpenAiClient client, ToolRegistry registry) {
        this.client = client;
        this.registry = registry;
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

                ToolUseResult result = executeTool(toolUse);
                System.out.println("[도구 결과] " + result.content());

                messages.add(result.toMessage());
            }
        }
    }

    private ToolUseResult executeTool(ToolUse toolUse) {
        return registry.getTool(toolUse.name())
                .map(tool -> {
                    try {
                        String output = tool.execute(toolUse.arguments());
                        return ToolUseResult.success(toolUse.id(), output);
                    } catch (Exception e) {
                        return ToolUseResult.error(toolUse.id(), "오류: " + e.getMessage());
                    }
                })
                .orElse(ToolUseResult.error(toolUse.id(), "알 수 없는 도구: " + toolUse.name()));
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

        Main app = new Main(new OpenAiClient(apiKey), registry);

        System.out.println("=== 7 장 도구 호출 데모===\n");

        System.out.println("--- 질문: 지금 몇 시야? ---");
        String answer1 = app.chat(" 지금 몇 시야?");
        System.out.println("[최종 응답] " + answer1 + "\n");

        System.out.println("--- 질문: build.gradle 파일 내용을 알려줘 ---");
        String answer2 = app.chat("build.gradle 파일의 내용을 요약해줘");
        System.out.println("[최종 응답] " + answer2);
    }

    public void demoUrlImage() throws Exception {
        String imageUrl = "https://loremflickr.com/600/400";

        ChatRequest request = new ChatRequest(
                MODEL,
                List.of(
                        Message.ofSystem(" 당신은 이미지를 분석하는 AI 비서입니다."),
                        Message.ofUser(List.of(
                                new ContentPart.TextPart(
                                        " 이 이미지에 무엇이 보이나요? 간략하게 설명해주세요."
                                ),
                                ContentPart.ImagePart.ofUrl(imageUrl, "low")
                        ))
                )
        );

        ChatResponse response = client.chat(request);
        System.out.println("[응답] " + response.content());
        System.out.println("[토큰] " + response.usage());
    }

    public void runTemplatePrompt(OpenAiClient client) throws Exception {
        PromptManager manager = new PromptManager();

        String translatorPrompt = manager.render("translator", Map.of(
                "sourceLanguage", "한국어",
                "targetLanguage", "영어",
                "formal", true,
                "text", "안녕하세요, 만나서 반갑습니다."
        ));

        String reviewerPrompt = manager.render("code-reviewer", Map.of(
                "language", "java",
                "criteria", List.of(" 가독성", " 성능", " 보안"),
                "code", "String password = request.getParameter(\"pw\");"
        ));

        ChatRequest request = new ChatRequest(
                MODEL,
                List.of(
                        Message.ofUser(reviewerPrompt)
                )
        );
        ChatResponse response = client.chat(request);
        System.out.println("AI: " + response.content());
    }
}