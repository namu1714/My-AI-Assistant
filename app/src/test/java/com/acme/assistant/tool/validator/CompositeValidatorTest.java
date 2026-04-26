package com.acme.assistant.tool.validator;

import com.acme.assistant.tool.ToolContext;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CompositeValidatorTest {

    @Test
    void 모든_검증기를_통과하면_허용된다() {
        AllowedToolValidator allow =
                new AllowedToolValidator("current_time", "file_read");
        DenyListValidator deny =
                new DenyListValidator("file_write");

        CompositeValidator composite = new CompositeValidator(allow, deny);

        assertThat(composite.isAllowed("current_time", ToolContext.empty())).isTrue();
    }

    @Test
    void 하나라도_거부하면_전체가_거부된다() {
        AllowedToolValidator allow =
                new AllowedToolValidator("current_time", "file_read", "file_write");
        DenyListValidator deny =
                new DenyListValidator("file_write");

        CompositeValidator composite = new CompositeValidator(allow, deny);

        assertThat(composite.isAllowed("file_write", ToolContext.empty())).isFalse();
    }
}
