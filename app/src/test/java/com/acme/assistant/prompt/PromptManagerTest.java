package com.acme.assistant.prompt;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class PromptManagerTest {

    @Test
    void 템플릿_파일을_로드하고_렌더링한다() {
        PromptManager manager = new PromptManager();

        String result = manager.render("translator", Map.of(
                "sourceLanguage", "한국어",
                "targetLanguage", "영어",
                "formal", true,
                "text", "안녕하세요."
        ));

        assertThat(result).contains("전문 번역가");
        assertThat(result).contains("한국어");
        assertThat(result).contains("영어");
        assertThat(result).contains("격식체");
        assertThat(result).contains("안녕하세요");
    }

    @Test
    void 동일_템플릿을_캐시에서_반환한다() {
        PromptManager manager = new PromptManager();

        PromptTemplate first = manager.getTemplate("translator");
        PromptTemplate second = manager.getTemplate("translator");

        assertThat(first).isSameAs(second);
    }

    @Test
    void 존재하지_않는_템플릿에_예외를_던진다() {
        PromptManager manager = new PromptManager();

        assertThatThrownBy(() -> manager.getTemplate("nonexistent"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("템플릿을 찾을 수 없습니다");
    }
}
