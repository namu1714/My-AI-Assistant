package com.acme.assistant.llm;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class RoleTest {

    @Test
    void value_returnsStringRepresentation() {
        assertThat(Role.SYSTEM.value()).isEqualTo("system");
        assertThat(Role.USER.value()).isEqualTo("user");
        assertThat(Role.ASSISTANT.value()).isEqualTo("assistant");
        assertThat(Role.TOOL.value()).isEqualTo("tool");
    }

    @Test
    void from_validValue_returnsRole() {
        assertThat(Role.from("system")).isEqualTo(Role.SYSTEM);
        assertThat(Role.from("user")).isEqualTo(Role.USER);
        assertThat(Role.from("assistant")).isEqualTo(Role.ASSISTANT);
        assertThat(Role.from("tool")).isEqualTo(Role.TOOL);
    }

    @Test
    void from_invalidValue_throwsException() {
        assertThatThrownBy(() -> Role.from("invalid"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("알 수 없는 역할");
    }
}
