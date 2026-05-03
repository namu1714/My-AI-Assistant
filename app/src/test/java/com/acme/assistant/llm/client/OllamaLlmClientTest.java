package com.acme.assistant.llm.client;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class OllamaLlmClientTest {

    @Test
    void defaultConstructor_createsInstance() {
        OllamaLlmClient client = new OllamaLlmClient();
        assertThat(client).isNotNull();
    }
    @Test
    void implementsLlmClient() {
        OllamaLlmClient client = new OllamaLlmClient();
        assertThat(client).isInstanceOf(LlmClient.class);
    }
}
