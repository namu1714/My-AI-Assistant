package com.acme.assistant.tool.web;

public record SearchResult(
        String title, String url, String snippet
) {
    @Override
    public String toString() {
        return title + "\n" + url + "\n" + snippet;
    }
}
