package com.acme.assistant;

import com.acme.assistant.client.OpenAiClient;
import com.acme.assistant.llm.*;
import com.acme.assistant.llm.client.LlmClient;
import com.acme.assistant.llm.client.OpenAiLlmClient;
import com.acme.assistant.tool.*;
import com.acme.assistant.tool.bash.BashTool;
import com.acme.assistant.tool.bash.CommandValidator;
import com.acme.assistant.tool.file.*;
import com.acme.assistant.tool.todo.TodoStore;
import com.acme.assistant.tool.todo.TodoTool;
import com.acme.assistant.tool.validator.AllowedToolValidator;
import com.acme.assistant.tool.web.MockSearchProvider;
import com.acme.assistant.tool.web.WebSearchTool;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;


public class Main {

    private static final String SYSTEM_PROMPT = """
            당신은 프로젝트 분석 전문가 AI 비서입니다.
            사용 가능한 도구를 활용하여 프로젝트 구조를 분석하고,
            파일을 읽고, 검색하고, 작업 목록을 관리할 수 있습니다.
            작업 흐름:
            1. grep 이나 bash 로 프로젝트 구조를 파악한다
            2. file_read 로 관심 있는 파일을 읽는다
            3. todo 로 발견한 항목을 정리한다
            4. file_tracker 로 작업 이력을 확인한다
           """;

    private final LlmClient client;
    private final LlmModel model;
    private final ToolRegistry registry;
    private final ToolExecutionManager executionManager;
    private final TokenTracker tokenTracker;

    public Main(
            LlmClient client,
            LlmModel model,
            ToolRegistry registry,
            ToolExecutionManager executionManager,
            TokenTracker tokenTracker
    ) {
        this.client = client;
        this.model = model;
        this.registry = registry;
        this.executionManager = executionManager;
        this.tokenTracker = tokenTracker;
    }

    public static void main(String[] args) throws Exception {
        String apiKey = System.getenv("OPENAI_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            System.err.println("OPENAI_API_KEY 환경 변수를 설정해야 합니다.");
            return;
        }

        // 기본 디렉토리 설정
        Path baseDir = Path.of(".")
                .toAbsolutePath().normalize();
        PathValidator pathValidator = new PathValidator(baseDir);
        CommandValidator commandValidator = new CommandValidator();
        FileTracker fileTracker = new FileTracker();
        TodoStore todoStore = new TodoStore();

        // 9개 도구 등록
        ToolRegistry registry = new ToolRegistry();
        registry.register(new CurrentTimeTool());
        registry.register(new FileReadTool(pathValidator));
        registry.register(new FileWriteTool(pathValidator));
        registry.register(new FileEditTool(pathValidator));
        registry.register(new GrepTool(pathValidator));
        registry.register(new BashTool(commandValidator, pathValidator));
        registry.register(new WebSearchTool(new MockSearchProvider()));
        registry.register(new TodoTool(todoStore));
        registry.register(new FileTrackerTool(fileTracker));

        // 안전한 도구만 허용
        AllowedToolValidator validator = new AllowedToolValidator(
                "current_time", "file_read", "grep",
                "bash", "todo", "file_tracker"
        );

        ToolExecutionManager executionManager = new ToolExecutionManager(registry, validator);

        LlmClient llmClient = new OpenAiLlmClient(
                new OpenAiClient(apiKey));
        LlmModel model = new LlmModel("gpt-4o-mini");
        TokenTracker tokenTracker = new TokenTracker();

        Main app = new Main(
                llmClient, model, registry, executionManager, tokenTracker
        );

        List<ChatMessage> messages = new ArrayList<>();
        messages.add(ChatMessage.ofSystem(SYSTEM_PROMPT));
        messages.add(ChatMessage.ofUser("프로젝트 구조를 분석해줘"));

        String answer = app.chat(messages);

        System.out.println("\n[최종 응답] " + answer);
        System.out.println("\n[토큰 사용량] " + tokenTracker.summary());
    }

    public String chat(List<ChatMessage> messages) throws Exception {
        ToolContext context = ToolContext.of("main", "user");
        List<ToolDefinition> tools = registry.toToolDefinitions();

        while (true) {
            LlmResponse response = client.chat(model, messages, tools);
            tokenTracker.add(response.tokenUsage());

            if (!response.hasToolCalls()) {
                return response.content();
            }

            messages.add(response.toAssistantMessage());

            for (LlmToolCall toolCall : response.toolCalls()) {
                ToolUse toolUse = ToolUse.from(toolCall);
                System.out.println("[도구 호출] " + toolUse.name() + " - " + toolUse.arguments());

                ToolUseResult result = executionManager.execute(toolUse, context);

                if (result.isError()) {
                    System.out.println("[오류] " + result.content());
                } else {
                    System.out.println("[결과] " + truncate(result.content(), 200));
                }

                messages.add(
                        ChatMessage.ofTool(toolCall.id(), result.content())
                );
            }
        }
    }

    private String truncate(String text, int maxLength) {
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength) + "...";
    }
}