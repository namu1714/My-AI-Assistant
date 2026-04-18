package com.acme.assistant.exception;

public class OpenAiException extends RuntimeException {

    private final int statusCode;
    private final String errorType;
    private final String errorCode;

    public OpenAiException(int statusCode, String errorType, String errorCode, String message) {
        super(message);
        this.statusCode = statusCode;
        this.errorType = errorType;
        this.errorCode = errorCode;
    }

    public OpenAiException(int statusCode, String message) {
        this(statusCode, null, null, message);
    }

    public int statusCode() {
        return statusCode;
    }

    public String errorType() {
        return errorType;
    }

    public String errorCode() {
        return errorCode;
    }

    public boolean isRetryable() {
        return statusCode == 429 || statusCode >= 500;
    }
}
