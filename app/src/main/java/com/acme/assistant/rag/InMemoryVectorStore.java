package com.acme.assistant.rag;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class InMemoryVectorStore implements VectorStore {

    private final List<VectorEntry> entries = new ArrayList<>();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    record VectorEntry(TextChunk chunk, float[] embedding) {}

    @Override
    public void add(TextChunk chunk, float[] embedding) {
        lock.writeLock().lock();
        try {
            entries.add(new VectorEntry(chunk, embedding));
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void add(List<TextChunk> chunks, List<float[]> embeddings) {
        if (chunks.size() != embeddings.size()) {
            throw new IllegalArgumentException(
                    "chunks와 embeddings의 크기가 다릅니다: " +
                            chunks.size() + " != " + embeddings.size());
        }
        lock.writeLock().lock();
        try {
            for (int i = 0; i < chunks.size(); i++) {
                entries.add(
                        new VectorEntry(chunks.get(i), embeddings.get(i)));
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public List<SearchResult> search(float[] queryEmbedding, int topK) {
        lock.readLock().lock();
        try {
            return entries.stream()
                    .map(entry -> new SearchResult(
                            entry.chunk,
                            CosineSimilarity.calculate(queryEmbedding, entry.embedding)))
                    .sorted(Comparator.comparingDouble(SearchResult::score).reversed())
                    .limit(topK)
                    .toList();
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public void delete(String chunkId) {
        lock.writeLock().lock();
        try {
            entries.removeIf(entry ->
                    entry.chunk.id().equals(chunkId));
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public int size() {
        lock.readLock().lock();
        try {
            return entries.size();
        } finally {
            lock.readLock().unlock();
        }
    }
}
