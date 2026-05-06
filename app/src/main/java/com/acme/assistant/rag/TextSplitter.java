package com.acme.assistant.rag;

import java.util.List;

public interface TextSplitter {

    List<TextChunk> split(Document document);
}
