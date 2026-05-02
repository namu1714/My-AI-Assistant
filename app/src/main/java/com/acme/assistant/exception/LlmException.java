package com.acme.assistant.exception;

public class LlmException extends RuntimeException {

    private final String provider;

    public LlmException(String provider, String message) {
        super(message);
        this.provider = provider;
    }

    public LlmException(String provider, String message, Throwable cause) {
        super(message, cause);
        this.provider = provider;
    }

    public String provider() {
        return provider;
    }
}
