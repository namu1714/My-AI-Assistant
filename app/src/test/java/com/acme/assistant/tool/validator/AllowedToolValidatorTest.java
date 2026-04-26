package com.acme.assistant.tool.validator;

import com.acme.assistant.tool.ToolContext;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class AllowedToolValidatorTest {

    @Test
    void 허용된_도구는_통과한다() {
        AllowedToolValidator validator = new AllowedToolValidator("current_time", "file_read");

        assertThat(validator.isAllowed("current_time", ToolContext.empty())).isTrue();
        assertThat(validator.isAllowed("file_read", ToolContext.empty())).isTrue();
    }

    @Test
    void 허용되지_않은_도구는_거부한다() {
        AllowedToolValidator validator = new AllowedToolValidator("current_time");

        assertThat(validator.isAllowed("file_read", ToolContext.empty())).isFalse();
    }

}
