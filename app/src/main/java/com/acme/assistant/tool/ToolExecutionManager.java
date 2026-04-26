package com.acme.assistant.tool;

import com.acme.assistant.tool.validator.ToolPermissionValidator;

import java.util.List;

public class ToolExecutionManager {

    private final ToolRegistry registry;
    private final ToolPermissionValidator validator;

    public ToolExecutionManager(ToolRegistry registry) {
        this(registry, (toolName, context) -> true);
    }

    public ToolExecutionManager(
            ToolRegistry registry,
            ToolPermissionValidator validator
    ) {
        this.registry = registry;
        this.validator = validator;
    }

    public ToolUseResult execute(ToolUse toolUse, ToolContext context) {
        // 1. 도구 조회
        var toolOpt = registry.getTool(toolUse.name());
        if (toolOpt.isEmpty()) {
            return ToolUseResult.error(
                    toolUse.id(),
                    "알 수 없는 도구: " + toolUse.name()
            );
        }
        Tool tool = toolOpt.get();

        // 2. 권한 검증
        if (!validator.isAllowed(toolUse.name(), context)) {
            return ToolUseResult.error(
                    toolUse.id(),
                    toolUse.name() + " 도구의 실행이 거부되었습니다"
            );
        }

        // 3. 입력 파싱
        ToolInput input;
        try {
            input = ToolInput.parse(toolUse.arguments());
        } catch (IllegalArgumentException e) {
            return ToolUseResult.error(
                    toolUse.id(),
                    "입력 파싱 오류: " + e.getMessage()
            );
        }

        // 4. 도구 실행
        ToolResult result;
        try {
            result = tool.execute(input, context);
        } catch (Exception e) {
            System.out.println("[ToolExecutionManager] 도구 실행 중 예외: " + tool.name() + " - " + e.getMessage());

            return ToolUseResult.error(
                    toolUse.id(),
                    "도구 실행 오류: " + e.getMessage()
            );
        }

        // 5. 결과 래핑
        return ToolUseResult.from(toolUse.id(), result);
    }

    public List<ToolUseResult> executeAll(List<ToolUse> toolUses, ToolContext context) {
        return toolUses.stream()
                .map(toolUse -> execute(toolUse, context))
                .toList();
    }
}
