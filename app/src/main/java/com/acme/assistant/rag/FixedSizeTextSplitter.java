package com.acme.assistant.rag;

import java.util.ArrayList;
import java.util.List;

public class FixedSizeTextSplitter implements TextSplitter {

    private final int chunkSize;
    private final int overlap;

    public FixedSizeTextSplitter(int chunkSize, int overlap) {
        if (chunkSize <= 0) {
            throw new IllegalArgumentException("chunkSize는 양수여야 합니다: " + chunkSize);
        }
        if (overlap < 0 || overlap >= chunkSize) {
            throw new IllegalArgumentException(
                    "overlap은 0 이상, chunkSize 미만이어야 합니다: " + overlap);
        }
        this.chunkSize = chunkSize;
        this.overlap = overlap;
    }

    public FixedSizeTextSplitter(int chunkSize) {
        this(chunkSize, 0);
    }

    @Override
    public List<TextChunk> split(Document document) {
        String content = document.content();
        if (content == null || content.isBlank()) {
            return List.of();
        }

        List<TextChunk> chunks = new ArrayList<>();
        int start = 0;
        int chunkIndex = 0;

        while (start < content.length()) {
            int end = Math.min(start + chunkSize, content.length());
            String chunkContent = content.substring(start, end);

            String chunkId = document.id() + "-chunk-" + chunkIndex;
            chunks.add(new TextChunk(
                    chunkId, document.id(), chunkContent,
                    document.metadata()));

            start += chunkSize - overlap;
            chunkIndex++;
        }
        return chunks;
    }
}
