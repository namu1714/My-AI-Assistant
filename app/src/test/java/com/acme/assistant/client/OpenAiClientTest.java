package com.acme.assistant.client;

import com.acme.assistant.exception.OpenAiException;
import com.acme.assistant.model.ChatRequest;
import com.acme.assistant.model.ChatResponse;
import com.acme.assistant.model.Message;
import org.junit.jupiter.api.Test;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSession;
import java.io.IOException;
import java.net.Authenticator;
import java.net.CookieHandler;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class OpenAiClientTest {

    private static final String SUCCESS_RESPONSE = """
            {
                "id": "chatcmpl-test",
                "model": "gpt-4o-mini",
                "choices": [
                    {
                        "index": 0,
                        "message": {"role": "assistant", "content": " 테스트 응답입니다."},
                        "finish_reason": "stop"
                    }
                ],
                "usage": {"prompt_tokens": 10, "completion_tokens": 8, "total_tokens": 18}
            }
            """;
    private static final String ERROR_RESPONSE = """
            {
                "error": {
                    "message": "Rate limit exceeded. Please try again later.",
                    "type": "rate_limit_error",
                    "param": null,
                    "code": "rate_limit_exceeded"
                }
            }
            """;

    @Test
    void 정상_응답을_ChatResponse로_반환한다() throws Exception {
        HttpClient stubClient = createStupHttpClient(200, SUCCESS_RESPONSE);
        OpenAiClient client = new OpenAiClient("test-key", stubClient);

        ChatRequest request = new ChatRequest(
                "gpt-4o-mini",
                List.of(Message.ofUser("테스트"))
        );
        ChatResponse response = client.chat(request);

        assertThat(response.content()).isEqualTo(" 테스트 응답입니다.");
        assertThat(response.usage().totalTokens()).isEqualTo(18);
    }

    @Test
    void 오류_상태_코드에_예외를_던진다() {
        HttpClient stubClient = createStupHttpClient(429, ERROR_RESPONSE);
        OpenAiClient client = new OpenAiClient("test-key", stubClient);

        ChatRequest request = new ChatRequest(
                "gpt-4o-mini",
                List.of(Message.ofUser("테스트"))
        );

        assertThatThrownBy(() -> client.chat(request))
                .isInstanceOf(OpenAiException.class)
                .hasMessageContaining("rate_limit_exceeded");
    }

    @Test
    void 스트리밍_응답을_토큰_단위로_전달한다() throws Exception {
        String sseResponse = """
                data: {"id":"chatcmpl-test","model":"gpt-4o-mini","choices":[{"index":0,"delta":{"role":"assistant","content":""}}]}
                data: {"id":"chatcmpl-test","model":"gpt-4o-mini","choices":[{"index":0,"delta":{"content":" 안녕"}}]}
                data: {"id":"chatcmpl-test","model":"gpt-4o-mini","choices":[{"index":0,"delta":{"content":" 하세요"}}]}
                data: {"id":"chatcmpl-test","model":"gpt-4o-mini","choices":[{"index":0,"delta":{"content":"!"},"finish_reason": "stop"}]}
                data: [DONE]
                """;

        HttpClient stubClient = createStupHttpClient(200, sseResponse);
        OpenAiClient client = new OpenAiClient("test-key", stubClient);

        ChatRequest request = new ChatRequest(
                "gpt-4o-mini",
                List.of(Message.ofUser("테스트")),
                null, null, true, null
        );

        List<String> tokens = new ArrayList<>();
        client.chatStream(request, tokens::add);

        assertThat(tokens).containsExactly(" 안녕", " 하세요", "!");
    }

    private HttpClient createStupHttpClient(int statusCode, String body) {
        return new HttpClient() {
            @Override
            public <T> HttpResponse<T> send(
                    HttpRequest request,
                    HttpResponse.BodyHandler<T> responseBodyHandler
            ) throws IOException, InterruptedException {
                return createStupResponse(statusCode, body, request);
            }

            // 나머지 추상 메서드는 기본 구현 제공
            @Override
            public Optional<CookieHandler> cookieHandler() {
                return Optional.empty();
            }

            @Override
            public Optional<Duration> connectTimeout() {
                return Optional.empty();
            }

            @Override
            public Redirect followRedirects() {
                return null;
            }

            @Override
            public Optional<ProxySelector> proxy() {
                return Optional.empty();
            }

            @Override
            public SSLContext sslContext() {
                return null;
            }

            @Override
            public SSLParameters sslParameters() {
                return null;
            }

            @Override
            public Optional<Authenticator> authenticator() {
                return Optional.empty();
            }

            @Override
            public Version version() {
                return null;
            }

            @Override
            public Optional<Executor> executor() {
                return Optional.empty();
            }

            @Override
            public <T> CompletableFuture<HttpResponse<T>> sendAsync(HttpRequest request, HttpResponse.BodyHandler<T> responseBodyHandler) {
                return null;
            }

            @Override
            public <T> CompletableFuture<HttpResponse<T>> sendAsync(HttpRequest request, HttpResponse.BodyHandler<T> responseBodyHandler, HttpResponse.PushPromiseHandler<T> pushPromiseHandler) {
                return null;
            }
        };
    }

    @SuppressWarnings("unchecked")
    private <T> HttpResponse<T> createStupResponse(int statusCode, String body, HttpRequest request) {
        return new HttpResponse<T>() {
            @Override
            public int statusCode() {
                return statusCode;
            }

            @Override
            public HttpRequest request() {
                return request;
            }

            @Override
            public Optional<HttpResponse<T>> previousResponse() {
                return Optional.empty();
            }

            @Override
            public HttpHeaders headers() {
                return HttpHeaders.of(java.util.Map.of(), (a, b) -> true);
            }

            @Override
            public T body() {
                return (T) body;
            }

            @Override
            public Optional<SSLSession> sslSession() {
                return Optional.empty();
            }

            @Override
            public URI uri() {
                return request.uri();
            }

            @Override
            public HttpClient.Version version() {
                return HttpClient.Version.HTTP_2;
            }
        };
    }
}
