package com.acme.assistant.memory;

import com.acme.assistant.llm.ChatMessage;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class FileConversationRepository implements ConversationRepository {

    private final Path baseDir;
    private final ObjectMapper objectMapper;

    public FileConversationRepository(Path baseDir) {
        this.baseDir = baseDir;
        this.objectMapper = new ObjectMapper()
                .enable(SerializationFeature.INDENT_OUTPUT)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        try {
            Files.createDirectories(baseDir);
        } catch (IOException e) {
            throw new RuntimeException("대화 저장 디렉터리 생성 실패: " + baseDir, e);
        }
    }

    @Override
    public void save(String conversationId, List<ChatMessage> messages) {
        Path file = resolve(conversationId);
        try {
            objectMapper.writeValue(file.toFile(), messages);
        } catch (IOException e) {
            throw new RuntimeException("대화 저장 실패: " + conversationId, e);
        }
    }

    @Override
    public Optional<List<ChatMessage>> load(String conversationId) {
        Path file = resolve(conversationId);
        if (!Files.exists(file)) {
            return Optional.empty();
        }
        try {
            List<ChatMessage> messages = objectMapper.readValue(
                    file.toFile(),
                    new TypeReference<List<ChatMessage>>() {});
            return Optional.of(new ArrayList<>(messages));
        } catch (IOException e) {
            throw new RuntimeException("대화 로드 실패: " + conversationId, e);
        }
    }

    @Override
    public List<String> list() {
        try (Stream<Path> paths = Files.list(baseDir)) {
            return paths
                    .filter(p -> p.toString().endsWith(".json"))
                    .map(p -> p.getFileName().toString()
                            .replace(".json", ""))
                    .toList();
        } catch (IOException e) {
            throw new RuntimeException("대화 목록 조회 실패: ", e);
        }
    }

    @Override
    public void delete(String conversationId) {
        Path file = resolve(conversationId);
        try {
            Files.deleteIfExists(file);
        } catch (IOException e) {
            throw new RuntimeException("대화 삭제 실패: " + conversationId, e);
        }
    }

    private Path resolve(String conversationId) {
        return baseDir.resolve(conversationId + ".json");
    }
}
