package com.acme.assistant;

import com.acme.assistant.client.OpenAiClient;
import com.acme.assistant.ui.ConsoleChatBot;
import com.fasterxml.jackson.databind.*;


public class Main {

    public static void main(String[] args) throws Exception {
        String apiKey = System.getenv("OPENAI_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            System.err.println("OPENAI_API_KEY 환경 변수를 설정해야 합니다.");
            return;
        }

        OpenAiClient client = new OpenAiClient(apiKey);
        ConsoleChatBot chatBot = new ConsoleChatBot(
                client,
                "당신은 친절한 AI 비서입니다. 간결하게 답변하세요."
        );
        chatBot.setStreamingEnabled(true);
        chatBot.start();
    }
}