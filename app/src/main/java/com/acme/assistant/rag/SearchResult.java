package com.acme.assistant.rag;

public record SearchResult(
        TextChunk chunk,
        double score
) { }
