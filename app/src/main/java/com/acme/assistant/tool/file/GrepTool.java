package com.acme.assistant.tool.implementation;

import com.acme.assistant.tool.*;
import com.acme.assistant.tool.validator.PathValidator;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class GrepTool extends AbstractTool {

    private static final int MAX_RESULTS = 100;

    private final PathValidator pathValidator;

    public GrepTool(PathValidator pathValidator) {
        super(new ToolDefinition(
                "grep",
                "디렉터리에서 정규식 패턴과 일치하는 줄을 검색한다. "
                        + "결과는 '파일: 줄번호: 내용' 형식이다.",
                JsonSchemaBuilder.objectSchema()
                        .property("pattern", "string",
                                "검색할 정규식 패턴")
                        .property("path", "string",
                                "검색할 디렉터리 경로 (기본값: 기본 디렉터리)")
                        .property("include", "string",
                                "파일 이름 glob 패턴 (예: *.java)")
                        .required("pattern")
                        .build()
        ));
        this.pathValidator = pathValidator;
    }

    @Override
    public ToolResult execute(ToolInput input, ToolContext context) {
        try {
            String patternStr = input.requireString("pattern");
            String pathStr = input.optionalString("path")
                    .orElse(pathValidator.baseDirectory().toString());
            String include = input.optionalString("include")
                    .orElse(null);

            Pattern regex;
            try {
                regex = Pattern.compile(patternStr);
            } catch (PatternSyntaxException e) {
                return ToolResult.error("정규식 오류: " + e.getMessage());
            }

            Path searchDir = pathValidator.validate(pathStr);

            if (!Files.isDirectory(searchDir)) {
                return ToolResult.error("디렉터리가 아닙니다: " + pathStr);
            }

            PathMatcher matcher = include != null
                    ? searchDir.getFileSystem().getPathMatcher("glob:" + include)
                    : null;

            List<String> results = new ArrayList<>();

            Files.walkFileTree(searchDir, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (results.size() >= MAX_RESULTS) {
                        return FileVisitResult.TERMINATE;
                    }
                    if (matcher != null
                            && !matcher.matches(file.getFileName())) {
                        return FileVisitResult.CONTINUE;
                    }
                    if (!Files.isRegularFile(file)
                            || Files.size(file) > 1_000_000) {
                        return FileVisitResult.CONTINUE;
                    }
                    try {
                        List<String> lines = Files.readAllLines(file);
                        Path relative = pathValidator.baseDirectory()
                                .relativize(file);
                        for (int i = 0; i < lines.size(); i++) {
                            Matcher m = regex.matcher(lines.get(i));
                            if (m.find()) {
                                results.add(relative + ":"
                                        + (i + 1) + ":" + lines.get(i));
                                if (results.size() >= MAX_RESULTS) {
                                    return FileVisitResult.TERMINATE;
                                }
                            }
                        }
                    } catch (Exception ignored) {
                        // 바이너리 파일 등 읽기 실패 무시
                    }
                    return FileVisitResult.CONTINUE;
                }
            });

            if (results.isEmpty()) {
                return ToolResult.success("일치하는 결과가 없습니다.");
            }

            String summary = results.size() > MAX_RESULTS
                    ? "\n... (결과가 " + MAX_RESULTS + "개로 제한됨)" : "";

            return ToolResult.success(String.join("\n", results) + summary);

        } catch (SecurityException e) {
            return ToolResult.error("경로 접근 거부: " + e.getMessage());
        } catch (Exception e) {
            return ToolResult.error("검색 오류: " + e.getMessage());
        }
    }
}
