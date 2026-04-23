package com.acme.assistant.ui;

import com.acme.assistant.client.OpenAiClient;
import com.acme.assistant.conversation.Conversation;
import com.acme.assistant.model.ChatRequest;
import com.acme.assistant.model.ChatResponse;
import com.acme.assistant.model.ContentPart;
import com.acme.assistant.util.ImageUtils;

import java.awt.*;
import java.nio.file.Path;
import java.util.List;
import java.util.Scanner;
import java.util.function.Consumer;

public class ConsoleChatBot {

    private static final String MODEL = "gpt-4o-mini";
    private static final String EXIT_COMMAND = "/quit";
    private static final String IMAGE_COMMAND = "/image";

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
        System.out.println("이미지를 분석하려면 /image <파일경로> 를 입력하세요.");
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
                if (input.equals(IMAGE_COMMAND) || input.startsWith(IMAGE_COMMAND + " ")) {
                    if (!handleImageCommand(input)) {
                        continue;
                    }
                } else {
                    conversation.addUserMessage(input);
                }

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

    private boolean handleImageCommand(String input) throws Exception {
        String args = input.substring(IMAGE_COMMAND.length()).trim();

        String imagePath;
        String prompt = "이 이미지에 무엇이 보이나요? 자세히 설명해주세요.";

        int spaceIndex = args.indexOf(' ');
        if (spaceIndex > 0) {
            imagePath = args.substring(0, spaceIndex).trim();
            prompt = args.substring(spaceIndex + 1).trim();
        } else {
            imagePath = args;
        }

        if (imagePath.isEmpty()) {
            System.out.println("사용법: /image <파일경로> [질문]");
            return false;
        }

        String dataUri = ImageUtils.toDataUri(Path.of(imagePath));

        conversation.addUserMessage(List.of(
                new ContentPart.TextPart(prompt),
                ContentPart.ImagePart.ofUrl(dataUri)
        ));
        return true;
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
