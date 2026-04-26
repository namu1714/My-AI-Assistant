package com.acme.assistant.tool.implementation.web;

import java.util.List;

public interface SearchProvider {
    List<SearchResult> search(String query, int maxResults);
}
