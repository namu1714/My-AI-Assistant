package com.acme.assistant.client;

import com.acme.assistant.exception.OpenAiException;
import com.acme.assistant.model.ChatRequest;
import com.acme.assistant.model.ChatResponse;
import com.acme.assistant.model.ErrorResponse;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.ThreadLocalRandom;

public class OpenAiClient {

    private static final String BASE_URL = "https://api.openai.com";
    private static final int MAX_RETRIES = 3;
    private static final int BASE_DELAY_MS = 1000;

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;

    public OpenAiClient(String apiKey) {
        this(apiKey, HttpClient.newHttpClient());
    }

    public OpenAiClient(String apiKey, HttpClient httpClient) {
        this.apiKey = apiKey;
        this.httpClient = httpClient;
        this.objectMapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
                .setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    public ChatResponse chat(ChatRequest chatRequest)
            throws IOException, InterruptedException {

        String json = objectMapper.writeValueAsString(chatRequest);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/v1/chat/completions"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        return sendWithRetry(request);
    }

    private ChatResponse sendWithRetry(HttpRequest request) throws IOException, InterruptedException {
        OpenAiException lastException = null;

        for (int attempt = 0; attempt <= MAX_RETRIES; attempt++) {
            if (attempt > 0) {
                long delay = calculateDelay(attempt);
                Thread.sleep(delay);
            }

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return objectMapper.readValue(response.body(), ChatResponse.class);
            }

            OpenAiException exception = createException(response.statusCode(), response.body());

            if (!exception.isRetryable()) {
                throw exception;
            }

            lastException = exception;
        }
        throw lastException;
    }

    private long calculateDelay(int attempt) {
        long delay = BASE_DELAY_MS * (1L << (attempt - 1));
        long jitter = ThreadLocalRandom.current().nextLong(delay / 2);
        return delay + jitter;
    }

    private OpenAiException createException(int statusCode, String body) {
        try {
            ErrorResponse errorResponse = objectMapper.readValue(body, ErrorResponse.class);
            ErrorResponse.Error error = errorResponse.error();
            return new OpenAiException(
                    statusCode,
                    error.message(),
                    error.type(),
                    error.code()
            );
        } catch (Exception e) {
            return new OpenAiException(statusCode, body);
        }
    }
}
