package com.acme.assistant;

import com.acme.assistant.client.OpenAiClient;
import com.acme.assistant.llm.*;
import com.acme.assistant.llm.client.LlmClient;
import com.acme.assistant.llm.client.LlmClientFactory;
import com.acme.assistant.llm.embedding.EmbeddingModel;
import com.acme.assistant.llm.embedding.OpenAiEmbeddingClient;
import com.acme.assistant.rag.*;
import com.acme.assistant.tool.*;
import com.acme.assistant.tool.file.*;

import java.nio.file.Path;
import java.util.List;
import java.util.Scanner;


public class Main {

    public static void main(String[] args) throws Exception {
        String providerName = System.getenv("LLM_PROVIDER");
        if (providerName == null) {
            providerName = "openai";
        }
        LlmProvider provider = LlmProvider.from(providerName);

        // LlmClient 생성
        LlmClient llmClient = LlmClientFactory.fromEnvironment();
        LlmModel llmModel = new LlmModel(
                LlmClientFactory.defaultModel(provider));
        TokenTracker tokenTracker = new TokenTracker();

        System.out.println("=== 12 RAG AI 비서 ===");
        System.out.println("[LLM 제공자] " + provider.value() + "\n");

        // EmbeddingClient 생성 (OpenAI 임베딩 사용)
        String apiKey = System.getenv("OPENAI_API_KEY");
        var pipeline = getRagPipeline(apiKey, llmClient, llmModel);

        // 문서 인덱싱
        Path docsDir = Path.of("docs");
        System.out.println("\n[문서 인덱싱] " + docsDir);

        var loader = new MarkdownDocumentLoader(docsDir);
        List<Document> documents = loader.load();
        System.out.println("[로드된 문서] " + documents.size() + " 개");

        pipeline.ingest(documents);
        System.out.println("[인덱싱 완료] "
                + pipeline.documentCount() + " 개 청크\n");

        // 대화 루프
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("질문 > ");
            String question = scanner.nextLine().trim();

            if (question.equalsIgnoreCase("quit") || question.equalsIgnoreCase("exit")) {
                break;
            }

            if (question.isEmpty()) {
                continue;
            }

            String answer = pipeline.query(question);
            System.out.println("\n[답변] " + answer + "\n");
        }

        System.out.println("종료합니다.");
    }

    private static RagPipeline getRagPipeline(String apiKey, LlmClient llmClient, LlmModel llmModel) {
        OpenAiClient openAiClient = new OpenAiClient(apiKey);
        var embeddingClient = new OpenAiEmbeddingClient(openAiClient);
        var embeddingModel = new EmbeddingModel("text-embedding-3-small");

        // RAG 파이프라인 구성
        var pipeline = new RagPipeline(
                embeddingClient,
                embeddingModel,
                llmClient,
                llmModel,
                new FixedSizeTextSplitter(500, 50),
                new InMemoryVectorStore(),
                new RagPromptBuilder(),
                3
        );
        return pipeline;
    }
}