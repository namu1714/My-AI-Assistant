package com.acme.assistant.prompt;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class PromptTemplateTest {

    @Test
    void 단순_변수를_치환한다() {
        PromptTemplate template = new PromptTemplate(
                "{{language}}로 {{topic}} 에 대해 설명해줘."
        );

        String result = template.render(Map.of(
                "language", "한국어",
                "topic", "자바 레코드"
        ));

        assertThat(result).isEqualTo("한국어로 자바 레코드 에 대해 설명해줘.");
    }

    @Test
    void 리스트_섹션을_반복한다() {
        PromptTemplate template = new PromptTemplate("""
            다음 항목을 분석해줘:
            {{#items}}
            - {{.}}
            {{/items}}
        """);

        String result = template.render(Map.of(
                "items", List.of("성능", "보안", "유지보수성")
        ));

        assertThat(result).contains("- 성능");
        assertThat(result).contains("- 보안");
        assertThat(result).contains("- 유지보수성");
    }

    @Test
    void 조건부_섹션을_렌더링한다() {
        PromptTemplate template = new PromptTemplate(
                "{{#formal}} 존댓말로 답변하세요.{{/formal}}" +
                        "{{^formal}} 편하게 답변해.{{/formal}}"
        );

        String formal = template.render(Map.of("formal", true));
        assertThat(formal).isEqualTo(" 존댓말로 답변하세요.");

        String casual = template.render(Map.of("formal", false));
        assertThat(casual).isEqualTo(" 편하게 답변해.");
    }
}
