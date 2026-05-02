package com.acme.assistant.llm;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TokenTrackerTest {

    @Test
    void initialState_isEmpty() {
        TokenTracker tracker = new TokenTracker();

        assertThat(tracker.cumulative()).isEqualTo(TokenUsage.EMPTY);
        assertThat(tracker.callCount()).isZero();
    }

    @Test
    void add_accumulatesUsage() {
        TokenTracker tracker = new TokenTracker();

        tracker.add(new TokenUsage(100, 50));
        tracker.add(new TokenUsage(200, 80));

        assertThat(tracker.cumulative().inputTokens()).isEqualTo(300);
        assertThat(tracker.cumulative().outputTokens()).isEqualTo(130);
        assertThat(tracker.cumulative().totalTokens()).isEqualTo(430);
        assertThat(tracker.callCount()).isEqualTo(2);
    }

    @Test
    void reset_clearsAll() {
        TokenTracker tracker = new TokenTracker();
        tracker.add(new TokenUsage(100, 50));

        tracker.reset();

        assertThat(tracker.cumulative()).isEqualTo(TokenUsage.EMPTY);
        assertThat(tracker.callCount()).isZero();
    }

    @Test
    void summary_formatsCorrectly() {
        TokenTracker tracker = new TokenTracker();

        tracker.add(new TokenUsage(1000, 500));
        tracker.add(new TokenUsage(2000, 800));

        String summary = tracker.summary();

        System.out.println(summary); // For visual verification
        assertThat(summary).contains("2회 호출");
        assertThat(summary).contains("3,000 토큰");
        assertThat(summary).contains("1,300 토큰");
        assertThat(summary).contains("4,300 토큰");
    }
}
