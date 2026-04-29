package com.acme.assistant.tool.file;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FileTracker {

    private final List<FileRecord> records = new ArrayList<>();

    public void record(String path, FileOperation operation) {
        records.add(new FileRecord(path, operation, Instant.now()));
    }

    public List<FileRecord> getRecords() {
        return List.copyOf(records);
    }

    public List<FileRecord> getRecordsByOperation(
            FileOperation operation
    ) {
        return records.stream()
                .filter(r -> r.operation() == operation)
                .toList();
    }

    public String summary() {
        if (records.isEmpty()) {
            return "파일 작업 기록이 없습니다.";
        }

        Map<FileOperation, List<String>> grouped =
                records.stream()
                        .collect(Collectors.groupingBy(
                                FileRecord::operation,
                                LinkedHashMap::new,
                                Collectors.mapping(FileRecord::path, Collectors.toList())
                        ));

        var sb = new StringBuilder();
        sb.append("총 ").append(records.size())
                .append("건의 파일 작업:\n");

        for (var entry : grouped.entrySet()) {
            List<String> unique = entry.getValue().stream()
                    .distinct().toList();
            sb.append("\n").append(entry.getKey())
                    .append(" (").append(unique.size())
                    .append("개 파일):\n");
            for (String path : unique) {
                sb.append(" - ").append(path).append("\n");
            }
        }

        return sb.toString().stripTrailing();
    }
}
