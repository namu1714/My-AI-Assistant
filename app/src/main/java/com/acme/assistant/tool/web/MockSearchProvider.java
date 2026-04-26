package com.acme.assistant.tool.implementation.web;

import java.util.List;

public class MockSearchProvider implements SearchProvider {

    private static final List<SearchResult> MOCK_DATA = List.of(
            new SearchResult(
                    "Java 21 New Features",
                    "https://example.com/java21",
                    "Java 21 introduces record patterns, "
                            + "virtual threads, and more."),
            new SearchResult(
                    "OpenAI API Guide",
                    "https://example.com/openai-api",
                    "Complete guide to using the "
                            + "OpenAI Chat Completions API."),
            new SearchResult(
                    "Building AI Agents in Java",
                    "https://example.com/ai-agents",
                    "Step-by-step tutorial for building "
                            + "AI agents with Java.")
    );

    @Override
    public List<SearchResult> search(String query, int maxResults) {
        return MOCK_DATA.stream()
                .filter(r -> containsIgnoreCase(r, query))
                .limit(maxResults)
                .toList();
    }

    private boolean containsIgnoreCase(SearchResult result, String query) {
        String lower = query.toLowerCase();
        return result.title().toLowerCase().contains(lower)
                || result.snippet().toLowerCase().contains(lower);
    }
}
