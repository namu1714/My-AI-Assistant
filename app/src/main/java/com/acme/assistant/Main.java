package com.acme.assistant;

import com.acme.assistant.client.OpenAiClient;
import com.acme.assistant.model.ChatRequest;
import com.acme.assistant.model.ChatResponse;
import com.acme.assistant.model.ContentPart;
import com.acme.assistant.model.Message;
import com.acme.assistant.prompt.PromptManager;
import com.acme.assistant.ui.ConsoleChatBot;
import com.fasterxml.jackson.databind.*;

import java.util.List;
import java.util.Map;


public class Main {

    private static final String MODEL = "gpt-4o-mini";

    public static void main(String[] args) throws Exception {
        String apiKey = System.getenv("OPENAI_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            System.err.println("OPENAI_API_KEY 환경 변수를 설정해야 합니다.");
            return;
        }

        OpenAiClient client = new OpenAiClient(apiKey);

        runChatBot(client);
        // runTemplatePrompt(client);
        // demoUrlImage(client);
    }

    private static void demoUrlImage(OpenAiClient client) throws Exception {
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

    private static void runTemplatePrompt(OpenAiClient client) throws Exception {
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

    private static void runChatBot(OpenAiClient client) {
        ConsoleChatBot chatBot = new ConsoleChatBot(
                client,
                "당신은 친절한 AI 비서입니다. 간결하게 답변하세요."
        );
        chatBot.setStreamingEnabled(true);
        chatBot.start();
    }
}