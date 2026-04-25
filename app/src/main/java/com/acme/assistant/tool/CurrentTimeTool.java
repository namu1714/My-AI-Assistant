package com.acme.assistant.tool;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CurrentTimeTool extends AbstractTool {

    public CurrentTimeTool() {
        super(new ToolDefinition(
            "current_time",
            "현재 날짜와 시간을 반환한다",
            JsonSchemaBuilder.objectSchema().build()
        ));
    }

    @Override
    public String execute(String arguments) throws Exception {
        return LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}
