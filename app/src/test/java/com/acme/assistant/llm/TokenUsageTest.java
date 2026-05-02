package com.acme.assistant.llm;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TokenUsageTest {

    @Test
    void twoArgConstructor_autoCalculatesTotal() {
        TokenUsage usage = new TokenUsage(100, 50);

        assertThat(usage.inputTokens()).isEqualTo(100);
        assertThat(usage.outputTokens()).isEqualTo(50);
        assertThat(usage.totalTokens()).isEqualTo(150);
    }
    @Test
    void empty_hasAllZeros() {
        assertThat(TokenUsage.EMPTY.inputTokens()).isZero();
        assertThat(TokenUsage.EMPTY.outputTokens()).isZero();
        assertThat(TokenUsage.EMPTY.totalTokens()).isZero();
    }

    @Test
    void add_combinesTwoUsages() {
        TokenUsage a = new TokenUsage(100, 50);
        TokenUsage b = new TokenUsage(200, 80);

        TokenUsage sum = a.add(b);

        assertThat(sum.inputTokens()).isEqualTo(300);
        assertThat(sum.outputTokens()).isEqualTo(130);
        assertThat(sum.totalTokens()).isEqualTo(430);
    }
}
