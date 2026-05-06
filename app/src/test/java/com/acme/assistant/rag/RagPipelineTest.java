package com.acme.assistant.rag;

import com.acme.assistant.llm.LlmModel;
import com.acme.assistant.llm.TokenUsage;
import com.acme.assistant.llm.client.MockLlmClient;
import com.acme.assistant.llm.embedding.EmbeddingModel;
import com.acme.assistant.llm.embedding.EmbeddingResponse;
import com.acme.assistant.llm.embedding.MockEmbeddingClient;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class RagPipelineTest {

    @Test
    void ingest_후_query_관련_문서_기반_답변을_생성한다() {
        // Mock 설정
        var embeddingClient = new MockEmbeddingClient();
        var llmClient = new MockLlmClient();

        // 인덱싱용 임베딩 (문서 2개, 각 1 청크)
        embeddingClient.enqueue(new EmbeddingResponse(
                List.of(new float[]{1.0f, 0.0f, 0.0f}),
                TokenUsage.EMPTY));
        embeddingClient.enqueue(new EmbeddingResponse(
                List.of(new float[]{0.0f, 1.0f, 0.0f}),
                TokenUsage.EMPTY));

        // 쿼리용 임베딩 (질문 -> 첫 문서와 유사한 벡터)
        embeddingClient.enqueue(new EmbeddingResponse(
                List.of(new float[]{0.9f, 0.1f, 0.0f}),
                TokenUsage.EMPTY));

        // LLM 응답
        llmClient.enqueue("Gradle로 빌드합니다.");

        // 파이프라인 생성
        var pipeline = new RagPipeline(
                embeddingClient,
                new EmbeddingModel("test-model"),
                llmClient,
                new LlmModel("test-llm"),
                new FixedSizeTextSplitter(1000),
                new InMemoryVectorStore(),
                new RagPromptBuilder(),
                3
        );

        // 문서 인덱싱
        pipeline.ingest(List.of(
                new Document("build", "프로젝트는 Gradle로 빌드한다."),
                new Document("deploy", "Docker로 배포한다.")
        ));

        assertThat(pipeline.documentCount()).isEqualTo(2);

        // 질문
        String answer = pipeline.query("빌드 방법은?");

        assertThat(answer).isEqualTo("Gradle로 빌드합니다.");

        // LLM 에 전달된 메시지 검증
        var messages = llmClient.receivedMessages().getFirst();
        assertThat(messages.get(0).content())
                .contains("Gradle로 빌드한다."); // 검색된 문서 포함
        assertThat(messages.get(1).content())
                .isEqualTo("빌드 방법은?"); // 원래 질문
    }

    @Test
    void 빈_저장소에_query하면_기본_메시지를_반환한다() {
        var embeddingClient = new MockEmbeddingClient();
        embeddingClient.enqueue(new EmbeddingResponse(
                List.of(new float[]{1.0f, 0.0f}),
                TokenUsage.EMPTY));

        var pipeline = new RagPipeline(
                embeddingClient,
                new EmbeddingModel("test-model"),
                new MockLlmClient(),
                new LlmModel("test-llm"),
                new FixedSizeTextSplitter(1000),
                new InMemoryVectorStore(),
                new RagPromptBuilder(),
                3
        );

        String answer = pipeline.query("질문");

        assertThat(answer).isEqualTo("관련 문서를 찾을 수 없습니다.");
    }
}
