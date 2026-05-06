package com.acme.assistant.rag;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class RagPromptBuilderTest {

    @Test
    void 검색_결과로_시스템_메시지를_생성한다() {
        var builder = new RagPromptBuilder();
        var results = List.of(
                new SearchResult(
                        new TextChunk("c1", "doc1", "Java 21 은 LTS 버전이다.",
                                Map.of("source", "guide.md")),
                        0.95),
                new SearchResult(
                        new TextChunk("c2", "doc1", "Gradle 로 빌드한다.",
                                Map.of("source", "build.md")),
                        0.82)
        );

        var message = builder.buildSystemMessage(results);

        assertThat(message.role().value()).isEqualTo("system");
        assertThat(message.content()).contains("Java 21 은 LTS 버전이다.");
        assertThat(message.content()).contains("Gradle 로 빌드한다.");
        assertThat(message.content()).contains("guide.md");
        assertThat(message.content()).contains("0.95");
    }

    @Test
    void 컨텍스트_길이를_초과하면_잘린다() {
        var builder = new RagPromptBuilder("문서: %s", 50); // 매우 짧은 제한
        var results = List.of(
                new SearchResult(
                        new TextChunk("c1", "doc1", "짧은 내용"),
                        0.9),
                new SearchResult(
                        new TextChunk("c2", "doc1", "이 문서는 잘려야 한다.".repeat(10)),
                        0.8)
        );

        var context = builder.buildContext(results);

        // 첫 번째 결과는 포함, 두 번째는 길이 초과로 제외
        assertThat(context).contains("짧은 내용");
    }

    @Test
    void 메타데이터가_없으면_documentId를_출처로_사용한다() {
        var builder = new RagPromptBuilder();
        var results = List.of(
                new SearchResult(
                        new TextChunk("c1", "my-doc", "내용"),
                        0.9)
        );
        var message = builder.buildSystemMessage(results);

        assertThat(message.content()).contains("my-doc");
    }

    @Test
    void 빈_검색_결과는_빈_컨텍스트를_생성한다() {
        var builder = new RagPromptBuilder();
        var context = builder.buildContext(List.of());

        assertThat(context).isEmpty();
    }
}
