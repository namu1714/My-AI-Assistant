package com.acme.assistant.llm;

public class TokenTracker {

    private TokenUsage cumulative = TokenUsage.EMPTY;
    private int callCount = 0;

    public synchronized void add(TokenUsage usage) {
        cumulative = cumulative.add(usage);
        callCount++;
    }

    public synchronized TokenUsage cumulative() {
        return cumulative;
    }

    public synchronized int callCount() {
        return callCount;
    }

    public synchronized void reset() {
        cumulative = TokenUsage.EMPTY;
        callCount = 0;
    }

    public synchronized String summary() {
        return String.format(
                "총 %d회 호출 | 입력: %,d 토큰 | 출력: %,d 토큰 | 합계: %,d 토큰",
                callCount,
                cumulative.inputTokens(),
                cumulative.outputTokens(),
                cumulative.totalTokens()
        );
    }
}
