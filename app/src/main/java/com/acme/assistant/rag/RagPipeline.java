package com.acme.assistant.rag;

import com.acme.assistant.llm.ChatMessage;
import com.acme.assistant.llm.LlmModel;
import com.acme.assistant.llm.LlmResponse;
import com.acme.assistant.llm.client.LlmClient;
import com.acme.assistant.llm.embedding.EmbeddingClient;
import com.acme.assistant.llm.embedding.EmbeddingModel;
import com.acme.assistant.llm.embedding.EmbeddingResponse;

import java.util.ArrayList;
import java.util.List;

public class RagPipeline {

    private final EmbeddingClient embeddingClient;
    private final EmbeddingModel embeddingModel;
    private final LlmClient llmClient;
    private final LlmModel llmModel;
    private final TextSplitter textSplitter;
    private final VectorStore vectorStore;
    private final RagPromptBuilder promptBuilder;
    private final int topK;

    public RagPipeline(
            EmbeddingClient embeddingClient,
            EmbeddingModel embeddingModel,
            LlmClient llmClient,
            LlmModel llmModel,
            TextSplitter textSplitter,
            VectorStore vectorStore,
            RagPromptBuilder promptBuilder,
            int topK
    ) {
        this.embeddingClient = embeddingClient;
        this.embeddingModel = embeddingModel;
        this.llmClient = llmClient;
        this.llmModel = llmModel;
        this.textSplitter = textSplitter;
        this.vectorStore = vectorStore;
        this.promptBuilder = promptBuilder;
        this.topK = topK;
    }

    public void ingest(List<Document> documents) {
        for (Document document : documents) {
            List<TextChunk> chunks = textSplitter.split(document);
            if (chunks.isEmpty()) {
                continue;
            }

            List<String> texts = chunks.stream()
                    .map(TextChunk::content)
                    .toList();

            EmbeddingResponse response = embeddingClient.embed(embeddingModel, texts);

            vectorStore.add(chunks, response.embeddings());
        }
    }

    public String query(String question) {
        // 1. 질문을 벡터로 변환
        EmbeddingResponse queryEmbedding = embeddingClient.embed(embeddingModel, question);

        // 2. 유사한 청크 검색
        List<SearchResult> results = vectorStore.search(queryEmbedding.getFirst(), topK);

        if (results.isEmpty()){
            return "관련 문서를 찾을 수 없습니다.";
        }

        // 3. 프롬프트 구성
        ChatMessage systemMessage = promptBuilder.buildSystemMessage(results);

        List<ChatMessage> messages = new ArrayList<>();
        messages.add(systemMessage);
        messages.add(ChatMessage.ofUser(question));

        // 4. LLM 호출
        LlmResponse response = llmClient.chat(llmModel, messages);
        return response.content();
    }

    public int documentCount() {
        return vectorStore.size();
    }
}
