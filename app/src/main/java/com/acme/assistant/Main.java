package com.acme.assistant;

import com.acme.assistant.client.OpenAiClient;
import com.acme.assistant.model.ChatRequest;
import com.acme.assistant.model.ChatResponse;
import com.acme.assistant.model.Message;
import com.fasterxml.jackson.databind.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

public class Main {

    public static void main(String[] args) throws Exception {
        String apiKey = System.getenv("OPENAI_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                    "OPENAI_API_KEY 환경 변수를 설정해야 합니다"
            );
        }

        OpenAiClient client = new OpenAiClient(apiKey);

        ChatRequest chatRequest = new ChatRequest(
                "gpt-4o-mini",
                List.of(
                        Message.ofSystem("당신은 친절한 AI 비서입니다."),
                        Message.ofUser("자기소개를 한 문장으로 해주세요.")
                )
        );

        ChatResponse chatResponse = client.chat(chatRequest);

        System.out.println("AI: " + chatResponse.content());
        System.out.println("Token Usage: " + chatResponse.usage().totalTokens());
    }
}