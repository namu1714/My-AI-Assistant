package com.acme.assistant.memory;

import com.acme.assistant.llm.ChatMessage;

import java.util.List;

public final class TokenEstimator {

    private static final int MESSAGE_OVERHEAD = 4;

    private TokenEstimator() {}

    public static int estimate(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }

        return text.length() / 4;
    }

    public static int estimate(ChatMessage message) {
        int tokens = MESSAGE_OVERHEAD;
        if (message.content() != null) {
            tokens += estimate(message.content());
        }
        return tokens;
    }

    public static int estimate(List<ChatMessage> messages) {
        int total = 0;
        for (ChatMessage message : messages) {
            total += estimate(message);
        }
        return total;
    }
}
