package com.acme.assistant.tool.file;

import java.time.Instant;

public record FileRecord(
        String path,
        FileOperation operation,
        Instant timestamp
) {
    @Override
    public String toString() {
        return operation + " " + path + " (" + timestamp + ")";
    }
}
