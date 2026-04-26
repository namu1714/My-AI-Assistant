package com.acme.assistant.tool.implementation;

import com.acme.assistant.tool.*;
import com.acme.assistant.tool.validator.CommandValidator;
import com.acme.assistant.tool.validator.PathValidator;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class BashTool extends AbstractTool {

    private static final int DEFAULT_TIMEOUT = 30;
    private static final int MAX_OUTPUT_BYTES = 100_100;

    private final CommandValidator commandValidator;
    private final PathValidator pathValidator;

    public BashTool(CommandValidator commandValidator, PathValidator pathValidator) {
        super(new ToolDefinition(
                "bash",
                "셸 명령을 실행한다. "
                        + "타임아웃(기본 30초)과 출력 제한(100KB)이 적용된다.",
                JsonSchemaBuilder.objectSchema()
                        .property("command", "string", "실행할 셸 명령")
                        .property("timeout", "integer", "타임아웃 (초, 기본값 30)")
                        .property("working_directory", "string", "작업 디렉터리 (기본값: 기본 디렉터리)")
                        .required("command")
                        .build()
        ));
        this.commandValidator = commandValidator;
        this.pathValidator = pathValidator;
    }

    @Override
    public ToolResult execute(ToolInput input, ToolContext context) {
        try {
            String command = input.requireString("command");
            int timeout = input.optionalInt("timeout", DEFAULT_TIMEOUT);

            commandValidator.validate(command);

            Path workDir = input.optionalString("working_directory")
                    .map(pathValidator::validate)
                    .orElse(pathValidator.baseDirectory());

            if (!Files.isDirectory(workDir)) {
                return ToolResult.error("작업 디렉터리가 존재하지 않습니다: " + workDir);
            }

            ProcessBuilder pb = new ProcessBuilder("/bin/bash", "-c", command)
                    .directory(workDir.toFile())
                    .redirectErrorStream(true);

            Process process = pb.start();

            // 출력을 별도 스레드에서 읽는다
            var outputFuture = new CompletableFuture<String>();
            Thread reader = Thread.ofVirtual().start(() -> {
                try {
                    byte[] bytes = process.getInputStream()
                            .readNBytes(MAX_OUTPUT_BYTES);
                    outputFuture.complete(new String(bytes, StandardCharsets.UTF_8));
                } catch (IOException e) {
                    outputFuture.complete("");
                }
            });

            boolean finished = process.waitFor(timeout, TimeUnit.SECONDS);

            if (!finished) {
                process.destroyForcibly();
                reader.interrupt();
                String partialOutput = outputFuture.getNow("");
                return ToolResult.error(
                        "타임아웃 (" + timeout + "초) 초과. 출력:\n" + partialOutput
                );
            }
            reader.join(1000);
            String output = outputFuture.getNow("");

            int exitCode = process.exitValue();
            String result = "exit_code: " + exitCode
                    + "\n" + output;

            if (result.length() > MAX_OUTPUT_BYTES) {
                result = result.substring(0, MAX_OUTPUT_BYTES)
                        + "\n... (출력이 100KB 로 제한됨)";
            }
            return exitCode == 0
                    ? ToolResult.success(result)
                    : ToolResult.error(result);
        } catch (SecurityException e) {
            return ToolResult.error(
                    " 명령 차단: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return ToolResult.error(" 명령이 인터럽트되었습니다");
        } catch (Exception e) {
            return ToolResult.error(
                    " 명령 실행 오류: " + e.getMessage());
        }
    }
}
