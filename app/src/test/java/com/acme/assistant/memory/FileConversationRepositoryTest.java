package com.acme.assistant.memory;

import com.acme.assistant.llm.ChatMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class FileConversationRepositoryTest {

    @TempDir
    Path tempDir;

    @Test
    void 대화를_JSON_파일로_저장하고_로드한다() {
        var repo = new FileConversationRepository(tempDir);
        var messages = List.of(
                ChatMessage.ofUser("안녕하세요"),
                ChatMessage.ofAssistant("안녕하세요! 무엇을 도와드릴까요?")
        );
        repo.save("conv-1", messages);

        var loaded = repo.load("conv-1");

        assertThat(loaded).isPresent();
        assertThat(loaded.get()).hasSize(2);
        assertThat(loaded.get().getFirst().content()).isEqualTo("안녕하세요");
    }

    @Test
    void JSON_라운드트립이_정상_동작한다() {
        var repo = new FileConversationRepository(tempDir);
        var messages = List.of(
                ChatMessage.ofSystem("AI 비서입니다."),
                ChatMessage.ofUser("안녕하세요"),
                ChatMessage.ofAssistant("반갑습니다!")
        );

        repo.save("roundtrip", messages);

        var loaded = repo.load("roundtrip").orElseThrow();

        assertThat(loaded.get(0).role().value())
                .isEqualTo("system");
        assertThat(loaded.get(1).role().value())
                .isEqualTo("user");
        assertThat(loaded.get(2).role().value())
                .isEqualTo("assistant");
    }
}
