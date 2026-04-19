package com.acme.assistant.model;

import java.util.List;

public record ChatResponse(
        String id,
        String model,
        List<Choice> choices,
        Usage usage
) {
    public String content() {
        if (choices == null || choices.isEmpty()) {
            return "";
        }
        return choices.getFirst().message().content();
    }
}
