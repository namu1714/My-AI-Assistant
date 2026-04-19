package com.acme.assistant.model;

import java.util.List;

public record ChatStreamResponse(
        String id,
        String model,
        List<StreamChoice> choices
) {
    public record StreamChoice(
            int index,
            Delta delta,
            String finishReason
    ) { }

    public record Delta(
        String role,
        String content
    ) { }
}
