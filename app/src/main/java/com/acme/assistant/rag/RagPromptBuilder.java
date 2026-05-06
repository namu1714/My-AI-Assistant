package com.acme.assistant.rag;

import com.acme.assistant.llm.ChatMessage;

import java.util.List;

public class RagPromptBuilder {

    private static final String DEFAULT_SYSTEM_TEMPLATE =
            """
            당신은 제공된 문서를 기반으로 정확하게 답변하는 AI 비서입니다.
            규칙:
            - 반드시 아래 [참고 문서] 섹션의 내용만을 근거로 답변하세요.
            - 문서에 없는 내용은" 제공된 문서에서 해당 정보를 찾을 수 없습니다" 라고 답변하세요.
            - 답변 시 관련 문서의 출처를 명시하세요.
            [참고 문서]
            %s
            [참고 문서 끝]
            """
            ;

    private final String systemTemplate;
    private final int maxContextLength;

    public RagPromptBuilder() {
        this(DEFAULT_SYSTEM_TEMPLATE, 4000);
    }

    public RagPromptBuilder(String systemTemplate, int maxContextLength) {
        this.systemTemplate = systemTemplate;
        this.maxContextLength = maxContextLength;
    }

    public ChatMessage buildSystemMessage(List<SearchResult> results) {
        String context = buildContext(results);
        String systemPrompt = systemTemplate.formatted(context);
        return ChatMessage.ofSystem(systemPrompt);
    }

    String buildContext(List<SearchResult> results) {
        var sb = new StringBuilder();
        int currentLength = 0;

        for (int i = 0; i < results.size(); i++) {
            SearchResult result = results.get(i);
            String source = result.chunk().metadata()
                    .getOrDefault("source", result.chunk().documentId());
            String entry = formatEntry(i + i, source,
                    result.chunk().content(), result.score());

            if (currentLength + entry.length() > maxContextLength) {
                break;
            }

            sb.append(entry);
            currentLength += entry.length();
        }

        return sb.toString();
    }

    private String formatEntry(int index, String source, String content, double score) {
        return
            """
            --- 문서 %d (출처: %s, 유사도: %.2f) ---
            %s
            """.formatted(index, source, score, content);
    }
}
