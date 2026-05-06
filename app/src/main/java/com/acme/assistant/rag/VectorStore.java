package com.acme.assistant.rag;

import java.util.List;

public interface VectorStore {

    void add(TextChunk chunk, float[] embedding);

    void add(List<TextChunk> chunks, List<float[]> embeddings);

    List<SearchResult> search(float[] queryEmbedding, int topK);

    void delete(String chunkId);

    int size();
}
