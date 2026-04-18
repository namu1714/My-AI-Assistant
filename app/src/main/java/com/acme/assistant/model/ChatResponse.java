package com.acme.assistant.model;

import java.util.LinkedList;

public record ChatResponse(
        String id,
        String model,
        LinkedList<Choice> choices,
        Usage usage
) {
    public String content() {
        if (choices == null || choices.isEmpty()) {
            return "";
        }
        return choices.get(0).message().content();
    }
}
