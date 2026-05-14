package com.acme.assistant.agent;

import com.acme.assistant.tool.ToolRegistry;

public class SystemPromptBuilder {

    private SystemPromptBuilder() {}

    public static String build(String basePrompt, ToolRegistry toolRegistry) {
        boolean hasPrompt = basePrompt != null && !basePrompt.isBlank();
        boolean hasTools = toolRegistry != null && !toolRegistry.getAllTools().isEmpty();

        if (!hasPrompt && !hasTools) {
            return "";
        }

        var sb = new StringBuilder();

        if (hasPrompt) {
            sb.append(basePrompt);
        }

        if (hasTools) {
            if (hasPrompt) {
                sb.append("\n\n");
            }
            sb.append("## 사용 가능한 도구\n\n");

            toolRegistry.getAllTools().forEach(tool -> {
                sb.append("- **").append(tool.name())
                        .append("**: ")
                        .append(tool.description())
                        .append("\n");
            });

            sb.append("\n");
            sb.append("도구를 사용해서 사용자에 질문에 답할 수 있다. ");
            sb.append("도구 호출 결과를 확인한 후 최종 답변을 작성한다. ");
        }
        return sb.toString();
    }
}
