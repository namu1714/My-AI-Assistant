package com.acme.assistant.ui;

import com.acme.assistant.client.OpenAiClient;
import com.acme.assistant.conversation.Conversation;
import com.acme.assistant.model.ChatRequest;
import com.acme.assistant.model.ChatResponse;

import java.util.Scanner;
import java.util.function.Consumer;

public class ConsoleChatBot {

    private static final String MODEL = "gpt-4o-mini";
    private static final String EXIT_COMMAND = "/quit";

    private final OpenAiClient client;
    private final Conversation conversation;
    private boolean streamingEnabled;

    public ConsoleChatBot(OpenAiClient client, String systemPrompt) {
        this.client = client;
        this.conversation = new Conversation(systemPrompt);
        this.streamingEnabled = false;
    }

    public void setStreamingEnabled(boolean streamingEnabled) {
        this.streamingEnabled = streamingEnabled;
    }

    public void start() {
        Scanner scanner = new Scanner(System.in);

        System.out.println("AI 비서와 대화를 시작합니다. 종료하려면 '" + EXIT_COMMAND + "'를 입력하세요.");
        System.out.println();

        while(true) {
            System.out.print("나: ");
            String input = scanner.nextLine().trim();

            if (input.isEmpty()) {
                continue;
            }

            if (EXIT_COMMAND.equalsIgnoreCase(input)) {
                System.out.println("대화를 종료합니다. 안녕히 가세요!");
                break;
            }

            try {
                conversation.addUserMessage(input);

                if (streamingEnabled) {
                    handleStreaming();
                } else {
                    handleBlocking();
                }

            } catch (Exception e) {
                System.err.println("오류가 발생했습니다: " + e.getMessage());
                System.out.println();
            }
        }
    }

    private void handleStreaming() throws Exception {
        ChatRequest request = new ChatRequest(
                MODEL, conversation.getMessages(), null, null, true, null
        );

        StringBuilder fullResponse = new StringBuilder();
        System.out.println("AI: ");

        Consumer<String> onToken = token -> {
            System.out.print(token);
            fullResponse.append(token);
        };

        client.chatStream(request, onToken);
        System.out.println();

        conversation.addAssistantMessage(fullResponse.toString());
    }

    private void handleBlocking() throws Exception {
        ChatRequest request = new ChatRequest(MODEL, conversation.getMessages());
        ChatResponse response = client.chat(request);

        String reply = response.content();
        conversation.addAssistantMessage(reply);

        System.out.println("AI: " + reply);
        System.out.println();
    }
}
