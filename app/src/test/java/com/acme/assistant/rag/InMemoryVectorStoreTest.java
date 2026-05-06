package com.acme.assistant.rag;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class InMemoryVectorStoreTest {

    @Test
    void 청크를_저장하고_크기를_확인한다() {
        var store = new InMemoryVectorStore();
        var chunk = new TextChunk("c1", "doc1", "content");

        store.add(chunk, new float[]{0.1f, 0.2f});

        assertThat(store.size()).isEqualTo(1);
    }

    @Test
    void 배치로_저장한다() {
        var store = new InMemoryVectorStore();
        var chunks = List.of(
                new TextChunk("c1", "doc1", "hello"),
                new TextChunk("c2", "doc1", "world")
        );
        var embeddings = List.of(
                new float[]{0.1f, 0.2f},
                new float[]{0.3f, 0.4f}
        );

        store.add(chunks, embeddings);

        assertThat(store.size()).isEqualTo(2);
    }

    @Test
    void 배치_저장_시_크기_불일치는_예외를_던진다() {
        var store = new InMemoryVectorStore();
        var chunks = List.of(
                new TextChunk("c1", "doc1", "hello"));
        var embeddings = List.of(
                new float[]{0.1f}, new float[]{0.2f});

        assertThatThrownBy(() -> store.add(chunks, embeddings))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 청크를_삭제한다() {
        var store = new InMemoryVectorStore();

        store.add(new TextChunk("c1", "doc1", "hello"),
                new float[]{0.1f});
        store.add(new TextChunk("c2", "doc1", "world"),
                new float[]{0.2f});
        store.delete("c1");

        assertThat(store.size()).isEqualTo(1);
    }

    @Test
    void 유사도_검색으로_가장_유사한_청크를_반환한다() {
        var store = new InMemoryVectorStore();
        store.add(new TextChunk("c1", "doc1",
                        " 고양이"),
                new float[]{1.0f, 0.0f, 0.0f});
        store.add(new TextChunk("c2", "doc1",
                        " 강아지"),
                new float[]{0.9f, 0.1f, 0.0f});
        store.add(new TextChunk("c3", "doc1",
                        " 주식"),
                new float[]{0.0f, 0.0f, 1.0f});

        List<SearchResult> results =
                store.search(new float[]{1.0f, 0.0f, 0.0f}, 2);

        assertThat(results).hasSize(2);
        assertThat(results.get(0).chunk().content()).isEqualTo(" 고양이");
        assertThat(results.get(0).score()).isCloseTo(1.0,
                org.assertj.core.data.Offset.offset(0.01));
    }
}
