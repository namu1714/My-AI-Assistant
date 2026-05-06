package com.acme.assistant.rag;

import java.util.List;

public interface DocumentLoader {

    List<Document> load();
}
