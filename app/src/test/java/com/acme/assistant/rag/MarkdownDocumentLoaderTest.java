package com.acme.assistant.rag;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class MarkdownDocumentLoaderTest {

    @TempDir
    Path tempDir;

    @Test
    void 마크다운_파일을_로딩한다() throws IOException {
        Files.writeString(tempDir.resolve("guide.md"), "# Guide\nHello");
        Files.writeString(tempDir.resolve("faq.md"), "# FAQ\nQ&A");
        Files.writeString(tempDir.resolve("data.txt"), "ignored");

        var loader = new MarkdownDocumentLoader(tempDir);
        List<Document> docs = loader.load();

        assertThat(docs).hasSize(2);
        assertThat(docs.get(0).content()).contains("FAQ");
        assertThat(docs.get(1).content()).contains("Guide");
    }

    @Test
    void 문서_ID는_상대_경로이다() throws IOException {
        Files.writeString(tempDir.resolve("guide.md"), "content");

        var loader = new MarkdownDocumentLoader(tempDir);
        List<Document> docs = loader.load();

        assertThat(docs.getFirst().id()).isEqualTo("guide.md");
    }

    @Test
    void 빈_디렉터리는_빈_리스트를_반환한다() {
        var loader = new MarkdownDocumentLoader(tempDir);

        assertThat(loader.load()).isEmpty();
    }

    @Test
    void 메타데이터에_파일_정보가_포함된다() throws IOException {
        Files.writeString(tempDir.resolve("test.md"), "content");

        var loader = new MarkdownDocumentLoader(tempDir);
        Document doc = loader.load().getFirst();

        assertThat(doc.metadata()).containsKey("source");
        assertThat(doc.metadata()).containsEntry("filename", "test.md");
    }
}
