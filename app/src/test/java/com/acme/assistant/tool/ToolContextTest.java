package com.acme.assistant.tool;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class ToolContextTest {

    @Test
    void 빈_컨텍스트를_생성한다() {
        ToolContext context = ToolContext.empty();

        assertThat(context.conversationId()).isNull();
        assertThat(context.userId()).isNull();
        assertThat(context.metadata()).isEmpty();
    }

    @Test
    void 메타데이터를_포함하여_생성한다() {
        ToolContext context = new ToolContext(
                "conv-1", "user-1",
                Map.of("env", "production", "version", "1.0")
        );
        assertThat(context.getMetadata("env")).contains("production");
        assertThat(context.getMetadata("missing")).isEmpty();
    }
}
