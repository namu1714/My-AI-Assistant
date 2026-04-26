package com.acme.assistant.tool.validator;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;

public class PathValidator {

    private final Path baseDirectory;

    public PathValidator(Path baseDirectory) {
        this.baseDirectory = baseDirectory.toAbsolutePath().normalize();
    }

    public Path validate(String pathString) {
        if (pathString == null || pathString.isBlank()) {
            throw new IllegalArgumentException("경로가 비어 있습니다");
        }

        Path resolved;
        try {
            Path requested = Path.of(pathString);
            if (requested.isAbsolute()) {
                resolved = requested.normalize();
            } else {
                resolved = baseDirectory.resolve(requested).normalize();
            }
        } catch (InvalidPathException e) {
            throw new IllegalArgumentException("잘못된 경로: " + pathString, e);
        }

        if (!resolved.startsWith(baseDirectory)) {
            throw new SecurityException("기본 디렉터리 바깥 접근 차단: " + resolved);
        }
        return resolved;
    }

    public Path baseDirectory() {
        return baseDirectory;
    }
}
