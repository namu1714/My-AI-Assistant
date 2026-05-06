package com.acme.assistant.rag;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class FixedSizeTextSplitterTest {

    @Test
    void 고정_크기로_분할한다() {
        var splitter = new FixedSizeTextSplitter(10);
        var doc = new Document("doc1", "Hello, World! This is a test.");

        List<TextChunk> chunks = splitter.split(doc);

        assertThat(chunks).hasSize(3);
        assertThat(chunks.get(0).content()).isEqualTo("Hello, Wor");
        assertThat(chunks.get(1).content()).isEqualTo("ld! This i");
        assertThat(chunks.get(2).content()).isEqualTo("s a test.");
    }

    @Test
    void 오버랩을_적용한다() {
        var splitter = new FixedSizeTextSplitter(10, 3);
        var doc = new Document("doc1", "ABCDEFGHIJKLMNOP");

        List<TextChunk> chunks = splitter.split(doc);

        assertThat(chunks.get(0).content()).isEqualTo("ABCDEFGHIJ");
        assertThat(chunks.get(1).content()).isEqualTo("HIJKLMNOP");
    }

    @Test
    void 청크_ID에_인덱스가_포함된다() {
        var splitter = new FixedSizeTextSplitter(5);
        var doc = new Document("my-doc", "Hello World");

        List<TextChunk> chunks = splitter.split(doc);

        assertThat(chunks.get(0).id()).isEqualTo("my-doc-chunk-0");
        assertThat(chunks.get(1).id()).isEqualTo("my-doc-chunk-1");
        assertThat(chunks.get(0).documentId()).isEqualTo("my-doc");
    }

    @Test
    void 메타데이터가_청크에_상속된다() {
        var splitter = new FixedSizeTextSplitter(100);

        var doc = new Document("doc1", "content",
                Map.of("source", "test.md"));

        List<TextChunk> chunks = splitter.split(doc);

        assertThat(chunks.getFirst().metadata()).containsEntry("source", "test.md");
    }

    @Test
    void chunkSize가_0이면_예외를_던진다() {
        assertThatThrownBy(() -> new FixedSizeTextSplitter(0))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
