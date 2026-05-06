package com.acme.assistant.rag;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class MarkdownDocumentLoader implements DocumentLoader {

    private final Path directory;

    public MarkdownDocumentLoader(Path directory) {
        this.directory = directory;
    }

    @Override
    public List<Document> load() {
        try (Stream<Path> paths = Files.walk(directory)) {
            return paths
                    .filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".md"))
                    .sorted()
                    .map(this::loadDocument)
                    .toList();
        } catch (IOException e) {
            throw new UncheckedIOException("문서 디렉터리를 읽을 수 없습니다: " + directory, e);
        }
    }

    private Document loadDocument(Path path) {
        try {
            String content = Files.readString(path);
            String id = directory.relativize(path).toString();
            Map<String, String> metadata = Map.of(
                    "source", path.toString(),
                    "filename", path.getFileName().toString()
            );
            return new Document(id, content, metadata);
        } catch (IOException e) {
            throw new UncheckedIOException("파일을 읽을 수 없습니다: " + path, e);
        }
    }
}
