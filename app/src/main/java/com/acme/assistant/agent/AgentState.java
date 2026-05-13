package com.acme.assistant.agent;

public enum AgentState {

    IDLE("대기"),
    THINKING("추론 중"),
    ACTING("도구 실행 중"),
    FINISHED("완료"),
    ERROR("오류");

    private final String description;

    AgentState(String description) {
        this.description = description;
    }

    public String description() {
        return description;
    }
}
