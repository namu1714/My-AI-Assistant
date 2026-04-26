package com.acme.assistant.tool.implementation.web;

import com.acme.assistant.tool.*;

import java.util.List;

public class WebSearchTool extends AbstractTool {

    private static final int DEFAULT_MAX_RESULTS = 5;

    private final SearchProvider searchProvider;

    public WebSearchTool(SearchProvider searchProvider) {
        super(new ToolDefinition(
                "web_search",
                "웹에서 검색어로 검색하여 결과를 반환한다.",
                JsonSchemaBuilder.objectSchema()
                        .property("query", "string", "검색할 질의어")
                        .property("max_results", "integer", "최대 결과 수 (기본값 5)")
                        .required("query")
                        .build()
        ));
        this.searchProvider = searchProvider;
    }

    @Override
    public ToolResult execute(ToolInput input, ToolContext context) {
        try {
            String query = input.requireString("query");
            int maxResults = input.optionalInt("max_results", DEFAULT_MAX_RESULTS);

            List<SearchResult> results = searchProvider.search(query, maxResults);

            if (results.isEmpty()) {
                return ToolResult.success("검색 결과가 없습니다: " + query);
            }

            var sb = new StringBuilder();
            for (int i = 0; i < results.size(); i++) {
                if (i > 0) sb.append("\n\n");
                sb.append("[").append(i + 1).append("] ")
                        .append(results.get(i));
            }

            return ToolResult.success(sb.toString());
        } catch (Exception e) {
            return ToolResult.error("검색 오류: " + e.getMessage());
        }
    }
}
