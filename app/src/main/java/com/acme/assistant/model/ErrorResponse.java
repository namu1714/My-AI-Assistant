package com.acme.assistant.model;

public record ErrorResponse(
        Error error
) {
    public record Error(
            String message,
            String type,
            String param,
            String code
    ) {}
}
