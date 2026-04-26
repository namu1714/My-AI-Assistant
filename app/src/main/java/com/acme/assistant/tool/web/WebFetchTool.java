package com.acme.assistant.tool.implementation.web;

import com.acme.assistant.tool.*;
import com.acme.assistant.tool.validator.UrlValidator;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class WebFetchTool extends AbstractTool {

    private static final int DEFAULT_TIMEOUT = 10;
    private static final int MAX_RESPONSE_LENGTH = 10_000;

    private final HttpClient httpClient;

    private final UrlValidator urlValidator;

    public WebFetchTool(HttpClient httpClient, UrlValidator urlValidator) {
        super(new ToolDefinition(
                "web_fetch",
                "URL의 내용을 가져온다. "
                        + "SSRF 방지를 위해 사설 네트워크 접근이 차단된다.",
                JsonSchemaBuilder.objectSchema()
                        .property("url", "string",
                                "가져올 URL (http/https)")
                        .property("timeout", "integer",
                                "타임아웃 (초, 기본값 10)")
                        .property("max_length", "integer",
                                "응답 최대 길이 (문자, 기본값 10000)")
                        .required("url")
                        .build()
        ));
        this.httpClient = httpClient;
        this.urlValidator = urlValidator;
    }

    @Override
    public ToolResult execute(ToolInput input, ToolContext context) {
        try {
            String urlStr = input.requireString("url");

            int timeout = input.optionalInt("timeout", DEFAULT_TIMEOUT);
            int maxLength = input.optionalInt("max_length", MAX_RESPONSE_LENGTH);

            URI validatedUri = urlValidator.validate(urlStr);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(validatedUri)
                    .timeout(Duration.ofSeconds(timeout))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request, HttpResponse.BodyHandlers.ofString());

            int statusCode = response.statusCode();
            String body = response.body();

            if (statusCode >= 400) {
                return ToolResult.error("HTTP 오류" + statusCode + ": " + truncate(body, maxLength));
            }
            return ToolResult.success(truncate(body, maxLength));
        } catch (SecurityException e) {
            return ToolResult.error("URI 접근 거부: " + e.getMessage());
        } catch (Exception e) {
            return ToolResult.error("웹 페이지 가져오기 오류: " + e.getMessage());
        }
    }

    private String truncate(String text, int maxLength) {
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength)
                + "\n... (응답이 " + maxLength + "자로 제한됨)";
    }
}
