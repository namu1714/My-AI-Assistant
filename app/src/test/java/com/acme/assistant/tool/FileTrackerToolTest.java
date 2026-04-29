package com.acme.assistant.tool;

import com.acme.assistant.tool.file.FileOperation;
import com.acme.assistant.tool.file.FileTracker;
import com.acme.assistant.tool.file.FileTrackerTool;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class FileTrackerToolTest {

    @Test
    void 파일_작업을_기록한다() {
        FileTracker tracker = new FileTracker();

        tracker.record("src/Main.java", FileOperation.READ);
        tracker.record("src/Main.java", FileOperation.EDIT);
        tracker.record("README.md", FileOperation.WRITE);

        assertThat(tracker.getRecords()).hasSize(3);
    }

    @Test
    void 요약을_생성한다() {
        FileTracker tracker = new FileTracker();

        tracker.record("src/Main.java", FileOperation.READ);
        tracker.record("README.md", FileOperation.WRITE);

        String summary = tracker.summary();

        assertThat(summary).contains("총 2건");
        assertThat(summary).contains("READ");
        assertThat(summary).contains("WRITE");
    }

    @Test
    void 요약을_반환한다() {
        FileTracker tracker = new FileTracker();

        tracker.record("Main.java", FileOperation.READ);
        tracker.record("README.md", FileOperation.WRITE);

        FileTrackerTool tool = new FileTrackerTool(tracker);

        ToolInput input = ToolInput.parse(
                "{\"action\":\"summary\"}");

        ToolResult result = tool.execute(input, ToolContext.empty());

        assertThat(result.isError()).isFalse();
        assertThat(result.content()).contains("총 2건");
    }
}
